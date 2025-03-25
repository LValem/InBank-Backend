package ee.taltech.inbankbackend.exceptions;

/**
 * Thrown when no valid loan can be found for the provided input.
 */
public class NoValidLoanException extends RuntimeException {

    public NoValidLoanException(String message) {
        super(message);
    }
}
