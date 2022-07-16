package guru.springframework.reactiveexamples;

import guru.springframework.reactiveexamples.domain.Person;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by jt on 2/27/21.
 */
public class PersonRepositoryImpl implements PersonRepository {

    private final Set<Person> people = new HashSet<>();

    {
        var michael = Person.builder().id(1).firstName("Michael").lastName("Phelps").build();
        var frida = Person.builder().id(2).firstName("Frida").lastName("Kahlo").build();
        var senna = Person.builder().id(3).firstName("Ayrton").lastName("Senna").build();
        var mansel = Person.builder().id(4).firstName("Nigel").lastName("Mansel").build();

        people.addAll(Arrays.asList(michael, frida, senna, mansel));
    }

    @Override
    public Mono<Person> getById(Integer id) {
        return people.stream()
                .filter(it -> it.getId().equals(id))
                .findFirst()
                .map(Mono::just)
                .orElse(Mono.empty());
    }

    @Override
    public Flux<Person> findAll() {
        return Flux.fromStream(people.stream());
    }
}
