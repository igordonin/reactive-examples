package guru.springframework.sfgrestbrewery.services;

import guru.springframework.sfgrestbrewery.domain.Beer;
import guru.springframework.sfgrestbrewery.repositories.BeerRepository;
import guru.springframework.sfgrestbrewery.web.mappers.BeerMapper;
import guru.springframework.sfgrestbrewery.web.model.BeerDto;
import guru.springframework.sfgrestbrewery.web.model.BeerPagedList;
import guru.springframework.sfgrestbrewery.web.model.BeerStyleEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.stream.Collectors;

/** Created by jt on 2019-04-20. */
@Slf4j
@Service
@RequiredArgsConstructor
public class BeerServiceImpl implements BeerService {
  private final BeerRepository beerRepository;
  private final BeerMapper beerMapper;
  private final R2dbcEntityTemplate template;

  @Cacheable(cacheNames = "beerListCache", condition = "#showInventoryOnHand == false ")
  @Override
  public Mono<BeerPagedList> listBeers(
      String beerName,
      BeerStyleEnum beerStyle,
      PageRequest pageRequest,
      Boolean showInventoryOnHand) {

    var optionalBeerStyle = Optional.ofNullable(beerStyle);

    Query query = Query.empty();
    if (!beerName.isBlank() && optionalBeerStyle.isPresent()) {
      query = Query.query(Criteria.where("beerName").is(beerName).and("beerStyle").is(beerStyle));
    } else if (!beerName.isBlank() && optionalBeerStyle.isEmpty()) {
      query = Query.query(Criteria.where("beerName").is(beerName));
    } else if (beerName.isBlank() && optionalBeerStyle.isPresent()) {
      query = Query.query(Criteria.where("beerStyle").is(beerStyle));
    }

    return template
        .select(Beer.class)
        .matching(query.with(pageRequest))
        .all()
        .map(beerMapper::beerToBeerDto)
        .collect(Collectors.toList())
        .map(
            beers ->
                new BeerPagedList(
                    beers,
                    PageRequest.of(pageRequest.getPageNumber(), pageRequest.getPageSize()),
                    beers.size()));
  }

  @Cacheable(
      cacheNames = "beerCache",
      key = "#beerId",
      condition = "#showInventoryOnHand == false ")
  @Override
  public Mono<BeerDto> getById(Integer beerId, Boolean showInventoryOnHand) {
    if (showInventoryOnHand) {
      return beerRepository.findById(beerId).map(beerMapper::beerToBeerDtoWithInventory);
    }

    return beerRepository.findById(beerId).map(beerMapper::beerToBeerDto);
  }

  @Override
  public Mono<BeerDto> saveNewBeer(BeerDto beerDto) {
    return beerRepository.save(beerMapper.beerDtoToBeer(beerDto)).map(beerMapper::beerToBeerDto);
  }

  @Override
  public Mono<BeerDto> updateBeer(Integer beerId, BeerDto beerDto) {
    return beerRepository
        .findById(beerId)
        // I just rather throw an exception here, but JT provided an empty object which for me
        // does not make any sense
        //        .switchIfEmpty(Mono.error(new Exception("Bad Request")))
        .defaultIfEmpty(Beer.builder().build())
        .map(
            beer -> {
              beer.setBeerName(beerDto.getBeerName());
              beer.setBeerStyle(BeerStyleEnum.valueOf(beerDto.getBeerStyle()));
              beer.setPrice(beerDto.getPrice());
              beer.setUpc(beerDto.getUpc());

              return beer;
            })
        .flatMap(beerRepository::save)
        .map(beerMapper::beerToBeerDto);
  }

  @Cacheable(cacheNames = "beerUpcCache")
  @Override
  public Mono<BeerDto> getByUpc(String upc) {
    return beerRepository.findByUpc(upc).map(beerMapper::beerToBeerDto);
  }

  // I don't like it not returning a Mono<Void>.
  @Override
  public void deleteBeerById(Integer beerId) {
    beerRepository.deleteById(beerId).subscribe();
  }
}
