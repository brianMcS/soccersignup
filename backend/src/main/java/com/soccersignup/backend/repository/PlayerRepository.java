package com.soccersignup.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.soccersignup.backend.model.Player;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {
}
