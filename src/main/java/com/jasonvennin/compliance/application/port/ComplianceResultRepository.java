package com.jasonvennin.compliance.application.port;

import com.jasonvennin.compliance.compliance.domain.ComplianceResult;

public interface ComplianceResultRepository {

    ComplianceResult save(ComplianceResult result);
}
