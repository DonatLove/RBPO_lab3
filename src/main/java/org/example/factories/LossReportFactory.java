package org.example.factories;

import org.example.annotation.Cumponent;
import org.example.models.LossReport;
import org.example.models.User;

@Cumponent
public class LossReportFactory {
    public LossReport create(
            User applicant,
            String description,
            String category,
            String status) {
        LossReport lossReport = new LossReport();
        lossReport.setApplicant(applicant);
        lossReport.setDescription(description);
        lossReport.setCategory(category);
        lossReport.setStatus(status);
        return lossReport;
    }
}
