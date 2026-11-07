package org.example.factories;

import org.example.annotation.Cumponent;
import org.example.models.FoundItem;
import org.example.models.ItemClaim;
import org.example.models.LossReport;

@Cumponent
public class ItemClaimFactory {
    public ItemClaim create(
            LossReport lossReport,
            FoundItem foundItem,
            String evidence,
            String status) {
        ItemClaim itemClaim = new ItemClaim();
        itemClaim.setLossReport(lossReport);
        itemClaim.setFoundItem(foundItem);
        itemClaim.setEvidence(evidence);
        itemClaim.setStatus(status);
        return itemClaim;
    }
}
