package guru.springframework.reactivebeerclient.client;

import guru.springframework.reactivebeerclient.config.WebClientProperties;
import guru.springframework.reactivebeerclient.model.BeerDto;
import guru.springframework.reactivebeerclient.model.BeerPagedList;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Optional;
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
  public Mono<BeerPagedList> listBeers(
      Integer pageNumber,
      Integer pageSize,
      String beerName,
      String beerStyle,
      Boolean showInventoryOnHand) {
    return webClient
        .get()
        .uri(
            uriBuilder ->
                uriBuilder
                    .path(WebClientProperties.BEER_V1_PATH)
                    .queryParamIfPresent("pageNumber", Optional.ofNullable(pageNumber))
                    .queryParamIfPresent("pageSize", Optional.ofNullable(pageSize))
                    .queryParamIfPresent("beerName", Optional.ofNullable(beerName))
                    .queryParamIfPresent("beerStyle", Optional.ofNullable(beerStyle))
                    .queryParamIfPresent(
                        "showInventoryOnHand", Optional.ofNullable(showInventoryOnHand))
                    .build())
        .retrieve()
        .bodyToMono(BeerPagedList.class);
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
