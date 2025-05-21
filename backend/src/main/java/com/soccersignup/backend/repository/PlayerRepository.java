package com.soccersignup.backend.repository;
import org.springframework.data.jpa.repository.JpaRepository;

import com.soccersignup.backend.model.Player;


public interface PlayerRepository extends JpaRepository<Player, Long> {
}
