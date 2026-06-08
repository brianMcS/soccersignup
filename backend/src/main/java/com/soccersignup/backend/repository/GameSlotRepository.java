package com.soccersignup.backend.repository;

import java.util.List;
import java.util.Optional;

import com.soccersignup.backend.model.Game;
import com.soccersignup.backend.model.SlotStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.soccersignup.backend.model.GameSlot;
import com.soccersignup.backend.model.Player;

@Repository
public interface GameSlotRepository extends JpaRepository<GameSlot, Long> {
    List<GameSlot> findByGame(Game game);

    List<GameSlot> findByGameAndStatus(Game game, SlotStatus status);

    Optional<GameSlot> findByGameAndPlayer(Game game, Player player);

    long countByGameAndStatus(Game game, SlotStatus status);

    Optional<GameSlot> findFirstByGameAndStatusOrderBySignedUpAtAsc(Game game, SlotStatus status);

    boolean existsByGameAndPlayer(Game game, Player player);

    boolean existsByGameAndPlayerAndStatus(Game game, Player player, SlotStatus status);
}
