package guru.springframework.netflux.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("movies")
@RequiredArgsConstructor
public class MovieController {

  private final MovieDomainService movieDomainService;
  private final MovieEventsService movieEventsService;

  @GetMapping("{id}")
  Mono<Movie> findById(@PathVariable String id) {
    return this.movieDomainService.findById(id);
  }

  @GetMapping
  Flux<Movie> findAll() {
    return this.movieDomainService.findAll();
  }

  @GetMapping(value = "{id}/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  Flux<MovieEvent> streamMovieEvents(@PathVariable String id) {
    return this.movieEventsService.streamMovieEvents(id);
  }
}
