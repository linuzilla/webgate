package ncu.mac.webgate.controllers;

import ncu.mac.webgate.properties.WebGateProperties;
import ncu.mac.webgate.services.WebGatewayService;
import ncu.mac.webgate.services.WebGatewayServiceImpl;
import ncu.mac.webgate.services.tweaks.UriManipulateTweak;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@EnableConfigurationProperties(WebGateProperties.class)
public class WebGateController {
    private static final Logger logger = LoggerFactory.getLogger(WebGateController.class);
//    private static final String DEFAULT_USER_AGENT = "Mozilla/5.0 (iPhone; CPU iPhone OS 14_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/14.0 EdgiOS/45.11.11 Mobile/15E148 Safari/605.1.15";

    private final Map<String, WebGatewayService> virtualHostMap;
    private final WebGateProperties webGateProperties;

    public WebGateController(ApplicationContext applicationContext, WebGateProperties webGateProperties) {
        this.webGateProperties = webGateProperties;

//        try {
//            System.out.println(new ObjectMapper().writeValueAsString(webGateProperties));
//        } catch (JsonProcessingException e) {
//            e.printStackTrace();
//        }
        final var uriManipulateTweaks = new ArrayList<>(applicationContext.getBeansOfType(UriManipulateTweak.class)
                .values());

        logger.debug("UriManipulateTweaks: {} record(s)", uriManipulateTweaks.size());

        this.virtualHostMap = webGateProperties.getHosts().entrySet()
                .stream()
                .map(entry -> new AbstractMap.SimpleEntry<>(entry.getKey().replaceAll("_", "."),
                        new WebGatewayServiceImpl(uriManipulateTweaks, entry.getValue(), webGateProperties.getUserAgent())))
                .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));

    }

    public Mono<ServerResponse> permitAllProxying(ServerRequest serverRequest) {
//        logger.info("Session: {}", webSession.getId());
        return serverRequest.headers().header(HttpHeaders.HOST)
                .stream()
                .findFirst()
                .map(virtualHostWithPort -> virtualHostWithPort.split(":")[0])
                .map(virtualHost -> {
                    logger.debug("VirtualHost: {}", virtualHost);
                    return virtualHost;
                })
                .map(virtualHostMap::get)
                .map(webGatewayService -> webGatewayService.proxying(serverRequest))
                .orElseGet(() -> {
                    logger.info("VirtualHost not found, redirect: {}", webGateProperties.getDefaultRedirection());

                    return ServerResponse.status(HttpStatus.FOUND)
                            .header(HttpHeaders.LOCATION, webGateProperties.getDefaultRedirection())
                            .build();
                });
    }
}
