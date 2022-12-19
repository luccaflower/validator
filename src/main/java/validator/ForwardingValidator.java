package validator;


import result.*;

import java.util.function.*;

public interface ForwardingValidator<T> extends Validator<T> {
    Validator<T> inner();
    @Override
    default Result<T, ValidationError> validate(Supplier<T> s) {
        return inner().validate(s);
    }
}
