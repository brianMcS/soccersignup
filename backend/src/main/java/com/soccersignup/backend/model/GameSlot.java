package com.soccersignup.backend.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "game_slots", uniqueConstraints = @UniqueConstraint(columnNames =  {"game_id", "player_id"}))
public class GameSlot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @Enumerated(EnumType.STRING)
    private SlotStatus status = SlotStatus.CONFIRMED;

    @Column(nullable = false)
    private LocalDateTime signedUpAt;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            columnDefinition = "varchar(255) default 'UNPAID'"
    )
    private PaymentStatus paymentStatus = PaymentStatus.UNPAID;

    @Column(
            nullable = false,
            precision = 10,
            scale = 2,
            columnDefinition = "numeric(10,2) default 5.00"
    )
    private BigDecimal feeAmount;

    private LocalDateTime paidAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "confirmed_by_id")
    private Player confirmedBy;

    private LocalDateTime confirmedAt;

    public GameSlot() {
    }

    public static GameSlot create(
            Game game,
            Player player,
            SlotStatus status,
            LocalDateTime signedUpAt
    ) {
        GameSlot slot = new GameSlot();
        slot.game = game;
        slot.player = player;
        slot.status = status;
        slot.signedUpAt = signedUpAt;
        slot.paymentStatus = PaymentStatus.UNPAID;
        slot.feeAmount = game.getFeeAmount();

        return slot;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public SlotStatus getStatus() {
        return status;
    }

    public void setStatus(SlotStatus status) {
        this.status = status;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getSignedUpAt() {
        return signedUpAt;
    }

    public void setSignedUpAt(LocalDateTime signedUpAt) {
        this.signedUpAt = signedUpAt;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public BigDecimal getFeeAmount() {
        return feeAmount;
    }

    public void setFeeAmount(BigDecimal feeAmount) {
        this.feeAmount = feeAmount;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }

    public Player getConfirmedBy() {
        return confirmedBy;
    }

    public void setConfirmedBy(Player confirmedBy) {
        this.confirmedBy = confirmedBy;
    }

    public LocalDateTime getConfirmedAt() {
        return confirmedAt;
    }

    public void setConfirmedAt(LocalDateTime confirmedAt) {
        this.confirmedAt = confirmedAt;
    }
}
