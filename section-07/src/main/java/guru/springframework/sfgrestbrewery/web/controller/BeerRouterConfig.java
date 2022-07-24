package guru.springframework.sfgrestbrewery.web.controller;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;

@Configuration
public class BeerRouterConfig {

  @Bean
  public RouterFunction<ServerResponse> beerRoutesV2(BeerHandler handler) {
    return RouterFunctions.route()
        .GET("/api/v2/beer/{beerId}", accept(APPLICATION_JSON), handler::getBeerById)
        .build();
  }
}
