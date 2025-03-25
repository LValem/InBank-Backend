package ee.taltech.inbankbackend.endpoint;

import com.fasterxml.jackson.databind.ObjectMapper;
import ee.taltech.inbankbackend.exceptions.*;
import ee.taltech.inbankbackend.service.Countries;
import ee.taltech.inbankbackend.service.Decision;
import ee.taltech.inbankbackend.service.DecisionEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import ee.taltech.inbankbackend.config.DecisionEngineConstants;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This class holds integration tests for the DecisionEngineController endpoint.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
@ExtendWith(MockitoExtension.class)
public class DecisionEngineControllerTest {

    private static final String PERSONAL_CODE_1234 = "1234";
    private static final String PERSONAL_CODE_34903254088 = "34903254088";
    private static final int LOAN_AMOUNT_1000 = 1000;
    private static final int LOAN_PERIOD_12 = 12;
    private static final int LOAN_PERIOD_10 = 10;
    private static final long LOAN_AMOUNT_10 = 10L;
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DecisionEngine decisionEngine;

    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        objectMapper = new ObjectMapper();
    }

    /**
     * This method tests the /loan/decision endpoint with valid inputs.
     */
    @Test
    public void givenValidRequest_whenRequestDecision_thenReturnsExpectedResponse() throws Exception {
        Decision decision = new Decision(LOAN_AMOUNT_1000, LOAN_PERIOD_12, null);
        when(decisionEngine.calculateApprovedLoan(anyString(), anyLong(), anyInt(), any(Countries.class)))
                .thenReturn(decision);

        DecisionRequest request = new DecisionRequest(PERSONAL_CODE_1234, LOAN_AMOUNT_10, LOAN_PERIOD_10,
                Countries.ESTONIA);

        MvcResult result = mockMvc.perform(post("/loan/decision").content(objectMapper
                .writeValueAsString(request)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.loanAmount").value(LOAN_AMOUNT_1000))
                .andExpect(jsonPath("$.loanPeriod").value(LOAN_PERIOD_12))
                .andExpect(jsonPath("$.errorMessage").isEmpty()).andReturn();

        DecisionResponse response = objectMapper.readValue(result.getResponse()
                .getContentAsString(), DecisionResponse.class);
        assert response.getLoanAmount() == LOAN_AMOUNT_1000;
        assert response.getLoanPeriod() == LOAN_PERIOD_12;
        assert response.getErrorMessage() == null;
    }

    /**
     * This test ensures that if an invalid personal code is provided, the controller returns
     * an HTTP Bad Request (400) response with the appropriate error message in the response body.
     */
    @Test
    public void givenInvalidAge_whenRequestDecision_thenReturnsBadRequest() throws Exception {
        when(decisionEngine.calculateApprovedLoan(anyString(), anyLong(), anyInt(), any(Countries.class)))
                .thenThrow(new InvalidAgeException(DecisionEngineConstants.INVALID_AGE_ERROR));

        DecisionRequest request = new DecisionRequest(PERSONAL_CODE_34903254088, LOAN_AMOUNT_10, LOAN_PERIOD_10,
                Countries.LATVIA);

        MvcResult result = mockMvc.perform(post("/loan/decision").content(objectMapper
                .writeValueAsString(request)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.loanAmount").isEmpty())
                .andExpect(jsonPath("$.loanPeriod").isEmpty())
                .andExpect(jsonPath("$.errorMessage")
                        .value(DecisionEngineConstants.INVALID_AGE_ERROR)).andReturn();

        DecisionResponse response = objectMapper.readValue(result.getResponse().getContentAsString(),
                DecisionResponse.class);
        assert response.getLoanAmount() == null;
        assert response.getLoanPeriod() == null;
        assert response.getErrorMessage().equals(DecisionEngineConstants.INVALID_AGE_ERROR);
    }

    /**
     * This test ensures that if an invalid Personal Code is provided, the controller returns
     * an HTTP Bad Request (400) response with the appropriate error message in the response body.
     */
    @Test
    public void givenInvalidPersonalCode_whenRequestDecision_thenReturnsBadRequest() throws Exception {
        when(decisionEngine.calculateApprovedLoan(anyString(), anyLong(), anyInt(), any(Countries.class)))
                .thenThrow(new InvalidPersonalCodeException(DecisionEngineConstants.INVALID_PERSONAL_ID_CODE));

        DecisionRequest request = new DecisionRequest(PERSONAL_CODE_1234, LOAN_AMOUNT_10, LOAN_PERIOD_10,
                Countries.LATVIA);

        MvcResult result = mockMvc.perform(post("/loan/decision").content(objectMapper
                        .writeValueAsString(request)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.loanAmount").isEmpty())
                .andExpect(jsonPath("$.loanPeriod").isEmpty())
                .andExpect(jsonPath("$.errorMessage")
                        .value(DecisionEngineConstants.INVALID_PERSONAL_ID_CODE)).andReturn();

        DecisionResponse response = objectMapper.readValue(result.getResponse().getContentAsString(),
                DecisionResponse.class);
        assert response.getLoanAmount() == null;
        assert response.getLoanPeriod() == null;
        assert response.getErrorMessage().equals(DecisionEngineConstants.INVALID_PERSONAL_ID_CODE);
    }

    /**
     * This test ensures that if an invalid loan amount is provided, the controller returns
     * an HTTP Bad Request (400) response with the appropriate error message in the response body.
     */
    @Test
    public void givenInvalidLoanAmount_whenRequestDecision_thenReturnsBadRequest() throws Exception {
        when(decisionEngine.calculateApprovedLoan(anyString(), anyLong(), anyInt(), any(Countries.class)))
                .thenThrow(new InvalidLoanAmountException(DecisionEngineConstants.INVALID_LOAN_AMOUNT));

        DecisionRequest request = new DecisionRequest(PERSONAL_CODE_1234, LOAN_AMOUNT_10, LOAN_PERIOD_10,
                Countries.LITHUANIA);

        MvcResult result = mockMvc.perform(post("/loan/decision").content(objectMapper
                .writeValueAsString(request)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.loanAmount").isEmpty())
                .andExpect(jsonPath("$.loanPeriod").isEmpty())
                .andExpect(jsonPath("$.errorMessage")
                        .value(DecisionEngineConstants.INVALID_LOAN_AMOUNT)).andReturn();

        DecisionResponse response = objectMapper.readValue(result.getResponse().getContentAsString(),
                DecisionResponse.class);
        assert response.getLoanAmount() == null;
        assert response.getLoanPeriod() == null;
        assert response.getErrorMessage().equals(DecisionEngineConstants.INVALID_LOAN_AMOUNT);
    }

    /**
     * This test ensures that if an invalid loan period is provided, the controller returns
     * an HTTP Bad Request (400) response with the appropriate error message in the response body.
     */
    @Test
    public void givenInvalidLoanPeriod_whenRequestDecision_thenReturnsBadRequest() throws Exception {
        when(decisionEngine.calculateApprovedLoan(anyString(), anyLong(), anyInt(), any(Countries.class)))
                .thenThrow(new InvalidLoanPeriodException(DecisionEngineConstants.INVALID_LOAN_PERIOD));

        DecisionRequest request = new DecisionRequest(PERSONAL_CODE_1234, LOAN_AMOUNT_10, LOAN_PERIOD_10,
                Countries.LITHUANIA);

        MvcResult result = mockMvc.perform(post("/loan/decision").content(objectMapper
                .writeValueAsString(request)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.loanAmount").isEmpty())
                .andExpect(jsonPath("$.loanPeriod").isEmpty())
                .andExpect(jsonPath("$.errorMessage")
                        .value(DecisionEngineConstants.INVALID_LOAN_PERIOD)).andReturn();

        DecisionResponse response = objectMapper.readValue(result.getResponse().getContentAsString(),
                DecisionResponse.class);
        assert response.getLoanAmount() == null;
        assert response.getLoanPeriod() == null;
        assert response.getErrorMessage().equals(DecisionEngineConstants.INVALID_LOAN_PERIOD);
    }

    /**
     * This test ensures that if no valid loan is found, the controller returns
     * an HTTP Bad Request (400) response with the appropriate error message in the response body.
     */
    @Test
    public void givenNoValidLoan_whenRequestDecision_thenReturnsBadRequest() throws Exception {
        when(decisionEngine.calculateApprovedLoan(anyString(), anyLong(), anyInt(), any(Countries.class)))
                .thenThrow(new NoValidLoanException(DecisionEngineConstants
                        .NO_VALID_LOAN_FOUND_FOR_THE_PROVIDED_PARAMETERS));

        DecisionRequest request = new DecisionRequest(PERSONAL_CODE_1234, (long) LOAN_AMOUNT_1000, LOAN_PERIOD_12,
                Countries.ESTONIA);

        MvcResult result = mockMvc.perform(post("/loan/decision").content(objectMapper
                .writeValueAsString(request)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.loanAmount").isEmpty())
                .andExpect(jsonPath("$.loanPeriod").isEmpty())
                .andExpect(jsonPath("$.errorMessage")
                        .value(DecisionEngineConstants
                                .NO_VALID_LOAN_FOUND_FOR_THE_PROVIDED_PARAMETERS)).andReturn();

        DecisionResponse response = objectMapper.readValue(result.getResponse().getContentAsString(),
                DecisionResponse.class);
        assert response.getLoanAmount() == null;
        assert response.getLoanPeriod() == null;
        assert response.getErrorMessage().equals(DecisionEngineConstants
                .NO_VALID_LOAN_FOUND_FOR_THE_PROVIDED_PARAMETERS);
    }

    /**
     * This test ensures that if an unexpected error occurs when processing the request, the controller returns
     * an HTTP Internal Server Error (500) response with the appropriate error message in the response body.
     */
    @Test
    public void givenUnexpectedError_whenRequestDecision_thenReturnsInternalServerError() throws Exception {
        when(decisionEngine.calculateApprovedLoan(anyString(), anyLong(), anyInt(), any(Countries.class)))
                .thenThrow(new RuntimeException());

        DecisionRequest request = new DecisionRequest(PERSONAL_CODE_1234, LOAN_AMOUNT_10, LOAN_PERIOD_10,
                Countries.LATVIA);

        MvcResult result = mockMvc.perform(post("/loan/decision").content(objectMapper
                .writeValueAsString(request)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.loanAmount").isEmpty())
                .andExpect(jsonPath("$.loanPeriod").isEmpty())
                .andExpect(jsonPath("$.errorMessage")
                        .value(DecisionEngineConstants.AN_UNEXPECTED_ERROR_OCCURRED)).andReturn();

        DecisionResponse response = objectMapper.readValue(result.getResponse().getContentAsString(),
                DecisionResponse.class);
        assert response.getLoanAmount() == null;
        assert response.getLoanPeriod() == null;
        assert response.getErrorMessage().equals(DecisionEngineConstants.AN_UNEXPECTED_ERROR_OCCURRED);
    }
}
