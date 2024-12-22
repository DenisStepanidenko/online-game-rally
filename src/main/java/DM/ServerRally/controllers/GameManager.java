package DM.ServerRally.controllers;

import DM.ServerRally.lobby.Lobby;
import DM.ServerRally.server.Server;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class GameManager {
    private final Logger logger = LoggerFactory.getLogger(GameManager.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private List<Lobby> lobbies;

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
    }

    public Optional<String> getLobbyAsJson() {
        try {
            return objectMapper.writeValueAsString(lobbies).describeConstable();
        } catch (JsonProcessingException e) {
            logger.error("Произошла ошибка при парсинге списка лобби " + e.getMessage());
            return Optional.empty();
        }
    }


}
