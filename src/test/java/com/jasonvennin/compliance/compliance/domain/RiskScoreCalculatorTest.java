package com.jasonvennin.compliance.compliance.domain;

import com.jasonvennin.compliance.rule.domain.RuleViolation;
import com.jasonvennin.compliance.rule.domain.Severity;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RiskScoreCalculatorTest {

    private final RiskScoreCalculator calculator =
            new RiskScoreCalculator();

    @Test
    void shouldReturnZeroWhenThereAreNoViolations() {
        RiskScore result = calculator.calculate(List.of());

        assertEquals(0, result.value());
    }

    @Test
    void shouldReturnTenForLowViolation() {
        RiskScore result = calculator.calculate(
                List.of(violation(Severity.LOW))
        );

        assertEquals(10, result.value());
    }

    @Test
    void shouldReturnTwentyFiveForMediumViolation() {
        RiskScore result = calculator.calculate(
                List.of(violation(Severity.MEDIUM))
        );

        assertEquals(25, result.value());
    }

    @Test
    void shouldReturnFiftyForHighViolation() {
        RiskScore result = calculator.calculate(
                List.of(violation(Severity.HIGH))
        );

        assertEquals(50, result.value());
    }

    @Test
    void shouldReturnOneHundredForCriticalViolation() {
        RiskScore result = calculator.calculate(
                List.of(violation(Severity.CRITICAL))
        );

        assertEquals(100, result.value());
    }

    @Test
    void shouldSumMultipleViolations() {
        RiskScore result = calculator.calculate(
                List.of(
                        violation(Severity.LOW),
                        violation(Severity.MEDIUM),
                        violation(Severity.HIGH)
                )
        );

        assertEquals(85, result.value());
    }

    @Test
    void shouldCapScoreAtOneHundred() {
        RiskScore result = calculator.calculate(
                List.of(
                        violation(Severity.CRITICAL),
                        violation(Severity.HIGH)
                )
        );

        assertEquals(100, result.value());
    }

    private RuleViolation violation(Severity severity) {
        return new RuleViolation(
                "TEST_RULE",
                severity,
                "Test violation"
        );
    }
}
