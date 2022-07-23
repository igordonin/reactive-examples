package guru.springframework.reactivebeerclient.client;

import guru.springframework.reactivebeerclient.model.BeerDto;
import guru.springframework.reactivebeerclient.model.BeerPagedList;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface BeerClient {

  Mono<BeerDto> getBeerById(UUID id, Boolean showInventoryOnHand);

  Mono<BeerPagedList> listBeers(
      Integer pageNumber,
      Integer pageSize,
      String beerName,
      String beerStyle,
      Boolean showInventoryOnHand);

  Mono<ResponseEntity<Void>> createBeer(BeerDto beerDto);

  Mono<ResponseEntity<Void>> updateBeer(UUID id, BeerDto beerDto);

  Mono<ResponseEntity<Void>> deleteById(UUID id);

  Mono<BeerDto> getBeerByUpc(String upc);
}
