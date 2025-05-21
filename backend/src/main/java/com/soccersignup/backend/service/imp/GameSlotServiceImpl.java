package com.soccersignup.backend.service.imp;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.soccersignup.backend.model.GameSlot;
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
    public GameSlot addSignup(GameSlot slot) {
        // prevent double signup
        gameSlotRepository.findByUserIdAndGameDate(slot.getUserId(), slot.getGameDate())
                .ifPresent(existing -> {
                    throw new RuntimeException("User already signed up for this week");
                });

        slot.setTimestamp(LocalDateTime.now());
        return gameSlotRepository.save(slot);
    }

    @Override
    public void removeSignup(Long userId, LocalDate gameDate) {
        gameSlotRepository.findByUserIdAndGameDate(userId, gameDate)
                .ifPresent(gameSlotRepository::delete);
    }
}
