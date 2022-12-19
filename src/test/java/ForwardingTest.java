import org.junit.jupiter.api.*;
import result.*;
import validator.*;

import java.util.function.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class ForwardingTest {

    @Test
    void test() {
        var validateNameAndAge = PersonValidator.create()
            .validName()
            .isAdult();

        Supplier<Person> validPerson = () -> new Person("valid", 18);
        Supplier<Person> invalidName = () -> new Person("invalid", 18);
        Supplier<Person> notOfAge = () -> new Person("valid", 16);

        assertThat(validateNameAndAge.validate(validPerson).isOk(), is(true));
        assertThat(validateNameAndAge.validate(invalidName).isErr(), is(true));
        assertThat(validateNameAndAge.validate(notOfAge).isErr(), is(true));
    }

    static class Person {
        String name;
        int age;

        public Person(String name, int age) {
            this.name = name;
            this.age = age;
        }


        public boolean isAdult() {
            return age >= 18;
        }
    }

    static class PersonValidator implements ForwardingValidator<Person> {

        private Validator<Person> inner;
        private PersonValidator(Validator<Person> inner) {
            this.inner = inner;
        }

        @Override
        public Validator<Person> inner() {
            return inner;
        }
        
        public static PersonValidator create() {
            return new PersonValidator(Validator.succeed());
        }

        public PersonValidator isAdult() {
            return new PersonValidator(this.and(
                Person::isAdult,
                () -> new Exception("Person must be adult"))
            );
        }

        public PersonValidator validName() {
            return new PersonValidator(this.and(
                p -> p.name.equals("valid"),
                () -> new Exception("Invalid name")
            ));
        }
    }
}
