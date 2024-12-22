package DM.ServerRally.lobby;

import DM.ServerRally.executor.ClientHandler;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
/**
 * Базовое лобби
 */
public class Lobby {
    @JsonIgnore
    private static int counterLobbies = 1;

    @JsonProperty("id")
    private int id;

    @JsonProperty("name_of_lobby")
    private String nameOfLobby;

    @JsonIgnore
    private ClientHandler player1;

    @JsonIgnore
    private ClientHandler player2;

    @JsonProperty("count_of_players")
    private int countOfPlayersInLobby = 0;

    public Lobby(String nameOfLobby) {
        this.nameOfLobby = nameOfLobby;
        id = counterLobbies;
        counterLobbies++;
    }

    @JsonProperty("player1")
    public String getPlayer1Name() {
        return !Objects.isNull(player1) ? player1.getName() : null;
    }

    @JsonProperty("player2")
    public String getPlayer2Name() {
        return !Objects.isNull(player2) ? player2.getName() : null;
    }


}
