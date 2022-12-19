import org.junit.jupiter.api.*;
import result.*;
import validator.*;

import java.util.function.*;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static result.Result.*;
import static result.Unit.*;

class ValidatorTest {
    @Test
    void identityValidatorPasses() {
        assertThat(Validator.succeed().validate(Unit::unit), is(ok(unit())));
    }

    @Test
    void aRuleIsAPredicate() {
        assertThat(
            Validator.from(in -> true, Exception::new).validate(Unit::unit),
            is(ok(unit()))
        );
    }

    @Test
    void aFailingRuleGivesAnError() {
        assertThat(
            Validator.from(in -> false, Exception::new).validate(Unit::unit)
                .isErr(),
            is(true)
        );
    }

    @Test
    void aValidationErrorContainsTheError() {
        assertThat(
            Validator.from(in -> false, () -> new Exception("Failed"))
                .validate(Unit::unit)
                .unwrapErr()
                .getMessage()
            ,
            containsString("Failed")
        );
    }

    @Test
    void theValidationErrorAssociatedWithTheRuleIsThrown() {
        assertThat(
            Validator.from(in -> true, () -> new Exception("First"))
                .and(in -> false, () -> new Exception("Second"))
                .validate(Unit::unit)
                .unwrapErr()
                .getMessage(),
            containsString("Second")
        );
    }

    @Test
    void endResult() {
        Validator.Rule<TestDummy> flagIsTrue = (TestDummy t) -> t.flag;
        Validator.Rule<TestDummy> somethingIsSomething = t -> t.something.equals("something");
        Supplier<TestDummy> createDummy = () -> new TestDummy("something", true);
        var result = Validator
            .from(flagIsTrue, () -> new Exception("Flag is false"))
            .and(somethingIsSomething, () -> new Exception("Missing something"))
            .validate(createDummy);
        assertThat(result.isOkAnd(ok ->
            ok.flag && ok.something.equals("something")), is(true));
    }

    static class TestDummy {
        String something;
        boolean flag;

        public TestDummy(String something, boolean flag) {
            this.something = something;
            this.flag = flag;
        }
    }
}
