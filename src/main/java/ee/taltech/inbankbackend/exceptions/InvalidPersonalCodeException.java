package ee.taltech.inbankbackend.exceptions;

/**
 * Thrown when the provided personal ID code is invalid.
 */
public class InvalidPersonalCodeException extends RuntimeException {

    public InvalidPersonalCodeException(String message) {
        super(message);
    }
}
