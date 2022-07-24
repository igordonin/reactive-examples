package guru.springframework.sfgrestbrewery.web.controller;

import guru.springframework.sfgrestbrewery.bootstrap.BeerLoader;
import guru.springframework.sfgrestbrewery.web.model.BeerDto;
import guru.springframework.sfgrestbrewery.web.model.BeerPagedList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.netty.http.client.HttpClient;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.MediaType.APPLICATION_JSON;

/** Created by jt on 3/7/21. */
public class WebClientIntegrationTest {

  public static final String BASE_URL = "http://localhost:8080";

  WebClient webClient;

  @BeforeEach
  void setUp() {
    webClient =
        WebClient.builder()
            .baseUrl(BASE_URL)
            .clientConnector(new ReactorClientHttpConnector(HttpClient.create().wiretap(true)))
            .build();
  }

  @Test
  void getBeerByUpc() throws InterruptedException {
    CountDownLatch countDownLatch = new CountDownLatch(1);

    var beerDtoMono =
        webClient
            .get()
            .uri("/api/v1/beerUpc/" + BeerLoader.BEER_2_UPC)
            .accept(APPLICATION_JSON)
            .retrieve()
            .bodyToMono(BeerDto.class);

    beerDtoMono.subscribe(
        beer -> {
          assertThat(beer).isNotNull();
          assertThat(beer.getBeerName()).isNotBlank();

          countDownLatch.countDown();
        });

    countDownLatch.await(1000, MILLISECONDS);
    assertThat(countDownLatch.getCount()).isEqualTo(0);
  }

  @Test
  void getBeerId() throws InterruptedException {
    CountDownLatch countDownLatch = new CountDownLatch(1);

    var beerDtoMono =
        webClient
            .get()
            .uri("/api/v1/beer")
            .accept(APPLICATION_JSON)
            .retrieve()
            .bodyToMono(BeerDto.class);

    beerDtoMono.subscribe(
        beer -> {
          assertThat(beer).isNotNull();
          assertThat(beer.getBeerName()).isNotBlank();

          countDownLatch.countDown();
        });

    countDownLatch.await(1000, MILLISECONDS);
    assertThat(countDownLatch.getCount()).isEqualTo(0);
  }

  @Test
  void testSaveBeer() throws InterruptedException {
    CountDownLatch countDownLatch = new CountDownLatch(1);

    var beerDto =
        BeerDto.builder()
            .beerName("JTs Beer")
            .upc("1231141")
            .beerStyle("PALE_ALE")
            .price(new BigDecimal("8.99"))
            .build();

    var beerDtoMono =
        webClient
            .post()
            .uri("/api/v1/beer")
            .accept(APPLICATION_JSON)
            .body(BodyInserters.fromValue(beerDto))
            .retrieve()
            .toBodilessEntity();

    beerDtoMono
        .publishOn(Schedulers.parallel())
        .subscribe(
            responseEntity -> {
              assertThat(responseEntity.getStatusCode().is2xxSuccessful()).isTrue();
              countDownLatch.countDown();
            });

    countDownLatch.await(1000, MILLISECONDS);
    assertThat(countDownLatch.getCount()).isEqualTo(0);
  }

  @Test
  void testSaveBeerBadRequest() throws InterruptedException {
    CountDownLatch countDownLatch = new CountDownLatch(1);

    var beerDto = BeerDto.builder().price(new BigDecimal("8.99")).build();

    var beerDtoMono =
        webClient
            .post()
            .uri("/api/v1/beer")
            .accept(APPLICATION_JSON)
            .body(BodyInserters.fromValue(beerDto))
            .retrieve()
            .toBodilessEntity();

    beerDtoMono
        .publishOn(Schedulers.parallel()) // TODO Why did he use this?
        .doOnError(
            throwable -> {
              countDownLatch.countDown();
            })
        .subscribe(responseEntity -> {});

    countDownLatch.await(1000, MILLISECONDS);
    assertThat(countDownLatch.getCount()).isEqualTo(0);
  }

  @Test
  void testUpdateBeer() throws InterruptedException {

    CountDownLatch countDownLatch = new CountDownLatch(3);

    webClient
        .get()
        .uri("/api/v1/beer")
        .accept(APPLICATION_JSON)
        .retrieve()
        .bodyToMono(BeerPagedList.class)
        .publishOn(Schedulers.single())
        .subscribe(
            pagedList -> {
              countDownLatch.countDown();

              // get existing beer
              BeerDto beerDto = pagedList.getContent().get(0);

              BeerDto updatePayload =
                  BeerDto.builder()
                      .beerName("JTsUpdate")
                      .beerStyle(beerDto.getBeerStyle())
                      .upc(beerDto.getUpc())
                      .price(beerDto.getPrice())
                      .build();

              // update existing beer
              webClient
                  .put()
                  .uri("/api/v1/beer/" + beerDto.getId())
                  .contentType(APPLICATION_JSON)
                  .body(BodyInserters.fromValue(updatePayload))
                  .retrieve()
                  .toBodilessEntity()
                  .flatMap(
                      responseEntity -> {
                        // get and verify update
                        countDownLatch.countDown();
                        return webClient
                            .get()
                            .uri("/api/v1/beer/" + beerDto.getId())
                            .accept(APPLICATION_JSON)
                            .retrieve()
                            .bodyToMono(BeerDto.class);
                      })
                  .subscribe(
                      savedDto -> {
                        assertThat(savedDto.getBeerName()).isEqualTo("JTsUpdate");
                        countDownLatch.countDown();
                      });
            });

    countDownLatch.await(1000, MILLISECONDS);
    assertThat(countDownLatch.getCount()).isEqualTo(0);
  }

  @Test
  void testUpdateBeerNotFound() throws InterruptedException {

    CountDownLatch countDownLatch = new CountDownLatch(2);

    var exceptionClassName =
        "org.springframework.web.reactive.function.client.WebClientResponseException$NotFound";

    BeerDto updatePayload =
        BeerDto.builder()
            .beerName("JTsUpdate")
            .beerStyle("PALE_ALE")
            .upc("12345667")
            .price(new BigDecimal("9.99"))
            .build();

    webClient
        .put()
        .uri("/api/v1/beer/" + 200)
        .contentType(APPLICATION_JSON)
        .body(BodyInserters.fromValue(updatePayload))
        .retrieve()
        .toBodilessEntity()
        .subscribe(
            responseEntity -> {},
            throwable -> {
              if (throwable.getClass().getName().equals(exceptionClassName)) {
                var ex = (WebClientResponseException) throwable;

                if (ex.getStatusCode().equals(NOT_FOUND)) {
                  countDownLatch.countDown();
                }
              }
            });

    countDownLatch.countDown();

    countDownLatch.await(1000, MILLISECONDS);
    assertThat(countDownLatch.getCount()).isEqualTo(0);
  }

  @Test
  void testListBeers() throws InterruptedException {
    CountDownLatch countDownLatch = new CountDownLatch(1);

    Mono<BeerPagedList> beerPagedListMono =
        webClient
            .get()
            .uri("/api/v1/beer")
            .accept(APPLICATION_JSON)
            .retrieve()
            .bodyToMono(BeerPagedList.class);

    //        BeerPagedList pagedList = beerPagedListMono.block();
    //        pagedList.getContent().forEach(beerDto -> System.out.println(beerDto.toString()));
    beerPagedListMono
        .publishOn(Schedulers.parallel())
        .subscribe(
            beerPagedList -> {
              beerPagedList.getContent().forEach(beerDto -> System.out.println(beerDto.toString()));

              countDownLatch.countDown();
            });

    countDownLatch.await();
  }
}
