package guru.springframework.netflux.bootstrap;

import guru.springframework.netflux.domain.Movie;
import guru.springframework.netflux.domain.MovieDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@RequiredArgsConstructor
@Component
public class InitMovies implements CommandLineRunner {

  private final MovieDomainService movieDomainService;

  @Override
  public void run(String... args) {
    this.movieDomainService
        .deleteAll()
        .thenMany(
            Flux.just(
                    "Silence of the Lambs",
                    "Aeon Flux",
                    "Lord of the Rings",
                    "Monte Cristo",
                    "The Gangster")
                .map(title -> Movie.builder().title(title).build())
                .flatMap(movieDomainService::save))
        .subscribe(null, null, () -> movieDomainService.findAll().subscribe(System.out::println));
  }
}
