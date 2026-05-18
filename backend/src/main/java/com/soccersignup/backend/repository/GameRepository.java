package com.soccersignup.backend.repository;

import java.time.LocalDate;
import java.util.Optional;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;

import com.soccersignup.backend.model.Game;
import org.springframework.data.jpa.repository.Lock;

public interface GameRepository extends JpaRepository<Game, Long> {
    Optional<Game> findByGameDate(LocalDate gameDate);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Game> findWithLockingById(Long id);
}
