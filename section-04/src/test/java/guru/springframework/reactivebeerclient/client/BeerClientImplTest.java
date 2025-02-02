package guru.springframework.reactivebeerclient.client;

import guru.springframework.reactivebeerclient.config.WebClientConfig;
import guru.springframework.reactivebeerclient.model.BeerDto;
import guru.springframework.reactivebeerclient.model.BeerPagedList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.http.HttpStatus.*;

class BeerClientImplTest {

  BeerClientImpl beerClient;

  @BeforeEach
  void setUp() {
    beerClient = new BeerClientImpl(new WebClientConfig().webClient());
  }

  @Test
  void getBeerById() {
    var beerPagedListMono = beerClient.listBeers(null, null, null, null, null);
    var beerPagedList = beerPagedListMono.block();

    var beerId = beerPagedList.stream().findFirst().orElse(null).getId();

    var beerDtoMono = beerClient.getBeerById(beerId, false);

    var beerDto = beerDtoMono.block();

    assertThat(beerDto.getId()).isEqualTo(beerId);
    assertThat(beerDto.getQuantityOnHand()).isNull();
  }

  @Test
  void getBeerByIdShowInventoryTrue() {
    var beerPagedListMono = beerClient.listBeers(null, null, null, null, null);
    var beerPagedList = beerPagedListMono.block();

    var beerId = beerPagedList.stream().findFirst().get().getId();

    var beerDtoMono = beerClient.getBeerById(beerId, true);

    var beerDto = beerDtoMono.block();

    assertThat(beerDto.getId()).isEqualTo(beerId);
    assertThat(beerDto.getQuantityOnHand()).isNotNull();
  }

  @Test
  void listBeers() {
    Mono<BeerPagedList> beerPagedListMono = beerClient.listBeers(null, null, null, null, null);

    BeerPagedList beerPagedList = beerPagedListMono.block();

    assertThat(beerPagedList).isNotNull();
    assertThat(beerPagedList.getContent().size()).isGreaterThan(0);
  }

  @Test
  void listBeersPageSize10() {
    Mono<BeerPagedList> beerPagedListMono = beerClient.listBeers(1, 10, null, null, null);

    BeerPagedList beerPagedList = beerPagedListMono.block();

    assertThat(beerPagedList).isNotNull();
    assertThat(beerPagedList.getContent().size()).isEqualTo(10);
  }

  @Test
  void listBeersPageSizeNoRecords() {
    Mono<BeerPagedList> beerPagedListMono = beerClient.listBeers(10, 20, null, null, null);

    BeerPagedList beerPagedList = beerPagedListMono.block();

    assertThat(beerPagedList).isNotNull();
    assertThat(beerPagedList.getContent().size()).isEqualTo(0);
  }

  @Test
  void createBeer() {
    var beerDto =
        BeerDto.builder()
            .beerName("Dogfinished 90 Min IPA")
            .beerStyle("IPA")
            .upc("12312")
            .price(new BigDecimal("10.99"))
            .build();

    var response = beerClient.createBeer(beerDto).block();

    assertThat(response.getStatusCode()).isEqualTo(CREATED);
  }

  @Test
  void updateBeer() {
    var beerPagedListMono = beerClient.listBeers(null, null, null, null, null);
    var beerPagedList = beerPagedListMono.block();

    var beerDto = beerPagedList.stream().findFirst().get();
    var beerId = beerDto.getId();

    beerDto.setId(null); // this is a really stupid requirement from the server
    beerDto.setBeerName("Updated Beer Name");

    var response = beerClient.updateBeer(beerId, beerDto).block();

    assertThat(response.getStatusCode()).isEqualTo(NO_CONTENT);
  }

  @Test
  void deleteById() {
    var beerPagedListMono = beerClient.listBeers(null, null, null, null, null);
    var beerPagedList = beerPagedListMono.block();

    var beerDto = beerPagedList.stream().findFirst().get();
    var beerId = beerDto.getId();

    var response = beerClient.deleteById(beerId).block();

    assertThat(response.getStatusCode()).isEqualTo(NO_CONTENT);
  }

  @Test
  void deleteByIdWhenNotFound() {
    var mono = beerClient.deleteById(UUID.randomUUID());
    assertThrows(WebClientResponseException.class, mono::block);
  }

  @Test
  void deleteByIdWhenNotFoundOption2() {
    var response =
        beerClient
            .deleteById(UUID.randomUUID())
            .onErrorResume(
                throwable -> {
                  if (throwable instanceof WebClientResponseException) {
                    var exception = (WebClientResponseException) throwable;
                    return Mono.just(ResponseEntity.status(exception.getStatusCode()).build());
                  }

                  throw new RuntimeException(throwable);
                })
            .block();

    assertThat(response.getStatusCode()).isEqualTo(NOT_FOUND);
  }

  @Test
  void getBeerByUpc() {
    var beerPagedListMono = beerClient.listBeers(null, null, null, null, null);
    var beerPagedList = beerPagedListMono.block();

    var upc = beerPagedList.stream().findFirst().get().getUpc();

    var beerDto = this.beerClient.getBeerByUpc(upc).block();

    assertThat(beerDto.getUpc()).isEqualTo(upc);
  }

  @Test
  void functionalTestGetBeerById() throws InterruptedException {
    AtomicReference<String> beerName = new AtomicReference<>();
    CountDownLatch countDownLatch = new CountDownLatch(1);

    beerClient
        .listBeers(null, null, null, null, null)
        .map(beerPagedList -> beerPagedList.getContent().get(0).getId())
        .map(beerId -> beerClient.getBeerById(beerId, false))
        .flatMap(mono -> mono)
        .subscribe(
            beerDto -> {
              beerName.set(beerDto.getBeerName());
              assertThat(beerName.get()).isNotBlank();
              countDownLatch.countDown();
            });

    // this is not recommended. just for learning
    // let's replace it with a count down latch.
    //    Thread.sleep(2000);

    countDownLatch.await();
  }
}
