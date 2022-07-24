package guru.springframework.sfgrestbrewery.repositories;

import guru.springframework.sfgrestbrewery.domain.Beer;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface BeerRepository extends ReactiveCrudRepository<Beer, Integer> {
  Mono<Beer> findByUpc(String upc);
}
