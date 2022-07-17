package guru.springframework.reactivebeerclient.client;

import guru.springframework.reactivebeerclient.model.BeerDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BeerClientImpl implements BeerClient {

  private final WebClient webClient;

  @Override
  public Mono<BeerDto> getBeerById(UUID id, Boolean showInventoryOnHand) {
    return null;
  }

  @Override
  public Mono<Page<List<BeerDto>>> listBeers(
      Integer pageNumber,
      Integer pageSize,
      String beerName,
      String beerStyle,
      Boolean showInventoryOnHand) {
    return null;
  }

  @Override
  public Mono<ResponseEntity<Void>> createBeer(BeerDto beerDto) {
    return null;
  }

  @Override
  public Mono<ResponseEntity<Void>> updatedBeer(BeerDto beerDto) {
    return null;
  }

  @Override
  public Mono<ResponseEntity<Void>> deleteById(UUID id) {
    return null;
  }

  @Override
  public Mono<BeerDto> getBeerByUpc(String upc) {
    return null;
  }
}
