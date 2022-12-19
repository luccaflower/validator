package validator;

public class ValidationError extends Exception {
    public ValidationError(Exception e) {
        super(e);
    }

    public ValidationError() {
        super();
    }
}
