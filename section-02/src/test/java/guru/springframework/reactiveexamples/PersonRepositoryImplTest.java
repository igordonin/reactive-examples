package guru.springframework.reactiveexamples;

import guru.springframework.reactiveexamples.domain.Person;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static java.lang.System.out;

class PersonRepositoryImplTest {

    PersonRepositoryImpl personRepository;

    @BeforeEach
    void setUp() {
        personRepository = new PersonRepositoryImpl();
    }

    @Test
    public void getByIdBlock() {
        var personMono = this.personRepository.getById(1);
        var person = personMono.block();
        out.println(person.toString());
    }

    @Test
    public void getByIdSubscribe() {
        var personMono = this.personRepository.getById(1);
        StepVerifier.create(personMono).expectNextCount(1).verifyComplete();
        personMono.subscribe(person -> out.println(person.toString()));
    }

    @Test
    public void getByIdSubscribeNotFound() {
        var personMono = this.personRepository.getById(11);
        StepVerifier.create(personMono).verifyComplete();
        personMono.subscribe(person -> out.println(person.toString()));
    }

    @Test
    public void getByIdMapFunction() {
        var personMono = this.personRepository.getById(1);
        personMono.map(Person::getFirstName).subscribe(out::println);
    }

    @Test
    public void findAllBlockFirst() {
        var personFlux = this.personRepository.findAll();
        var person = personFlux.blockFirst();
        out.println(person.toString());
    }

    @Test
    public void findAllSubscribe() {
        var personFlux = this.personRepository.findAll();
        StepVerifier.create(personFlux).expectNextCount(4).verifyComplete();
        personFlux.subscribe(person -> out.println(person.toString()));
    }

    @Test
    public void findAllToListMono() {
        var personFlux = this.personRepository.findAll();

        Mono<List<Person>> personsMonoList = personFlux.collectList();

        personsMonoList.subscribe(list -> {
            list.forEach(out::println);
        });
    }

    @Test
    public void findPersonMonoById() {
        var personFlux = this.personRepository.findAll();

        personFlux.filter(person -> person.getId().equals(2))
                .next()
                .subscribe(person -> out.println(person.toString()));
    }

    @Test
    public void findPersonMonoByIdNotFound() {
        var personFlux = this.personRepository.findAll();

        personFlux.filter(person -> person.getId().equals(10))
                .next()
                .subscribe(person -> out.println(person.toString()));
    }

    @Test
    public void findPersonMonoByIdNotFoundWithException() {
        var personFlux = this.personRepository.findAll();

        final Integer id = 10;

        personFlux.filter(person -> person.getId().equals(id))
                .single()
                .doOnError(it -> out.println("We found no one with that id " + id))
                .onErrorReturn(Person.builder().id(id).build())
                .subscribe(person -> out.println(person.toString()));
    }
}