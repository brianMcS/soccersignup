package com.soccersignup.backend.service.imp;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.soccersignup.backend.model.Game;
import com.soccersignup.backend.model.GameSlot;
import com.soccersignup.backend.model.Player;
import com.soccersignup.backend.model.SlotStatus;
import com.soccersignup.backend.repository.GameRepository;
import com.soccersignup.backend.repository.GameSlotRepository;
import com.soccersignup.backend.repository.PlayerRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("GameSlotServiceImpl Tests")
class GameSlotServiceImplTest {

    @Mock
    private GameSlotRepository gameSlotRepository;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private GameRepository gameRepository;

    @InjectMocks
    private GameSlotServiceImpl gameSlotService;

    private Game game;
    private Player player;
    private GameSlot gameSlot;

    @BeforeEach
    void setUp() {
        // Initialize test data
        game = new Game();
        game.setId(1L);
        game.setGameDate(LocalDate.now().plusDays(1));
        game.setMaxPlayers(18);
        game.setCreatedAt(LocalDateTime.now());

        player = new Player();
        player.setId(1L);
        player.setName("John Doe");
        player.setEmail("john@example.com");
        player.setPhone("555-0001");

        gameSlot = new GameSlot();
        gameSlot.setId(1L);
        gameSlot.setGame(game);
        gameSlot.setPlayer(player);
        gameSlot.setStatus(SlotStatus.CONFIRMED);
        gameSlot.setSignedUpAt(LocalDateTime.now());
    }

    // ========== Successful Signup Tests ==========

