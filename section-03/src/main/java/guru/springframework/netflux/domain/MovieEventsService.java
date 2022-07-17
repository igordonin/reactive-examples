package guru.springframework.netflux.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class MovieEventsService {

  private MovieRepository movieRepository;

  public Flux<MovieEvent> streamMovieEvents(String id) {
    return Flux.<MovieEvent>generate(
            movieEventSynchronousSink -> {
              movieEventSynchronousSink.next(new MovieEvent(id, new Date()));
            })
        .delayElements(Duration.ofSeconds(1L));
  }
}
