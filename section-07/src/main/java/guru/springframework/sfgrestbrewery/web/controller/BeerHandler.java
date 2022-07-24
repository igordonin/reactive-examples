package guru.springframework.sfgrestbrewery.web.controller;

import guru.springframework.sfgrestbrewery.services.BeerService;
import guru.springframework.sfgrestbrewery.web.model.BeerDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static guru.springframework.sfgrestbrewery.web.controller.BeerRouterConfig.BEER_URL;

@Slf4j
@Component
@RequiredArgsConstructor
public class BeerHandler {
  private final BeerService beerService;

  public Mono<ServerResponse> getBeerById(ServerRequest request) {
    var beerId = Integer.valueOf(request.pathVariable("beerId"));
    var showInventory = Boolean.valueOf(request.queryParam("showInventory").orElse("false"));

    return beerService
        .getById(beerId, showInventory)
        .flatMap(beerDto -> ServerResponse.ok().bodyValue(beerDto))
        .switchIfEmpty(ServerResponse.notFound().build());
  }

  public Mono<ServerResponse> getBeerByUpc(ServerRequest request) {
    var upc = request.pathVariable("upc");

    return beerService
        .getByUpc(upc)
        .flatMap(beerDto -> ServerResponse.ok().bodyValue(beerDto))
        .switchIfEmpty(ServerResponse.notFound().build());
  }

  public Mono<ServerResponse> saveBeer(ServerRequest serverRequest) {
    var beerDtoMono = serverRequest.bodyToMono(BeerDto.class);

    return beerService
        .saveBeer(beerDtoMono)
        .flatMap(
            beerDto ->
                ServerResponse.ok().header("location", BEER_URL + "/" + beerDto.getId()).build());
  }
}
