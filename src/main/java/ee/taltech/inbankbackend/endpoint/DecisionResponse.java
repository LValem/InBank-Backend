package ee.taltech.inbankbackend.endpoint;

import lombok.Data;
import org.springframework.stereotype.Component;

/**
 * Holds the response data of the REST endpoint.
 */
@Data
@Component
public class DecisionResponse {
    private Integer loanAmount;
    private Integer loanPeriod;
    private String errorMessage;
}
