package ncu.mac.webgate.services.tweaks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

import java.security.Principal;

public interface UriManipulateTweak {
    Logger logger = LoggerFactory.getLogger(UriManipulateTweak.class);

    default boolean matches(ServerRequest serverRequest) {
        return false;
    }

    @Nullable
    default UriManipulateTweak accept(ServerRequest serverRequest) {
        return matches(serverRequest) ? this : null;
    }

    default String tweakUri(Principal principal, ServerRequest serverRequest, WebSession webSession) {
        final var uri = serverRequest.uri();

        if (uri.getQuery() != null) {
            return uri.getPath() + "?" + uri.getQuery();
        } else {
            return uri.getPath();
        }
    }

    default WebClient.RequestHeadersSpec<?> tweakResponseBody(Principal principal, ServerRequest serverRequest, WebSession webSession, WebClient.RequestBodySpec requestBodySpec, MediaType mediaType, DataBuffer dataBuffer) {
        final var contentLength = serverRequest.headers().contentLength().orElse(0);
        requestBodySpec.contentLength(contentLength);
        requestBodySpec.contentType(mediaType);

        if (logger.isDebugEnabled()) {
            logger.debug("<<< Data Content-Type: {}, Length: {}", mediaType.toString(), contentLength);
        }
        return requestBodySpec.body(BodyInserters.fromDataBuffers(Mono.just(dataBuffer)));
    }

    default Mono<DataBuffer> pageContentAnalyzer(Principal principal, ClientResponse.Headers headers, DataBuffer dataBuffer) {
        return Mono.just(dataBuffer);
    }

    default void tweakHeader(HttpHeaders httpHeaders) {
    }
}