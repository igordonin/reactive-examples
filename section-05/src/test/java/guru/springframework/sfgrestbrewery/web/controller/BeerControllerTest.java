package guru.springframework.sfgrestbrewery.web.controller;

import guru.springframework.sfgrestbrewery.bootstrap.BeerLoader;
import guru.springframework.sfgrestbrewery.services.BeerService;
import guru.springframework.sfgrestbrewery.web.model.BeerDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;

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
