package com.soccersignup.backend.repository;

import java.util.List;
import java.util.Optional;

import com.soccersignup.backend.model.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.soccersignup.backend.model.GameSlot;
import com.soccersignup.backend.model.Player;

@Repository
public interface GameSlotRepository extends JpaRepository<GameSlot, Long> {
    List<GameSlot> findByGame(Game game);

    Optional<GameSlot> findByGameAndPlayer(Game game, Player player);
}
