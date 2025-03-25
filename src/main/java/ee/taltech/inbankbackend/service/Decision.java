package ee.taltech.inbankbackend.service;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Holds the response data of the REST endpoint.
 */
@Data
@AllArgsConstructor
public class Decision {
    private final Integer loanAmount;
    private final Integer loanPeriod;
    private final String errorMessage;
}
