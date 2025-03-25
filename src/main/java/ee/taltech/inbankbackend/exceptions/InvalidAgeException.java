package ee.taltech.inbankbackend.exceptions;

/**
 * Thrown when the requested loan amount is invalid.
 */
public class InvalidAgeException extends RuntimeException {

    public InvalidAgeException(String message) {
        super(message);
    }
}
