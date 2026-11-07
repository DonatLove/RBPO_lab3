package org.example.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;

import java.time.LocalDateTime;

@Entity
@Table(name = "handover_records", uniqueConstraints = @UniqueConstraint(
        name = "uk_handover_records_claim", columnNames = "item_claim_id"))
public class HandoverRecord implements Identifiable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "item_claim_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_handover_records_claim"))
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private ItemClaim itemClaim;

    @Column(name = "handover_time", nullable = false)
    private LocalDateTime handoverTime;

    @Column(nullable = false)
    private boolean confirmed;

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

    public ItemClaim getItemClaim() {
        return itemClaim;
    }

    public void setItemClaim(ItemClaim itemClaim) {
        this.itemClaim = itemClaim;
    }

    public LocalDateTime getHandoverTime() {
        return handoverTime;
    }

    public void setHandoverTime(LocalDateTime handoverTime) {
        this.handoverTime = handoverTime;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }
}
