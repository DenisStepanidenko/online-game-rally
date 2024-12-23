package DM.ServerRally.controllers;

import DM.ServerRally.lobby.Lobby;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
@Getter
public class GameManager {
    private final Logger logger = LoggerFactory.getLogger(GameManager.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private List<Lobby> lobbies;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    /**
     * Инициализация изначальных лобби
     */
    public void initializeStartLobbies() {
        List<Lobby> lobbyList = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            lobbyList.add(new Lobby("Lobby " + i));
            logger.info("Создание Лобби " + i);
        }
        this.lobbies = lobbyList;


        // проверяет каждые 10 секунд наполненность лобби
        scheduler.scheduleAtFixedRate(this::checkLobbiesForGameStart, 0, 10, TimeUnit.SECONDS);
    }

    public Optional<String> getLobbyAsJson() {
        try {
            return objectMapper.writeValueAsString(lobbies).describeConstable();
        } catch (JsonProcessingException e) {
            logger.error("Произошла ошибка при парсинге списка лобби " + e.getMessage());
            return Optional.empty();
        }
    }


    public Lobby findById(Integer id) {
        return lobbies.stream().filter(lobby -> lobby.getId() == id).findFirst().get();
    }


    public void checkLobbiesForGameStart() {
        for (Lobby lobby : lobbies) {
            if (!lobby.isStartingGame() && lobby.isFull()) {
                lobby.startGame();
            }
        }
    }

    public void shutdown() {
        scheduler.shutdown();
    }


}