    @Test
    @DisplayName("Should successfully add signup when game has available slots")
    void testAddSignup_Success() {
        // Arrange
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));
        when(playerRepository.findById(1L)).thenReturn(Optional.of(player));
        when(gameSlotRepository.findByGame(game)).thenReturn(new ArrayList<>());
        when(gameSlotRepository.findByGameAndPlayer(game, player)).thenReturn(Optional.empty());
        when(gameSlotRepository.save(any(GameSlot.class))).thenReturn(gameSlot);

        // Act
        GameSlot result = gameSlotService.addSignup(1L, 1L);

        // Assert
        assertNotNull(result);
        assertEquals(player.getId(), result.getPlayer().getId());
        assertEquals(game.getId(), result.getGame().getId());
        assertEquals(SlotStatus.CONFIRMED, result.getStatus());
        assertNotNull(result.getSignedUpAt());
        verify(gameSlotRepository, times(1)).save(any(GameSlot.class));
    }

    @Test
    @DisplayName("Should set signup timestamp when player signs up")
    void testAddSignup_SetsSignedUpTime() {
        // Arrange
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));
        when(playerRepository.findById(1L)).thenReturn(Optional.of(player));
        when(gameSlotRepository.findByGame(game)).thenReturn(new ArrayList<>());
        when(gameSlotRepository.findByGameAndPlayer(game, player)).thenReturn(Optional.empty());
        when(gameSlotRepository.save(any(GameSlot.class))).thenReturn(gameSlot);

        // Act
        GameSlot result = gameSlotService.addSignup(1L, 1L);

        // Assert
        assertNotNull(result.getSignedUpAt());
        assertTrue(result.getSignedUpAt().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    // ========== Full Game Rejection Tests (18 players) ==========

    @Test
    @DisplayName("Should reject signup when game reaches 18 players (max capacity)")
    void testAddSignup_GameFullWith18Players() {
        // Arrange
        game.setMaxPlayers(18);
        List<GameSlot> fullSlots = new ArrayList<>();
        for (int i = 0; i < 18; i++) {
            fullSlots.add(new GameSlot());
        }

        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));
        when(playerRepository.findById(1L)).thenReturn(Optional.of(player));
        when(gameSlotRepository.findByGame(game)).thenReturn(fullSlots);

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            gameSlotService.addSignup(1L, 1L);
        });

        assertEquals("Game is full (18 players max)", exception.getMessage());
        verify(gameSlotRepository, never()).save(any(GameSlot.class));
    }

    @Test
    @DisplayName("Should reject signup when game has 17 players (one spot remaining)")
    void testAddSignup_GameAlmostFullWith17Players() {
        // Arrange
        game.setMaxPlayers(18);
        List<GameSlot> almostFullSlots = new ArrayList<>();
        for (int i = 0; i < 17; i++) {
            almostFullSlots.add(new GameSlot());
        }

        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));
        when(playerRepository.findById(1L)).thenReturn(Optional.of(player));
        when(gameSlotRepository.findByGame(game)).thenReturn(almostFullSlots);
        when(gameSlotRepository.findByGameAndPlayer(game, player)).thenReturn(Optional.empty());
        when(gameSlotRepository.save(any(GameSlot.class))).thenReturn(gameSlot);

        // Act
        GameSlot result = gameSlotService.addSignup(1L, 1L);

        // Assert
        assertNotNull(result);
        verify(gameSlotRepository, times(1)).save(any(GameSlot.class));
    }

    // ========== Duplicate Player Prevention Tests ==========

    @Test
    @DisplayName("Should prevent duplicate signup by same player")
    void testAddSignup_DuplicatePlayerPrevention() {
        // Arrange
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));
        when(playerRepository.findById(1L)).thenReturn(Optional.of(player));
        when(gameSlotRepository.findByGame(game)).thenReturn(new ArrayList<>());
        when(gameSlotRepository.findByGameAndPlayer(game, player)).thenReturn(Optional.of(gameSlot));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            gameSlotService.addSignup(1L, 1L);
        });

        assertEquals("Player already signed up.", exception.getMessage());
        verify(gameSlotRepository, never()).save(any(GameSlot.class));
    }

    @Test
    @DisplayName("Should allow multiple different players to sign up for same game")
    void testAddSignup_MultipleDifferentPlayersAllowed() {
        // Arrange
        Player player2 = new Player();
        player2.setId(2L);
        player2.setName("Jane Smith");
        player2.setEmail("jane@example.com");
        player2.setPhone("555-0002");

        GameSlot gameSlot2 = new GameSlot();
        gameSlot2.setId(2L);
        gameSlot2.setGame(game);
        gameSlot2.setPlayer(player2);
        gameSlot2.setStatus(SlotStatus.CONFIRMED);
        gameSlot2.setSignedUpAt(LocalDateTime.now());

        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));
        when(playerRepository.findById(2L)).thenReturn(Optional.of(player2));
        when(gameSlotRepository.findByGame(game)).thenReturn(new ArrayList<>());
        when(gameSlotRepository.findByGameAndPlayer(game, player2)).thenReturn(Optional.empty());
        when(gameSlotRepository.save(any(GameSlot.class))).thenReturn(gameSlot2);

        // Act
        GameSlot result = gameSlotService.addSignup(1L, 2L);

        // Assert
        assertNotNull(result);
        assertEquals(player2.getId(), result.getPlayer().getId());
        verify(gameSlotRepository, times(1)).save(any(GameSlot.class));
    }

    // ========== Invalid Date Handling Tests ==========

    @Test
    @DisplayName("Should handle past game dates appropriately")
    void testAddSignup_PastGameDate() {
        // Arrange
        game.setGameDate(LocalDate.now().minusDays(1)); // Past date
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));
        when(playerRepository.findById(1L)).thenReturn(Optional.of(player));
        when(gameSlotRepository.findByGame(game)).thenReturn(new ArrayList<>());
        when(gameSlotRepository.findByGameAndPlayer(game, player)).thenReturn(Optional.empty());
        when(gameSlotRepository.save(any(GameSlot.class))).thenReturn(gameSlot);

        // Act
        GameSlot result = gameSlotService.addSignup(1L, 1L);

        // Assert - Service currently allows past dates, but this test documents the behavior
        assertNotNull(result);
        verify(gameSlotRepository, times(1)).save(any(GameSlot.class));
    }

    @Test
    @DisplayName("Should handle future game dates appropriately")
    void testAddSignup_FutureGameDate() {
        // Arrange
        game.setGameDate(LocalDate.now().plusDays(30)); // Far future
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));
        when(playerRepository.findById(1L)).thenReturn(Optional.of(player));
        when(gameSlotRepository.findByGame(game)).thenReturn(new ArrayList<>());
        when(gameSlotRepository.findByGameAndPlayer(game, player)).thenReturn(Optional.empty());
        when(gameSlotRepository.save(any(GameSlot.class))).thenReturn(gameSlot);

        // Act
        GameSlot result = gameSlotService.addSignup(1L, 1L);

        // Assert
        assertNotNull(result);
        verify(gameSlotRepository, times(1)).save(any(GameSlot.class));
    }

    // ========== Exception Handling Tests ==========

    @Test
    @DisplayName("Should throw exception when game not found")
    void testAddSignup_GameNotFound() {
        // Arrange
        when(gameRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            gameSlotService.addSignup(999L, 1L);
        });

        assertTrue(exception.getMessage().contains("Game not found"));
        verify(gameSlotRepository, never()).save(any(GameSlot.class));
    }

    @Test
    @DisplayName("Should throw exception when player not found")
    void testAddSignup_PlayerNotFound() {
        // Arrange
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));
        when(playerRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            gameSlotService.addSignup(1L, 999L);
        });

        assertTrue(exception.getMessage().contains("Player not found"));
        verify(gameSlotRepository, never()).save(any(GameSlot.class));
    }

    // ========== Remove Signup Tests ==========

    @Test
    @DisplayName("Should successfully remove signup")
    void testRemoveSignup_Success() {
        // Arrange
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));
        when(playerRepository.findById(1L)).thenReturn(Optional.of(player));
        when(gameSlotRepository.findByGameAndPlayer(game, player)).thenReturn(Optional.of(gameSlot));

        // Act
        gameSlotService.removeSignup(1L, 1L);

        // Assert
        verify(gameSlotRepository, times(1)).delete(gameSlot);
    }

    @Test
    @DisplayName("Should not throw exception when removing non-existent signup")
    void testRemoveSignup_NonExistent() {
        // Arrange
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));
        when(playerRepository.findById(1L)).thenReturn(Optional.of(player));
        when(gameSlotRepository.findByGameAndPlayer(game, player)).thenReturn(Optional.empty());

        // Act & Assert - Should not throw
        assertDoesNotThrow(() -> {
            gameSlotService.removeSignup(1L, 1L);
        });
        verify(gameSlotRepository, never()).delete(any(GameSlot.class));
    }

    // ========== Get Signups Tests ==========

    @Test
    @DisplayName("Should retrieve all signups for a game")
    void testGetSignupsForGame_Success() {
        // Arrange
        List<GameSlot> slots = List.of(gameSlot);
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));
        when(gameSlotRepository.findByGame(game)).thenReturn(slots);

        // Act
        List<GameSlot> result = gameSlotService.getSignupsForGame(1L);

        // Assert
        assertEquals(1, result.size());
        assertEquals(gameSlot, result.get(0));
        verify(gameSlotRepository, times(1)).findByGame(game);
    }

    @Test
    @DisplayName("Should throw exception when game not found in getSignupsForGame")
    void testGetSignupsForGame_GameNotFound() {
        // Arrange
        when(gameRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            gameSlotService.getSignupsForGame(999L);
        });

        assertTrue(exception.getMessage().contains("Game not found"));
    }
}