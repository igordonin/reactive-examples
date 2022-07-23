package guru.springframework.reactivebeerclient.client;

import guru.springframework.reactivebeerclient.config.WebClientConfig;
import guru.springframework.reactivebeerclient.model.BeerDto;
import guru.springframework.reactivebeerclient.model.BeerPagedList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;

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
  void deleteById() {}

  @Test
  void getBeerByUpc() {
    var beerPagedListMono = beerClient.listBeers(null, null, null, null, null);
    var beerPagedList = beerPagedListMono.block();

    var upc = beerPagedList.stream().findFirst().get().getUpc();

    var beerDto = this.beerClient.getBeerByUpc(upc).block();

    assertThat(beerDto.getUpc()).isEqualTo(upc);
  }
}
