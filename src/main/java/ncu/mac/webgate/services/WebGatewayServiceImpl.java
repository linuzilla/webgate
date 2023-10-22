package ncu.mac.webgate.services;

import ncu.mac.webgate.constants.Constants;
import ncu.mac.webgate.constants.Routes;
import ncu.mac.webgate.helpers.CookieHelper;
import ncu.mac.webgate.properties.WebGateProperties;
import ncu.mac.webgate.services.tweaks.UriManipulateTweak;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.MimeType;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class WebGatewayServiceImpl implements WebGatewayService {
    private static final Logger logger = LoggerFactory.getLogger(WebGatewayServiceImpl.class);

    private final WebClient webClient;
    private final WebGateProperties.VirtualHost webGateProperties;
    private final Map<String, String> replacements;
    private final String userAgent;
    private final List<UriManipulateTweak> uriManipulateTweaks;

    private final UriManipulateTweak dummyManipulateTweak = new UriManipulateTweak() {
    };

    public WebGatewayServiceImpl(List<UriManipulateTweak> uriManipulateTweaks, WebGateProperties.VirtualHost webGateProperties, String userAgent) {
        this.userAgent = userAgent;

        replacements = webGateProperties.getTextReplacements()
                .stream()
                .filter(strings -> strings.size() == 2)
                .peek(s -> logger.info("{} -> {}", s.get(0), s.get(1)))
                .map(s -> Pair.of(s.get(0), s.get(1)))
                .collect(Collectors.toUnmodifiableMap(Pair::getFirst, Pair::getSecond));

        this.uriManipulateTweaks = uriManipulateTweaks;

//        try {
//            System.out.println(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(webGateProperties));
//            System.out.println(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(replacements));
//        } catch (JsonProcessingException ignore) {
//        }

        this.webClient = WebClient.builder()
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(20 * 1024 * 1024)).build())
                .baseUrl(webGateProperties.getTarget())
