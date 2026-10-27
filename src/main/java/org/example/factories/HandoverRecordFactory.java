package org.example.factories;

import org.example.annotation.Cumponent;
import org.example.models.HandoverRecord;
import org.example.models.ItemClaim;

@Cumponent
public class HandoverRecordFactory {
    public HandoverRecord create(ItemClaim itemClaim, boolean confirmed) {
        HandoverRecord handoverRecord = new HandoverRecord();
        handoverRecord.setItemClaim(itemClaim);
        handoverRecord.setConfirmed(confirmed);
        return handoverRecord;
    }
}
