package com.soccersignup.backend.service.imp;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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

import com.soccersignup.backend.model.Player;
import com.soccersignup.backend.repository.PlayerRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("PlayerServiceImpl Tests")
class PlayerServiceImplTest {

    @Mock
    private PlayerRepository playerRepository;

    @InjectMocks
    private PlayerServiceImpl playerService;

    private Player player1;
    private Player player2;
    private Player player3;

    @BeforeEach
    void setUp() {
        // Initialize test players
        player1 = new Player();
        player1.setName("John Doe");
        player1.setEmail("john@example.com");
        player1.setPhone("555-0001");

        player2 = new Player();
        player2.setName("Jane Smith");
        player2.setEmail("jane@example.com");
        player2.setPhone("555-0002");

        player3 = new Player();
        player3.setName("Bob Johnson");
        player3.setEmail("bob@example.com");
        player3.setPhone("555-0003");
    }

    // ========== Get All Players Tests ==========

    @Test
    @DisplayName("Should retrieve all players successfully")
    void testGetAllPlayers_Success() {
        // Arrange
        List<Player> players = List.of(player1, player2, player3);
        when(playerRepository.findAll()).thenReturn(players);

        // Act
        List<Player> result = playerService.getAllPlayers();

        // Assert
        assertEquals(3, result.size());
        assertEquals(player1.getName(), result.get(0).getName());
        assertEquals(player2.getName(), result.get(1).getName());
        assertEquals(player3.getName(), result.get(2).getName());
        verify(playerRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return empty list when no players exist")
    void testGetAllPlayers_EmptyList() {
        // Arrange
        when(playerRepository.findAll()).thenReturn(new ArrayList<>());

        // Act
        List<Player> result = playerService.getAllPlayers();

        // Assert
        assertTrue(result.isEmpty());
        assertEquals(0, result.size());
        verify(playerRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return single player in list")
    void testGetAllPlayers_SinglePlayer() {
        // Arrange
        List<Player> players = List.of(player1);
        when(playerRepository.findAll()).thenReturn(players);

        // Act
        List<Player> result = playerService.getAllPlayers();

        // Assert
        assertEquals(1, result.size());
        assertEquals(player1.getName(), result.get(0).getName());
        verify(playerRepository, times(1)).findAll();
    }

    // ========== Get Player By ID Tests ==========

    @Test
    @DisplayName("Should retrieve player by valid ID")
    void testGetPlayerById_Success() {
        // Arrange
        player1.setId(1L);
        when(playerRepository.findById(1L)).thenReturn(Optional.of(player1));

        // Act
        Optional<Player> result = playerService.getPlayerById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(player1.getId(), result.get().getId());
        assertEquals(player1.getName(), result.get().getName());
        assertEquals(player1.getEmail(), result.get().getEmail());
        verify(playerRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should return empty Optional when player ID not found")
    void testGetPlayerById_NotFound() {
        // Arrange
        when(playerRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<Player> result = playerService.getPlayerById(999L);

        // Assert
        assertTrue(result.isEmpty());
        assertFalse(result.isPresent());
        verify(playerRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Should handle null ID gracefully")
    void testGetPlayerById_NullId() {
        // Arrange
        when(playerRepository.findById(null)).thenReturn(Optional.empty());

        // Act
        Optional<Player> result = playerService.getPlayerById(null);

        // Assert
        assertTrue(result.isEmpty());
        verify(playerRepository, times(1)).findById(null);
    }

    @Test
    @DisplayName("Should retrieve correct player when multiple exist")
    void testGetPlayerById_CorrectPlayerAmongMany() {
        // Arrange
        player1.setId(1L);
        player2.setId(2L);
        player3.setId(3L);
        when(playerRepository.findById(2L)).thenReturn(Optional.of(player2));

        // Act
        Optional<Player> result = playerService.getPlayerById(2L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(player2.getId(), result.get().getId());
        assertEquals("Jane Smith", result.get().getName());
    }

    // ========== Save Player Tests ==========

    @Test
    @DisplayName("Should save new player successfully")
    void testSavePlayer_NewPlayer() {
        // Arrange
        Player newPlayer = new Player("Alice Brown", "alice@example.com", "555-0004");
        Player savedPlayer = new Player("Alice Brown", "alice@example.com", "555-0004");
        savedPlayer.setId(4L);

        when(playerRepository.save(newPlayer)).thenReturn(savedPlayer);

        // Act
        Player result = playerService.savePlayer(newPlayer);

        // Assert
        assertNotNull(result);
        assertEquals(4L, result.getId());
        assertEquals("Alice Brown", result.getName());
        assertEquals("alice@example.com", result.getEmail());
        assertEquals("555-0004", result.getPhone());
        verify(playerRepository, times(1)).save(newPlayer);
    }

    @Test
    @DisplayName("Should update existing player")
    void testSavePlayer_UpdateExisting() {
        // Arrange
        player1.setId(1L);
        player1.setName("John Doe Updated");
        player1.setEmail("john.updated@example.com");

        when(playerRepository.save(player1)).thenReturn(player1);

        // Act
        Player result = playerService.savePlayer(player1);

        // Assert
        assertEquals(1L, result.getId());
        assertEquals("John Doe Updated", result.getName());
        assertEquals("john.updated@example.com", result.getEmail());
        verify(playerRepository, times(1)).save(player1);
    }

    @Test
    @DisplayName("Should save player with all fields populated")
    void testSavePlayer_AllFieldsPopulated() {
        // Arrange
        Player completePlayer = new Player("Complete Player", "complete@example.com", "555-9999");
        completePlayer.setId(99L);

        when(playerRepository.save(completePlayer)).thenReturn(completePlayer);

        // Act
        Player result = playerService.savePlayer(completePlayer);

        // Assert
        assertNotNull(result);
        assertEquals(99L, result.getId());
        assertEquals("Complete Player", result.getName());
        assertEquals("complete@example.com", result.getEmail());
        assertEquals("555-9999", result.getPhone());
    }

    @Test
    @DisplayName("Should save player with minimal fields")
    void testSavePlayer_MinimalFields() {
        // Arrange
        Player minimalPlayer = new Player();
        minimalPlayer.setName("Minimal");
        minimalPlayer.setEmail("minimal@example.com");
        minimalPlayer.setPhone("555-0005");
        minimalPlayer.setId(5L);

        when(playerRepository.save(minimalPlayer)).thenReturn(minimalPlayer);

        // Act
        Player result = playerService.savePlayer(minimalPlayer);

        // Assert
        assertNotNull(result);
        assertEquals("Minimal", result.getName());
    }

    @Test
    @DisplayName("Should preserve player data when saving")
    void testSavePlayer_DataPreserved() {
        // Arrange
        Player originalPlayer = new Player("Original Name", "original@example.com", "555-0001");
        originalPlayer.setId(10L);

        when(playerRepository.save(originalPlayer)).thenReturn(originalPlayer);

        // Act
        Player result = playerService.savePlayer(originalPlayer);

        // Assert
        assertEquals(originalPlayer.getId(), result.getId());
        assertEquals(originalPlayer.getName(), result.getName());
        assertEquals(originalPlayer.getEmail(), result.getEmail());
        assertEquals(originalPlayer.getPhone(), result.getPhone());
    }

    // ========== Delete Player Tests ==========

    @Test
    @DisplayName("Should delete player by valid ID")
    void testDeletePlayer_Success() {
        // Arrange
        player1.setId(1L);

        // Act
        playerService.deletePlayer(1L);

        // Assert
        verify(playerRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Should handle delete of non-existent player")
    void testDeletePlayer_NonExistent() {
        // Arrange - No setup needed, delete doesn't throw exception

        // Act & Assert - Should not throw
        assertDoesNotThrow(() -> {
            playerService.deletePlayer(999L);
        });
        verify(playerRepository, times(1)).deleteById(999L);
    }

    @Test
    @DisplayName("Should delete correct player among multiple")
    void testDeletePlayer_CorrectPlayerDeleted() {
        // Arrange
        player2.setId(2L);

        // Act
        playerService.deletePlayer(2L);

        // Assert
        verify(playerRepository, times(1)).deleteById(2L);
        verify(playerRepository, never()).deleteById(1L);
        verify(playerRepository, never()).deleteById(3L);
    }

    @Test
    @DisplayName("Should handle multiple delete operations")
    void testDeletePlayer_MultipleDeletes() {
        // Arrange
        player1.setId(1L);
        player2.setId(2L);
        player3.setId(3L);

        // Act
        playerService.deletePlayer(1L);
        playerService.deletePlayer(2L);
        playerService.deletePlayer(3L);

        // Assert
        verify(playerRepository, times(1)).deleteById(1L);
        verify(playerRepository, times(1)).deleteById(2L);
        verify(playerRepository, times(1)).deleteById(3L);
        verify(playerRepository, times(3)).deleteById(anyLong());
    }

    // ========== Integration Scenario Tests ==========

    @Test
    @DisplayName("Should support full CRUD lifecycle")
    void testCRUDLifecycle() {
        // Arrange - Create
        Player newPlayer = new Player("CRUD Test", "crud@example.com", "555-1111");
        newPlayer.setId(100L);
        when(playerRepository.save(newPlayer)).thenReturn(newPlayer);

        // Act & Assert - Create
        Player savedPlayer = playerService.savePlayer(newPlayer);
        assertNotNull(savedPlayer);
        assertEquals(100L, savedPlayer.getId());

        // Arrange - Read
        when(playerRepository.findById(100L)).thenReturn(Optional.of(savedPlayer));

        // Act & Assert - Read
        Optional<Player> retrievedPlayer = playerService.getPlayerById(100L);
        assertTrue(retrievedPlayer.isPresent());
        assertEquals("CRUD Test", retrievedPlayer.get().getName());

        // Arrange - Update
        savedPlayer.setName("CRUD Test Updated");
        when(playerRepository.save(savedPlayer)).thenReturn(savedPlayer);

        // Act & Assert - Update
        Player updatedPlayer = playerService.savePlayer(savedPlayer);
        assertEquals("CRUD Test Updated", updatedPlayer.getName());

        // Act & Assert - Delete
        assertDoesNotThrow(() -> playerService.deletePlayer(100L));
        verify(playerRepository, times(1)).deleteById(100L);
    }

    @Test
    @DisplayName("Should retrieve all players after multiple operations")
    void testGetAllPlayers_AfterMultipleOperations() {
        // Arrange
        player1.setId(1L);
        player2.setId(2L);
        player3.setId(3L);
        List<Player> allPlayers = List.of(player1, player2, player3);

        when(playerRepository.findAll()).thenReturn(allPlayers);

        // Act
        List<Player> result = playerService.getAllPlayers();

        // Assert
        assertEquals(3, result.size());
        verify(playerRepository, times(1)).findAll();
    }
}