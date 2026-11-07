package org.example.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;

@Entity
@Table(name = "item_claims", uniqueConstraints = @UniqueConstraint(
        name = "uk_item_claims_report_item", columnNames = {"loss_report_id", "found_item_id"}))
public class ItemClaim implements Identifiable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "loss_report_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_item_claims_loss_report"))
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private LossReport lossReport;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "found_item_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_item_claims_found_item"))
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private FoundItem foundItem;

    @Column(nullable = false, length = 1000)
    private String evidence;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ItemClaimStatus status = ItemClaimStatus.PENDING;

    @Version
    @JsonIgnore
    private Long version;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public LossReport getLossReport() {
        return lossReport;
    }

    public void setLossReport(LossReport lossReport) {
        this.lossReport = lossReport;
    }

    public FoundItem getFoundItem() {
        return foundItem;
    }

    public void setFoundItem(FoundItem foundItem) {
        this.foundItem = foundItem;
    }

    public String getEvidence() {
        return evidence;
    }

    public void setEvidence(String evidence) {
        this.evidence = evidence;
    }

    public ItemClaimStatus getStatus() {
        return status;
    }

    public void setStatus(ItemClaimStatus status) {
        this.status = status;
    }

    public void setStatus(String status) {
        this.status = ItemClaimStatus.valueOf(status);
    }
}
