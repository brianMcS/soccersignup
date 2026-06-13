package com.soccersignup.backend.repository;


import com.soccersignup.backend.model.Game;
import com.soccersignup.backend.model.TeamSheet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TeamSheetRepository extends JpaRepository<TeamSheet, Long> {
     Optional<TeamSheet> findByGame(Game game);
}
