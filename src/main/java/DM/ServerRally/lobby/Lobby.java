package DM.ServerRally.lobby;

import DM.ServerRally.executor.ClientHandler;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**
 * Базовое лобби
 */
@Getter
@Setter
public class Lobby {
    @JsonIgnore
    private final Logger logger = LoggerFactory.getLogger(Lobby.class);
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

    @JsonProperty("starting_game")
    private boolean isStartingGame = false;

    @JsonIgnore
    private boolean readyPlayer1 = false;
    @JsonIgnore
    private boolean readyPlayer2 = false;

    public Lobby(String nameOfLobby) {
        this.nameOfLobby = nameOfLobby;
        id = counterLobbies;
        counterLobbies++;
    }

    @JsonIgnore
    private ScheduledExecutorService readyTimer;

    @JsonProperty("player1")
    public String getPlayer1Name() {
        return !Objects.isNull(player1) ? player1.getUsername() : null;
    }

    @JsonProperty("player2")
    public String getPlayer2Name() {
        return !Objects.isNull(player2) ? player2.getUsername() : null;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        Lobby lobby = (Lobby) object;
        return id == lobby.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }


    @JsonIgnore
    public synchronized boolean incrementCountOfPlayersInLobby() {
        if (this.countOfPlayersInLobby == 2) {
            return false;
        } else {
            this.countOfPlayersInLobby++;
            return true;
        }
    }

    @JsonIgnore
    public synchronized void setPlayer(ClientHandler clientHandler) {
        if (Objects.isNull(player1)) {
            this.player1 = clientHandler;
        } else {
            player2 = clientHandler;
        }
    }

    @JsonIgnore
    public void decrementCountOfPlayersInLobby() {
        if (countOfPlayersInLobby != 0) {
            this.countOfPlayersInLobby--;
        }
    }

    @JsonIgnore
    public boolean isFull() {
        return this.countOfPlayersInLobby == 2;
    }


    public void startGame() {
        isStartingGame = true;
        logger.info("Игра в лобби под id=" + id + " перешла в режим подтверждения игроков");

        // сначала нужно уведомить игроков о том, что в лобби к ним зашли и ожидается готовность игроков
        // на этом моменте player1 и player2 не null
        player1.sendMessageToClient("PLAYER_JOINED/" + player2.getUsername());
        player2.sendMessageToClient("PLAYER_JOINED/" + player1.getUsername());

        readyTimer = Executors.newSingleThreadScheduledExecutor();

        // раз в секунду проверяем готовность игроков
        readyTimer.scheduleAtFixedRate(this::checkReadyStatus, 0, 1, TimeUnit.SECONDS);

        // если в течение 30 секунд кто-то не подтвердил, то нужно вернуть лобби в режим ожидания
        readyTimer.schedule(() -> {
            if (!readyPlayer1 || !readyPlayer2) {
                kickUnreadyPlayers();
                readyTimer.shutdown();
            }
        }, 30, TimeUnit.SECONDS);

    }

    /**
     * Если кто-то не подтвердил готовность к игре, то нужно вернуть лобби в состояние ожидания
     */
    private void kickUnreadyPlayers() {

        // мы зашли в этот метод, если хотя бы один игрок не подтвердил готовность играть

        if (!readyPlayer1 && !readyPlayer2) {
            logger.info("Клиент " + player1.getClientSocket().getInetAddress() + " не подтвердил готовность к игре в лобби " + nameOfLobby);
            logger.info("Клиент " + player2.getClientSocket().getInetAddress() + " не подтвердил готовность к игре в лобби " + nameOfLobby);
            player1.setLobby(null);
            player2.setLobby(null);
            this.setPlayer1(null);
            this.setPlayer2(null);
            decrementCountOfPlayersInLobby();
            decrementCountOfPlayersInLobby();
        } else if (!readyPlayer2) {

            if (!Objects.isNull(player1)) {
                player1.sendMessageToClient("LEFT_JOINED");
            }
            player2.setLobby(null);
            this.setPlayer2(null);
            decrementCountOfPlayersInLobby();
        } else {
            if (!Objects.isNull(player2)) {
                player2.sendMessageToClient("LEFT_JOINED");
            }
            player1.setLobby(null);
            this.setPlayer1(null);
            decrementCountOfPlayersInLobby();
        }


        logger.info("Лобби " + nameOfLobby + " вернулось в режим ожидания игроков");
        isStartingGame = false;
        readyPlayer1 = false;
        readyPlayer2 = false;

    }

    private void checkReadyStatus() {
        logger.info("Ожидается подтверждение игроков в лобби " + nameOfLobby);

        if (readyPlayer1 && readyPlayer2) {
            logger.info("Оба игрока в лобби " + nameOfLobby + " подтвердили готовность. Игра начинается");
            readyTimer.shutdown();
            initialazeGameField();
            startGameLoop();
        }
    }

    /**
     * Игровой цикл
     */
    private void startGameLoop() {
    }

    /**
     * Инициализация игрового поля
     */
    private void initialazeGameField() {
    }


    public void handlePlayerDisconnect(ClientHandler clientHandler) {

    }
}
