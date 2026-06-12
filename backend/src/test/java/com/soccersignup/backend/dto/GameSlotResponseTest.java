package com.soccersignup.backend.dto;

import com.soccersignup.backend.model.Game;
import com.soccersignup.backend.model.GameSlot;
import com.soccersignup.backend.model.PaymentStatus;
import com.soccersignup.backend.model.Player;
import com.soccersignup.backend.model.PlayerRole;
import com.soccersignup.backend.model.SlotStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class GameSlotResponseTest {

    @Test
    void hidesAnotherPlayersEmailAndPaymentDetails() {
        Player signedUpPlayer = player(1L, "Alex", "alex@example.com");
        Player viewer = player(2L, "Jordan", "jordan@example.com");
        GameSlot slot = slot(signedUpPlayer);

        GameSlotResponse response = GameSlotResponse.forViewer(slot, viewer);

        assertThat(response.playerId()).isEqualTo(1L);
        assertThat(response.playerName()).isEqualTo("Alex");
        assertThat(response.playerEmail()).isNull();
        assertThat(response.paymentStatus()).isNull();
        assertThat(response.feeAmount()).isNull();
    }

    @Test
    void includesPrivateDetailsForOwnerAndOrganiser() {
        Player signedUpPlayer = player(1L, "Alex", "alex@example.com");
        GameSlot slot = slot(signedUpPlayer);

        GameSlotResponse ownerResponse =
                GameSlotResponse.forViewer(slot, signedUpPlayer);

        Player organiser = player(2L, "Jordan", "jordan@example.com");
        organiser.addRole(PlayerRole.ORGANISER);
        GameSlotResponse organiserResponse =
                GameSlotResponse.forViewer(slot, organiser);

        assertThat(ownerResponse.playerEmail()).isEqualTo("alex@example.com");
        assertThat(ownerResponse.paymentStatus()).isEqualTo(PaymentStatus.UNPAID);
        assertThat(organiserResponse.playerEmail()).isEqualTo("alex@example.com");
        assertThat(organiserResponse.paymentStatus()).isEqualTo(PaymentStatus.UNPAID);
    }

    private GameSlot slot(Player player) {
        Game game = new Game();
        game.setId(1L);
        game.setFeeAmount(new BigDecimal("5.00"));
        return GameSlot.create(
                game,
                player,
                SlotStatus.CONFIRMED,
                LocalDateTime.now());
    }

    private Player player(Long id, String name, String email) {
        Player player = new Player(name, email, null);
        player.setId(id);
        player.addRole(PlayerRole.PLAYER);
        return player;
    }
}
