package guru.springframework.sfgrestbrewery.web.controller;

import guru.springframework.sfgrestbrewery.services.BeerService;
import guru.springframework.sfgrestbrewery.web.model.BeerDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

import static guru.springframework.sfgrestbrewery.web.controller.BeerRouterConfig.BEER_URL;

@Slf4j
@Component
@RequiredArgsConstructor
public class BeerHandler {
  private final BeerService beerService;
  private final Validator validator;

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

  public Mono<ServerResponse> saveBeer(ServerRequest request) {
    var beerDtoMono = request.bodyToMono(BeerDto.class).doOnNext(this::validate);

    return beerService
        .saveBeer(beerDtoMono)
        .flatMap(
            beerDto ->
                ServerResponse.ok().header("location", BEER_URL + "/" + beerDto.getId()).build());
  }

  public Mono<ServerResponse> updateBeer(ServerRequest request) {
    var beerId = Integer.valueOf(request.pathVariable("beerId"));
    var beerDtoMono = request.bodyToMono(BeerDto.class).doOnNext(this::validate);

    return beerService
        .updateBeerByMono(beerId, beerDtoMono)
        .flatMap(beerDto -> ServerResponse.noContent().build());
  }

  public Mono<ServerResponse> deleteBeer(ServerRequest request) {
    var beerId = Integer.valueOf(request.pathVariable("beerId"));

    return beerService
        .reactiveDeleteBeerById(beerId)
        .flatMap(voidMono -> ServerResponse.ok().build())
        .onErrorResume(e -> e instanceof NotFoundException, e -> ServerResponse.notFound().build());
  }

  private void validate(BeerDto beerDto) {
    var errors = new BeanPropertyBindingResult(beerDto, "beerDto");
    validator.validate(beerDto, errors);

    if (errors.hasErrors()) {
      throw new ServerWebInputException(errors.toString());
    }
  }
}
