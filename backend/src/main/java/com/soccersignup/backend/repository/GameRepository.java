package com.soccersignup.backend.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.soccersignup.backend.model.GameStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;

import com.soccersignup.backend.model.Game;
import org.springframework.data.jpa.repository.Lock;

public interface GameRepository extends JpaRepository<Game, Long> {
    Optional<Game> findByGameDate(LocalDate gameDate);
    List<Game> findByStatus(GameStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Game> findWithLockingById(Long id);
}
