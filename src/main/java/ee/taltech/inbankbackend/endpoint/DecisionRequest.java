package ee.taltech.inbankbackend.endpoint;

import ee.taltech.inbankbackend.service.Countries;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Holds the request data of the REST endpoint
 */
@Data
@AllArgsConstructor
public class DecisionRequest {
    private String personalCode;
    private Long loanAmount;
    private int loanPeriod;
    private Countries country;
}
