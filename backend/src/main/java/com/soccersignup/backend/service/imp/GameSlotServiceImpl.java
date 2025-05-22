package com.soccersignup.backend.service.imp;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.soccersignup.backend.model.GameSlot;
import com.soccersignup.backend.model.Player;
import com.soccersignup.backend.repository.GameSlotRepository;
import com.soccersignup.backend.service.GameSlotService;

@Service
public class GameSlotServiceImpl implements GameSlotService {

    private final GameSlotRepository gameSlotRepository;

    public GameSlotServiceImpl(GameSlotRepository gameSlotRepository) {
        this.gameSlotRepository = gameSlotRepository;
    }

    @Override
    public List<GameSlot> getSignupsForWeek(LocalDate date) {
        LocalDate friday = date.with(DayOfWeek.FRIDAY);
        LocalDate thursday = date.with(DayOfWeek.THURSDAY);
        return gameSlotRepository.findByGameDateBetween(friday, thursday);
    }

    @Override
    public GameSlot addSignup(GameSlot gameSlot) {
        LocalDate date = gameSlot.getGameDate();
        List<GameSlot> currentSignups = gameSlotRepository.findByGameDate(date);

        if (currentSignups.size() >= 18) {
            throw new IllegalStateException("Maximum number of players (18) already signed up for this date.");
        }

        // Prevent duplicate signup by same player (optional)
        boolean alreadySignedUp = currentSignups.stream()
                .anyMatch(slot -> slot.getPlayer().getId().equals(gameSlot.getPlayer().getId()));

        if (alreadySignedUp) {
            throw new IllegalStateException("Player already signed up for this date.");
        }

        gameSlot.setTimestamp(LocalDateTime.now());
        return gameSlotRepository.save(gameSlot);
    }

    @Override
    public void removeSignup(Player player, LocalDate gameDate) {
        gameSlotRepository.findByPlayerAndGameDate(player, gameDate)
                .ifPresent(gameSlotRepository::delete);
    }
}
