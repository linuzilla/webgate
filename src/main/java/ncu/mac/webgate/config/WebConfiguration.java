package ncu.mac.webgate.config;


import ncu.mac.webgate.controllers.WebGateController;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;

@Configuration
//@EnableConfigurationProperties(WebGateProperties.class)
public class WebConfiguration {
    private final WebGateController webGateController;

    public WebConfiguration(WebGateController webGateController) {
        this.webGateController = webGateController;
    }

    @Bean
    RouterFunction<?> routes() {
        return RouterFunctions.route(RequestPredicates.path("/**"), webGateController::permitAllProxying);
    }

}
