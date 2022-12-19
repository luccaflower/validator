package validator;

import result.*;

import java.util.function.*;
import java.util.stream.*;

import static result.Result.*;

@SuppressWarnings("unused")
@FunctionalInterface
public interface Validator<T> {
    Result<T, ValidationError> validate(Supplier<T> s);

    default Validator<T> and(Validator<T> v) {
        return in -> validate(in).and(v.validate(in));
    }

    default Validator<T> and(Rule<T> r, Supplier<? extends Exception> e) {
        return in -> validate(in).and(from(r, e).validate(in));
    }

    default Validator<T> or(Rule<T> r, Supplier<? extends Exception> e) {
        return in -> validate(in).or(from(r, e).validate(in));
    }

    default Validator<T> or(Validator<T> v) {
        return in -> validate(in).or(v.validate(in));
    }

    static <T> Validator<T> from(Rule<T> r, Supplier<? extends Exception> e) {
        return in -> r.appliesTo(in.get())
            ? ok(in.get())
            : err(new ValidationError(e.get()));
    }

    static <T> Validator<T> not(Rule<T> r, Supplier<? extends Exception> e) {
        return from(r.negate(), e);
    }

    static <T> Validator<T> allOf(Stream<Validator<T>> v) {
        return v.reduce(succeed(), Validator::and);
    }

    static <T> Validator<T> anyOf(Stream<Validator<T>> v) {
        return v.reduce(fail(), Validator::or);
    }

    static <T> Validator<T> succeed() {
        return in -> ok(in.get());
    }

    static <T> Validator<T> fail() {
        return in -> err(new ValidationError());
    }

    @FunctionalInterface
    interface Rule<T> {
        boolean appliesTo(T t);

        default Rule<T> negate() {
            return (T in) -> !appliesTo(in);
        }

        static <T> Rule<T> not(Rule<T> r) {
            return r.negate();
        }
    }
}