//                .defaultHeader(HttpHeaders.USER_AGENT, webGateProperties.getUserAgent())
                .build();
        this.webGateProperties = webGateProperties;
    }

    private String manipulatePage(DataBuffer dataBuffer) {
        final var pageContext = new AtomicReference<>(dataBuffer.toString(StandardCharsets.UTF_8));

        replacements.forEach((regex, replacement) -> pageContext.set(
                pageContext.get().replaceAll(regex, replacement)
        ));

        logger.debug(">>> Data: {}", pageContext.get().substring(0, Math.min(pageContext.get().length(), 20)));
        return pageContext.get();
    }

    @Override
    public Mono<ServerResponse> proxying(ServerRequest serverRequest) {

        final var manipulateTweak = uriManipulateTweaks.stream()
                .filter(uriManipulateTweak -> uriManipulateTweak.matches(serverRequest))
                .map(uriManipulateTweak -> uriManipulateTweak.accept(serverRequest))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(dummyManipulateTweak);

        return serverRequest.session()
                .flatMap(webSession -> serverRequest.principal()
                        .flatMap(principal -> {
//                            logger.info("username: {}, principal: {}", principal.getName(), principal.getClass().getName());

                            final var cookieStrings = new ArrayList<List<String>>();

                            final var requestBodySpec = this.webClient
                                    .method(Objects.requireNonNullElse(serverRequest.method(), HttpMethod.GET))
                                    .uri(manipulateTweak.tweakUri(principal, serverRequest, webSession))
                                    .headers(httpHeaders -> sendingHeaderManipulate(serverRequest, httpHeaders, cookieStrings::add));

                            final var cookieString = CookieHelper.filterCookie(cookieStrings, strings -> CookieHelper.cookieNameFilter(strings[0]));

                            if (StringUtils.hasText(cookieString)) {
                                logger.debug("Cookie String: {}", cookieString);
                                requestBodySpec.header(HttpHeaders.COOKIE, cookieString);
                            }

                            return serverRequest.headers().contentType()
                                    .map(mediaType -> serverRequest.bodyToMono(DataBuffer.class)
                                            .flatMap(dataBuffer -> manipulateTweak.tweakResponseBody(principal, serverRequest, webSession, requestBodySpec, mediaType, dataBuffer)
                                                    .exchangeToMono(clientResponse -> sendProxyRequest(serverRequest, clientResponse, principal, manipulateTweak)))
                                    )
                                    .orElseGet(() -> requestBodySpec.exchangeToMono(clientResponse -> sendProxyRequest(serverRequest, clientResponse, principal, manipulateTweak)));
                        }));
    }

    private void sendingHeaderManipulate(ServerRequest request, HttpHeaders httpHeaders, Consumer<List<String>> cookieHandler) {
        request.headers().asHttpHeaders()
                .forEach((headerName, headerValues) -> {
                    if (HttpHeaders.HOST.equalsIgnoreCase(headerName)) {
                        httpHeaders.put(headerName, List.of(webGateProperties.getHostHeader()));
                        logger.debug("<<< Header: Host: {} -> {}", headerValues.get(0), webGateProperties.getHostHeader());
                    } else if (HttpHeaders.USER_AGENT.equalsIgnoreCase(headerName) && StringUtils.hasText(userAgent)) {
                        httpHeaders.put(headerName, List.of(userAgent));
                        logger.debug("<<< Header: User-Agent: {}", headerValues.get(0));
                    } else if (HttpHeaders.ORIGIN.equalsIgnoreCase(headerName)) {
                        final var originHeaderValue = new AtomicReference<>(headerValues.get(0));
                        // reverse replacement
                        replacements.forEach((regex, replacement) -> originHeaderValue.set(
                                originHeaderValue.get().replaceAll(replacement, regex)
                        ));
                        logger.debug("Origin: {}", originHeaderValue.get());
                        httpHeaders.put(headerName, List.of(originHeaderValue.get()));
                    } else if (HttpHeaders.REFERER.equalsIgnoreCase(headerName)) {
                    } else if (HttpHeaders.COOKIE.equalsIgnoreCase(headerName)) {
                        cookieHandler.accept(headerValues);
//                    } else if (headerName.startsWith("X-")) {
                    } else {
                        httpHeaders.put(headerName, headerValues);
                        logger.debug("<<< Header: {}={}", headerName, headerValues.get(0));
                    }
                });
    }

    private void redirectLocationManipulate(HttpHeaders httpHeaders, String redirectUri) {
        logger.debug("Redirect URI: {}", redirectUri);

        if (redirectUri.startsWith(webGateProperties.getPauseLoginOnRedirectTo())) {
            httpHeaders.put(HttpHeaders.LOCATION, List.of(Routes.PORTAL));
        } else {
            final var location = new AtomicReference<>(redirectUri);

            replacements.forEach((regex, replacement) -> location.set(
                    location.get().replaceAll(regex, replacement)
            ));

            httpHeaders.put(HttpHeaders.LOCATION, List.of(location.get()));

            logger.debug(">>> Location: {}", location.get());
        }
    }

    private void receivedHeaderManipulate(ClientResponse clientResponse, HttpHeaders httpHeaders) {
        clientResponse.headers()
                .asHttpHeaders()
                .forEach((headerName, headerValues) -> {
                    if (logger.isDebugEnabled()) {
                        logger.debug(">>> Header: {}={}", headerName, headerValues.get(0));
                    }
                    if (HttpHeaders.LOCATION.equalsIgnoreCase(headerName)) {
                        redirectLocationManipulate(httpHeaders, headerValues.get(0));
                    } else if (Constants.X_REQUESTED_WITH.equalsIgnoreCase(headerName)) {
                        logger.warn(">>> Header: {}={}", headerName, headerValues.get(0));
                    } else if (HttpHeaders.SET_COOKIE.equalsIgnoreCase(headerName)) {
                        if (logger.isDebugEnabled()) {
                            headerValues.forEach(s -> logger.debug(">>> Set-Cookie ({}) = {}", headerName, s));
                        }
                        httpHeaders.put(headerName, headerValues);
                    } else {
                        httpHeaders.put(headerName, headerValues);
                    }
                });

        httpHeaders.put("X-Center5-Test", List.of("center5-test"));
    }

    private Mono<ServerResponse> sendProxyRequest(ServerRequest request, ClientResponse clientResponse, Principal principal, UriManipulateTweak uriManipulateTweak) {
        final var httpStatus = clientResponse.statusCode();

        if (logger.isDebugEnabled()) {
            logger.debug(">>> Status: {}, {} [{}] {}",
                    httpStatus, request.method(),
                    request.headers().contentType().map(MimeType::toString).orElse("-"),
                    request.uri().toString());
        }

        final var builder = ServerResponse.status(httpStatus)
                .headers(httpHeaders -> {
                    receivedHeaderManipulate(clientResponse, httpHeaders);
                    uriManipulateTweak.tweakHeader(httpHeaders);
                });

        if (httpStatus.is2xxSuccessful()) {
            AtomicBoolean gotDataBuffer = new AtomicBoolean(false);

            return clientResponse.bodyToMono(DataBuffer.class)
                    .doOnNext(dataBuffer -> gotDataBuffer.set(true))
                    .flatMap(dataBuffer -> uriManipulateTweak.pageContentAnalyzer(principal, clientResponse.headers(), dataBuffer))
                    .flatMap(dataBuffer -> clientResponse.headers().contentType()
                            .map(mediaType -> {
                                logger.debug(">>> [{}] Content-Type: {}", request.uri().toString(), mediaType.toString());
                                return mediaType;
                            })
                            .filter(mediaType -> mediaType.equalsTypeAndSubtype(MediaType.TEXT_HTML))
                            .map(mediaType -> builder.body(BodyInserters.fromPublisher(
                                    Mono.just(manipulatePage(dataBuffer)), String.class)))
                            .orElseGet(() -> builder.body(BodyInserters.fromDataBuffers(Mono.just(dataBuffer)))))
                    .doFinally(signalType -> {
                        if (!gotDataBuffer.get()) {
                            logger.warn("No dataBuffer get {}", httpStatus);
                        }
                    });
        } else {
            logger.debug("Status: {}", httpStatus);
        }
        return builder.bodyValue("");
    }
}
