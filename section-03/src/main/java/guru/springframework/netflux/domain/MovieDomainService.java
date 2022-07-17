package guru.springframework.netflux.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class MovieDomainService {

  private final MovieRepository movieRepository;

  public Mono<Movie> findById(String id) {
    return this.movieRepository.findById(id);
  }

  public Flux<Movie> findAll() {
    return this.movieRepository.findAll();
  }

  public Mono<Void> deleteAll() {
    return this.movieRepository.deleteAll();
  }

  public Mono<Movie> save(Movie movie) {
    return this.movieRepository.save(movie);
  }
}
