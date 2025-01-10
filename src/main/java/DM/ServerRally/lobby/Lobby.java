package DM.ServerRally.lobby;

import DM.ServerRally.executor.ClientHandler;
import DM.ServerRally.state.GameState;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.Objects;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**
 * Базовое лобби
 */
@Getter
@Setter
public class Lobby {

    /**
     * Нужно, чтобы на фоне проверять количество игроков
     * Нужен на случай, если два игрока отключатся во время игры, тогда делаем reset lobby
     */
    @JsonIgnore
    private ScheduledExecutorService playerCheckTimer;
    @JsonIgnore
    private final ObjectMapper objectMapper = new ObjectMapper();
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

    @JsonIgnore
    private double finishPlayer1 = -1;
    @JsonIgnore
    private double finishPlayer2 = -1;
    @JsonIgnore
    private boolean isExitPlayer1 = false;
    @JsonIgnore
    private boolean isExitPlayer2 = false;


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
            logger.info("Клиент " + player2.getClientSocket().getInetAddress() + " не подтвердил готовность к игре в лобби " + nameOfLobby);
            if (!Objects.isNull(player1)) {
                player1.sendMessageToClient("LEFT_JOINED");
            }
            player2.setLobby(null);
            this.setPlayer2(null);
            decrementCountOfPlayersInLobby();
        } else {
            logger.info("Клиент " + player1.getClientSocket().getInetAddress() + " не подтвердил готовность к игре в лобби " + nameOfLobby);
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


        if (isExitPlayer1 || isExitPlayer2) {
            readyTimer.shutdown();
            // значит кто-то из игроков нажал выход
            if (isExitPlayer1 && isExitPlayer2) {
                logger.info("Клиент " + player1.getClientSocket().getInetAddress() + " не подтвердил готовность к игре в лобби " + nameOfLobby);
                logger.info("Клиент " + player2.getClientSocket().getInetAddress() + " не подтвердил готовность к игре в лобби " + nameOfLobby);
                player1.setLobby(null);
                player2.setLobby(null);
                this.setPlayer1(null);
                this.setPlayer2(null);
                decrementCountOfPlayersInLobby();
                decrementCountOfPlayersInLobby();
            } else if (isExitPlayer1) {
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
            isExitPlayer1 = false;
            isExitPlayer2 = false;
        } else if (readyPlayer1 && readyPlayer2) {
            logger.info("Оба игрока в лобби " + nameOfLobby + " подтвердили готовность. Игра начинается");
            readyTimer.shutdown();
            GameState startState = initializeGameField();
            try {
                String startStateJson = objectMapper.writeValueAsString(startState);
                player1.sendMessageToClient("START " + startStateJson);
                player2.sendMessageToClient("START " + startStateJson);

                // проверяем на фоне количество игроков
                playerCheckTimer = Executors.newSingleThreadScheduledExecutor();
                playerCheckTimer.scheduleAtFixedRate(this::checkPlayerCount, 0, 5, TimeUnit.SECONDS);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }


        }
    }

    private void checkPlayerCount() {
        if (countOfPlayersInLobby == 0) {
            this.setPlayer1(null);
            this.setPlayer2(null);
            finishPlayer1 = -1;
            finishPlayer2 = -1;
            readyPlayer1 = false;
            readyPlayer2 = false;
            isStartingGame = false;
            playerCheckTimer.shutdown();
        }
    }


    /**
     * Инициализация игрового поля
     */
    private GameState initializeGameField() {
        GameState gameState = new GameState();

        int[][] gameField = new int[50][38];


        for (int i = 0; i < 50; i++) {
            for (int j = 0; j < 38; j++) {
                gameField[i][j] = 0;
            }
        }

        // генерация препятствий
        Random random = new Random();
        int obstacleCount = (int) (50 * 38 * 0.2); // 20% клеток - препятствия
        for (int i = 0; i < obstacleCount; i++) {
            int x = random.nextInt(50);
            int y = random.nextInt(38);
            gameField[x][y] = 1;
        }

        gameState.setGameField(gameField);

        return gameState;
    }


    public synchronized void sendFinishTime(double finishTime, ClientHandler clientHandler) {
        clientHandler.updateTime(finishTime);

        if (clientHandler.equals(player1)) {

            finishPlayer1 = finishTime;
            if (finishPlayer2 == -1) {
                // это означает, что второй игрок ещё проезжает трассу
                player1.sendMessageToClient("WIN/" + finishTime + "/" + "NO");
                player1.updateWins();
            } else {
                if (finishPlayer1 > finishPlayer2) {
                    player1.sendMessageToClient("LOSE/" + finishTime + "/" + finishPlayer2);
                } else if (finishPlayer1 < finishPlayer2) {
                    player1.sendMessageToClient("WIN/" + finishTime + "/" + finishPlayer2);
                    player1.updateWins();
                } else {
                    player1.sendMessageToClient("DRAW/" + finishTime + "/" + finishPlayer2);
                }

                finishPlayer1 = -1;
                finishPlayer2 = -1;
                playerCheckTimer.shutdown();
                isStartingGame = false;
            }


            // теперь нужно кикнуть этого игрока из лобби
            player1.setLobby(null);
            this.setPlayer1(null);
            readyPlayer1 = false;
            decrementCountOfPlayersInLobby();

        } else {
            finishPlayer2 = finishTime;
            if (finishPlayer1 == -1) {
                // это означает, что первыый игрок ещё проезжает трассу
                player2.sendMessageToClient("WIN/" + finishTime + "/" + "NO");
                player2.updateWins();
            } else {
                if (finishPlayer2 > finishPlayer1) {
                    player2.sendMessageToClient("LOSE/" + finishTime + "/" + finishPlayer1);
                } else if (finishPlayer2 < finishPlayer1) {
                    player2.sendMessageToClient("WIN/" + finishTime + "/" + finishPlayer1);
                    player2.updateWins();
                } else {
                    player2.sendMessageToClient("DRAW/" + finishTime + "/" + finishPlayer1);
                }

                finishPlayer1 = -1;
                finishPlayer2 = -1;
                playerCheckTimer.shutdown();
                isStartingGame = false;
            }

            // теперь нужно кикнуть этого игрока из лобби
            player2.setLobby(null);
            this.setPlayer2(null);
            readyPlayer2 = false;
            decrementCountOfPlayersInLobby();
        }
    }

    /**
     * Метод для оповещения игрока, если он не подтвердил готовность к игре (принудительно нажав на кнопку на клиенте)
     *
     * @param clientHandler игрок
     */
    public void kickPlayer(ClientHandler clientHandler) {
        if (clientHandler.equals(player1)) {
            isExitPlayer1 = true;
        } else {
            isExitPlayer2 = true;
        }
    }
}
