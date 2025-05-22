package com.soccersignup.backend.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.soccersignup.backend.model.GameSlot;
import com.soccersignup.backend.model.Player;

@Repository
public interface GameSlotRepository extends JpaRepository<GameSlot, Long> {
    List<GameSlot> findByGameDate(LocalDate gameDate);

    Optional<GameSlot> findByPlayerAndGameDate(Player player, LocalDate gameDate);

    List<GameSlot> findByGameDateBetween(LocalDate start, LocalDate end);
}
