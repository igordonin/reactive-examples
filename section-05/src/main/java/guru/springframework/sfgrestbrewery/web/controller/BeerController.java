package guru.springframework.sfgrestbrewery.web.controller;

import guru.springframework.sfgrestbrewery.services.BeerService;
import guru.springframework.sfgrestbrewery.web.model.BeerDto;
import guru.springframework.sfgrestbrewery.web.model.BeerPagedList;
import guru.springframework.sfgrestbrewery.web.model.BeerStyleEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/** Created by jt on 2019-04-20. */
@RequiredArgsConstructor
@RequestMapping("/api/v1/")
@RestController
public class BeerController {

  private static final Integer DEFAULT_PAGE_NUMBER = 0;
  private static final Integer DEFAULT_PAGE_SIZE = 25;

  private final BeerService beerService;

  @GetMapping(
      produces = {"application/json"},
      path = "beer")
  public ResponseEntity<Mono<BeerPagedList>> listBeers(
      @RequestParam(value = "pageNumber", required = false) Integer pageNumber,
      @RequestParam(value = "pageSize", required = false) Integer pageSize,
      @RequestParam(value = "beerName", required = false) String beerName,
      @RequestParam(value = "beerStyle", required = false) BeerStyleEnum beerStyle,
      @RequestParam(value = "showInventoryOnHand", required = false) Boolean showInventoryOnHand) {

    if (showInventoryOnHand == null) {
      showInventoryOnHand = false;
    }

    if (pageNumber == null || pageNumber < 0) {
      pageNumber = DEFAULT_PAGE_NUMBER;
    }

    if (pageSize == null || pageSize < 1) {
      pageSize = DEFAULT_PAGE_SIZE;
    }

    var beerList =
        beerService.listBeers(
            beerName, beerStyle, PageRequest.of(pageNumber, pageSize), showInventoryOnHand);

    return ResponseEntity.ok(beerList);
  }

  @GetMapping("beer/{beerId}")
  public ResponseEntity<Mono<BeerDto>> getBeerById(
      @PathVariable("beerId") Integer beerId,
      @RequestParam(value = "showInventoryOnHand", required = false) Boolean showInventoryOnHand) {

    var beer = beerService.getById(beerId, Optional.ofNullable(showInventoryOnHand).orElse(false));

    return ResponseEntity.ok(beer);
  }

  @GetMapping("beerUpc/{upc}")
  public ResponseEntity<Mono<BeerDto>> getBeerByUpc(@PathVariable("upc") String upc) {
    var beer = beerService.getByUpc(upc);
    return ResponseEntity.ok(beer);
  }

  @PostMapping(path = "beer")
  public ResponseEntity saveNewBeer(@RequestBody @Validated BeerDto beerDto) {

    var beerId = new AtomicInteger();

    beerService
        .saveNewBeer(beerDto)
        .subscribe(
            savedBeerDto -> {
              beerId.set(savedBeerDto.getId());
            });

    return ResponseEntity.created(
            UriComponentsBuilder.fromHttpUrl(
                    "http://api.springframework.guru/api/v1/beer/" + beerId)
                .build()
                .toUri())
        .build();
  }

  @PutMapping("beer/{beerId}")
  public ResponseEntity<Void> updateBeerById(
      @PathVariable("beerId") Integer beerId, @RequestBody @Validated BeerDto beerDto) {

    // This is a really poor workaround.
    var entityFound = new AtomicBoolean(false);

    beerService
        .updateBeer(beerId, beerDto)
        .subscribe(
            updatedDto -> {
              if (updatedDto.getId() != null) {
                entityFound.set(true);
              }
            });

    if (entityFound.get()) {
      return ResponseEntity.noContent().build();
    }

    return ResponseEntity.notFound().build();
  }

  @DeleteMapping("beer/{beerId}")
  public ResponseEntity<Void> deleteBeerById(@PathVariable("beerId") Integer beerId) {
    beerService.deleteBeerById(beerId);

    return ResponseEntity.ok().build();
  }
}
