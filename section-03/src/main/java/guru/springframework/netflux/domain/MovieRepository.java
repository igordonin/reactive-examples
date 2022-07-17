package guru.springframework.netflux.domain;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

interface MovieRepository extends ReactiveMongoRepository<Movie, String> {}
