package com.soccersignup.backend.service.impl;

import com.soccersignup.backend.model.Game;
import com.soccersignup.backend.model.GameSlot;
import com.soccersignup.backend.model.Player;
import com.soccersignup.backend.model.PlayerRole;
import com.soccersignup.backend.model.SlotStatus;
import com.soccersignup.backend.repository.GameRepository;
import com.soccersignup.backend.repository.GameSlotRepository;
import com.soccersignup.backend.repository.PlayerRepository;
import com.soccersignup.backend.service.GameSlotService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class GameSlotServiceConcurrencyIntegrationTest {

    @Autowired
    private GameSlotService gameSlotService;
    @Autowired
    private GameRepository gameRepository;
    @Autowired
    private GameSlotRepository gameSlotRepository;
    @Autowired
    private PlayerRepository playerRepository;

    private Game game;
    private Player firstConfirmed;
    private Player secondConfirmed;
    private Player firstWaitlisted;
    private Player secondWaitlisted;

    @BeforeEach
    void setUp() {
        gameSlotRepository.deleteAll();
        gameRepository.deleteAll();
        playerRepository.deleteAll();

        game = new Game();
        game.setGameDate(LocalDate.now().plusDays(1));
        game.setKickOffTime(LocalTime.of(19, 0));
        game.setLocation("Dublin");
        game.setMaxPlayers(2);
        game = gameRepository.saveAndFlush(game);

        firstConfirmed = savePlayer("confirmed-one@example.com");
        secondConfirmed = savePlayer("confirmed-two@example.com");
        firstWaitlisted = savePlayer("waitlisted-one@example.com");
        secondWaitlisted = savePlayer("waitlisted-two@example.com");

        LocalDateTime signedUpAt = LocalDateTime.now().minusMinutes(4);
        gameSlotRepository.saveAllAndFlush(List.of(
                GameSlot.create(game, firstConfirmed, SlotStatus.CONFIRMED, signedUpAt),
                GameSlot.create(game, secondConfirmed, SlotStatus.CONFIRMED, signedUpAt.plusMinutes(1)),
                GameSlot.create(game, firstWaitlisted, SlotStatus.WAITLISTED, signedUpAt.plusMinutes(2)),
                GameSlot.create(game, secondWaitlisted, SlotStatus.WAITLISTED, signedUpAt.plusMinutes(3))));
    }

    @Test
    void simultaneousConfirmedDeparturesPromoteDifferentWaitlistedPlayers()
            throws Exception {
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            Future<?> firstRemoval = executor.submit(() -> {
                await(start);
                gameSlotService.removeSignup(game.getId(), firstConfirmed.getId());
            });
            Future<?> secondRemoval = executor.submit(() -> {
                await(start);
                gameSlotService.removeSignup(game.getId(), secondConfirmed.getId());
            });

            start.countDown();
            firstRemoval.get();
            secondRemoval.get();
        } finally {
            executor.shutdownNow();
        }

        List<GameSlot> remainingSlots = gameSlotRepository.findByGame(
                gameRepository.findById(game.getId()).orElseThrow());

        assertThat(remainingSlots).hasSize(2);
        assertThat(remainingSlots)
                .allMatch(slot -> slot.getStatus() == SlotStatus.CONFIRMED);
        assertThat(remainingSlots)
                .extracting(slot -> slot.getPlayer().getId())
                .containsExactlyInAnyOrder(
                        firstWaitlisted.getId(),
                        secondWaitlisted.getId());
    }

    private Player savePlayer(String email) {
        Player player = new Player(email, email, null);
        player.addRole(PlayerRole.PLAYER);
        return playerRepository.saveAndFlush(player);
    }

    private void await(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while starting concurrent removal", e);
        }
    }
}
