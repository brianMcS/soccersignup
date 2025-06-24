package com.soccersignup.backend.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.soccersignup.backend.model.Game;

public interface GameRepository extends JpaRepository<Game, Long> {
    Optional<Game> findByGameDate(LocalDate gameDate);
}
