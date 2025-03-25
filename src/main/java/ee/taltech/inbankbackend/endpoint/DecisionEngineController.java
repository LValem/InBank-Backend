package ee.taltech.inbankbackend.endpoint;

import ee.taltech.inbankbackend.exceptions.InvalidLoanAmountException;
import ee.taltech.inbankbackend.exceptions.InvalidLoanPeriodException;
import ee.taltech.inbankbackend.exceptions.InvalidPersonalCodeException;
import ee.taltech.inbankbackend.exceptions.NoValidLoanException;
import ee.taltech.inbankbackend.service.Decision;
import ee.taltech.inbankbackend.service.DecisionEngine;
import ee.taltech.inbankbackend.config.DecisionEngineConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/loan")
@CrossOrigin
public class DecisionEngineController {

    private final DecisionEngine decisionEngine;
    private final DecisionResponse response;

    @Autowired
    DecisionEngineController(DecisionEngine decisionEngine, DecisionResponse response) {
        this.decisionEngine = decisionEngine;
        this.response = response;
    }

    /**
     * A REST endpoint that handles requests for loan decisions.
     * The endpoint accepts POST requests with a request body containing the customer's personal ID code,
     * requested loan amount, and loan period.<br><br>
     * - If the personal code, loan amount, or loan period is invalid, the endpoint returns a 400 Bad Request
     *   with a JSON body containing an error message, and null loan details.<br>
     * - If no valid loan can be calculated, the endpoint returns a 404 Not Found
     *   with an error message explaining the reason.<br>
     * - If an unexpected error occurs, the endpoint returns a 500 Internal Server Error
     *   with a generic error message.<br>
     * - If a valid loan is found, the endpoint returns a 200 OK with a JSON body containing:
     *   the approved loan amount, loan period, and no error message.
     *
     * @param request The request body containing the customer's personal ID code, requested loan amount, and loan period
     * @return A ResponseEntity with a DecisionResponse body containing the approved loan amount, period,
     *         and error message if applicable
     */
    @PostMapping("/decision")
    public ResponseEntity<DecisionResponse> requestDecision(@RequestBody DecisionRequest request) {
        DecisionResponse response = new DecisionResponse();

        try {
            Decision decision = decisionEngine
                    .calculateApprovedLoan(request.getPersonalCode(), request.getLoanAmount(), request.getLoanPeriod());

            response.setLoanAmount(decision.getLoanAmount());
            response.setLoanPeriod(decision.getLoanPeriod());
            response.setErrorMessage(decision.getErrorMessage());

            return ResponseEntity.ok(response);

        } catch (InvalidPersonalCodeException | InvalidLoanAmountException | InvalidLoanPeriodException e) {
            response.setErrorMessage(e.getMessage());
            return ResponseEntity.badRequest().body(response);

        } catch (NoValidLoanException e) {
            response.setErrorMessage(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);

        } catch (Exception e) {
            response.setErrorMessage(DecisionEngineConstants.AN_UNEXPECTED_ERROR_OCCURRED);
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
