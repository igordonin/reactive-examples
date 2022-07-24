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

  public static final String BEER_URL = "/api/v2/beer";
  public static final String BEER_URL_ID = "/api/v2/beer/{beerId}";
  public static final String BEER_UPC_URL_ID = "/api/v2/beerUpc/{upc}";

  @Bean
  public RouterFunction<ServerResponse> beerRoutesV2(BeerHandler handler) {
    return RouterFunctions.route()
        .GET(BEER_URL_ID, accept(APPLICATION_JSON), handler::getBeerById)
        .GET(BEER_UPC_URL_ID, accept(APPLICATION_JSON), handler::getBeerByUpc)
        .POST(BEER_URL, accept(APPLICATION_JSON), handler::saveBeer)
        .build();
  }
}
