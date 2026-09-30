package com.jasonvennin.compliance.infrastructure.configuration;

import com.jasonvennin.compliance.application.port.ComplianceResultRepository;
import com.jasonvennin.compliance.application.port.CustomerRepository;
import com.jasonvennin.compliance.application.port.TransactionRepository;
import com.jasonvennin.compliance.application.usecase.EvaluateTransactionUseCase;
import com.jasonvennin.compliance.compliance.domain.ComplianceEngine;
import com.jasonvennin.compliance.compliance.domain.RiskScoreCalculator;
import com.jasonvennin.compliance.rule.domain.ComplianceRule;
import com.jasonvennin.compliance.rule.domain.rules.HighAmountRule;
import com.jasonvennin.compliance.rule.domain.rules.HighRiskCustomerRule;
import com.jasonvennin.compliance.rule.domain.rules.RestrictedCountryRule;
import com.jasonvennin.compliance.rule.domain.rules.VelocityRule;
import com.jasonvennin.compliance.transaction.domain.TransactionHistory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Set;

@Configuration
public class ComplianceConfiguration {

    @Bean
    public HighAmountRule highAmountRule() {
        return new HighAmountRule();
    }

    @Bean
    public HighRiskCustomerRule highRiskCustomerRule() {
        return new HighRiskCustomerRule();
    }

    @Bean
    public RestrictedCountryRule restrictedCountryRule() {
        return new RestrictedCountryRule(
                Set.of(
                        "IR",
                        "KP",
                        "SY"
                )
        );
    }

    @Bean
    public VelocityRule velocityRule(
            TransactionHistory transactionHistory
    ) {
        return new VelocityRule(transactionHistory);
    }

    @Bean
    public RiskScoreCalculator riskScoreCalculator() {
        return new RiskScoreCalculator();
    }

    @Bean
    public ComplianceEngine complianceEngine(
            List<ComplianceRule> rules,
            RiskScoreCalculator riskScoreCalculator
    ) {
        return new ComplianceEngine(
                rules,
                riskScoreCalculator
        );
    }

    @Bean
    public EvaluateTransactionUseCase evaluateTransactionUseCase(
            TransactionRepository transactionRepository,
            CustomerRepository customerRepository,
            ComplianceResultRepository complianceResultRepository,
            ComplianceEngine complianceEngine
    ) {
        return new EvaluateTransactionUseCase(
                transactionRepository,
                customerRepository,
                complianceResultRepository,
                complianceEngine
        );
    }
}
