package guru.springframework.sfgrestbrewery.web.controller;

import guru.springframework.sfgrestbrewery.bootstrap.BeerLoader;
import guru.springframework.sfgrestbrewery.services.BeerService;
import guru.springframework.sfgrestbrewery.web.model.BeerDto;
import guru.springframework.sfgrestbrewery.web.model.BeerPagedList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@WebFluxTest(BeerController.class)
public class BeerControllerTest {

  @Autowired WebTestClient webTestClient;

  @MockBean BeerService beerService;

  BeerDto validBeer;

  @BeforeEach
  void setUp() {
    validBeer =
        BeerDto.builder()
            .beerName("Test Beer")
            .beerStyle("PALE_ALE")
            .upc(BeerLoader.BEER_1_UPC)
            .build();
  }

  @Test
  void listBeers() {
    List<BeerDto> beers = Arrays.asList(validBeer);

    BeerPagedList beerPagedList = new BeerPagedList(beers, PageRequest.of(0, 10), beers.size());

    given(beerService.listBeers(any(), any(), any(), any())).willReturn(beerPagedList);

    webTestClient
        .get()
        .uri("/api/v1/beer")
        .accept(APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(BeerPagedList.class);
  }

  @Test
  void getBeerByUpc() {
    given(beerService.getByUpc(any())).willReturn(validBeer);

    webTestClient
        .get()
        .uri("/api/v1/beerUpc/" + validBeer.getUpc())
        .accept(APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(BeerDto.class)
        .value(BeerDto::getBeerName, equalTo(validBeer.getBeerName()));
  }

  @Test
  void getBeerById() {
    var beerId = UUID.randomUUID();
    given(beerService.getById(any(), any())).willReturn(validBeer);

    webTestClient
        .get()
        .uri("/api/v1/beer/" + beerId)
        .accept(APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(BeerDto.class)
        .value(BeerDto::getBeerName, equalTo(validBeer.getBeerName()));
  }
}
