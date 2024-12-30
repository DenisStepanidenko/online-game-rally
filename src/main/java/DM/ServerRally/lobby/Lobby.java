package DM.ServerRally.lobby;

import DM.ServerRally.executor.ClientHandler;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
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
    @JsonIgnore
    private ScheduledExecutorService player1Timer;
    @JsonIgnore
    private ScheduledExecutorService player2Timer;
    @JsonIgnore
    private long startGame;


    @JsonProperty("player1")
    public String getPlayer1Name() {
        return !Objects.isNull(player1) ? player1.getUsername() : null;
    }

    @JsonProperty("player2")
    public String getPlayer2Name() {
        return !Objects.isNull(player2) ? player2.getUsername() : null;
    }

    /**
     * Игровое поле
     */
    @JsonIgnore
    private List<String> player1Field = new ArrayList<>();
    @JsonIgnore
    private List<String> player2Field = new ArrayList<>();

    /**
     * Позиция игроков
     */
    @JsonIgnore
    private int player1Position = 1;
    @JsonIgnore
    private int player2Position = 1;

    /**
     * Скорость игроков
     */
    @JsonIgnore
    private long player1Speed = 1000;
    @JsonIgnore
    private long player2Speed = 1000;

    /**
     * Время, когда скорость игрока вернётся к норме
     */
    @JsonIgnore
    private long player1SpeedResetTime = 0;
    @JsonIgnore
    private long player2SpeedResetTime = 0;

    /**
     * Время прохождения трассы игроками
     */
    @JsonIgnore
    private long player1FinishTime = 0;
    @JsonIgnore
    private long player2FinishTime = 0;
    @JsonIgnore
    private ScheduledExecutorService checkForDisconnect;


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
        }, 15, TimeUnit.SECONDS);

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
            player1.sendMessageToClient("START");
            player2.sendMessageToClient("START");
            startGame = System.currentTimeMillis();
            logger.info("Начало игры в лобби " + nameOfLobby + " равняется " + startGame);
            readyTimer.shutdown();
            startIndividualGameLoops();
        }


    }


    /**
     * Игровой цикл для каждого игрока
     */
    private void startIndividualGameLoops() {
        initialazeGameField();
        player1Timer = Executors.newSingleThreadScheduledExecutor();
        player2Timer = Executors.newSingleThreadScheduledExecutor();
        checkForDisconnect = Executors.newSingleThreadScheduledExecutor();


        logger.info("Запускаются игровые циклы для каждого игрока");
        player1Timer.scheduleAtFixedRate(() -> {
            if (isStartingGame) {
                updatePlayerField(player1, player1Field, player1Position);
                checkPlayerCollision(player1, player1Field, player1Position);
                checkPlayerFinish(player2, player1Field);
                logger.info("Отработал  player1Timer");
            }
        }, 0, player1Speed, TimeUnit.MILLISECONDS);

        player2Timer.scheduleAtFixedRate(() -> {
            if (isStartingGame) {
                updatePlayerField(player2, player2Field, player2Position);
                checkPlayerCollision(player2, player2Field, player2Position);
                checkPlayerFinish(player1, player2Field);
                logger.info("Отработал  player2Timer");
            }
        }, 0, player2Speed, TimeUnit.MILLISECONDS);

        checkForDisconnect.scheduleAtFixedRate(() -> {
            if (isStartingGame) {
                checkDisconnect();
            }
        }, 0, 3, TimeUnit.SECONDS);

    }

    private void checkDisconnect() {
        logger.info("Проверка на disconnect участников");
        if (countOfPlayersInLobby == 0) {
            resetLobby();
        }

    }


    private void checkPlayerCollision(ClientHandler player, List<String> field, int position) {
        if (!field.isEmpty()) {
            String currentRow = field.get(0);

            if (currentRow.charAt(position) == 'X') {
                if (player.equals(player1)) {
                    logger.info("Скорость игрока " + player1.getUsername() + " в лобби " + this.id + " замедлилась");
                    player1Speed = 2000;
                    player1SpeedResetTime = System.currentTimeMillis() + 5000;
                } else {
                    logger.info("Скорость игрока " + player2.getUsername() + " в лобби " + this.id + " замедлилась");
                    player2Speed = 2000;
                    player2SpeedResetTime = System.currentTimeMillis() + 5000;
                }

                player.sendMessageToClient("COLLISION");
            }
        }

        if (player.equals(player1)) {
            logger.info("Скорость игрока " + player1.getUsername() + " в лобби " + this.id + " восстановилась");
            if (System.currentTimeMillis() > player1SpeedResetTime) {
                player1Speed = 1000;
            }
        } else {
            logger.info("Скорость игрока " + player2.getUsername() + " в лобби " + this.id + " восстановилась");
            if (System.currentTimeMillis() > player2SpeedResetTime) {
                player2Speed = 1000;
            }
        }


    }

    private void checkPlayerFinish(ClientHandler player, List<String> field) {
        if (field.size() <= 1) {

            if (player.equals(player1)) {
                player1FinishTime = System.currentTimeMillis() - startGame;

                if (player2FinishTime == 0) {
                    if (Objects.nonNull(player1)) {
                        player1.sendMessageToClient("WIN/" + player1FinishTime + "/" + player2FinishTime);
                    }
                } else {
                    if (Objects.nonNull(player1)) {
                        player1.sendMessageToClient("LOSE/" + player1FinishTime + "/" + player2FinishTime);
                    }
                }

                // теперь его нужно выбросить из лобби
                this.setPlayer1(null);
                decrementCountOfPlayersInLobby();

                if (player1 != null) {
                    player1.setLobby(null);
                }

                player1Speed = 1000;
                player1Position = 1;
                player1SpeedResetTime = 0;


                if (player1Timer != null) {
                    player1Timer.shutdown();
                }

            } else {
                player2FinishTime = System.currentTimeMillis() - startGame;

                if (player1FinishTime == 0) {
                    if (Objects.nonNull(player2)) {
                        player2.sendMessageToClient("WIN/" + player2FinishTime + "/" + player1FinishTime);
                    }
                } else {
                    if (Objects.nonNull(player2)) {
                        player2.sendMessageToClient("LOSE/" + player2FinishTime + "/" + player1FinishTime);
                    }
                }


                // теперь его нужно выбросить из лобби
                this.setPlayer2(null);
                decrementCountOfPlayersInLobby();

                if (player2 != null) {
                    player2.setLobby(null);
                }

                player2Speed = 1000;
                player2Position = 1;
                player2SpeedResetTime = 0;


                if (player2Timer != null) {
                    player2Timer.shutdown();
                }
            }


        }
    }

    private void resetLobby() {
        logger.info("Происходит reset lobby с id = " + this.id);
        isStartingGame = false;
        player1Field.clear();
        player2Field.clear();
        player1Position = 1;
        player2Position = 1;
        player1Speed = 1000;
        player2Speed = 1000;
        player1SpeedResetTime = 0;
        player2SpeedResetTime = 0;
        player1FinishTime = 0;
        player2FinishTime = 0;
        countOfPlayersInLobby = 0;
        this.setPlayer1(null);
        this.setPlayer2(null);
        player1.setLobby(null);
        player2.setLobby(null);
        startGame = 0;

        if (player1Timer != null) {
            player1Timer.shutdown();
        }
        if (player2Timer != null) {
            player2Timer.shutdown();
        }

        if (checkForDisconnect != null) {
            checkForDisconnect.shutdown();
        }
    }

    private void updatePlayerField(ClientHandler player, List<String> field, int position) {
        logger.info("Происходит обновление поля для " + player.getUsername());
        if (!field.isEmpty()) {
            field.remove(0);
            field.add(generateRow());
        }

        player.sendMessageToClient("UPDATE_FIELD/" + String.join(";", field) + "/" + position);
    }

    public void movePlayerLeft(ClientHandler clientHandler) {
        if (clientHandler.equals(player1)) {
            player1Position = Math.max(0, player1Position - 1);
            logger.info("Позиция игрока " + player1.getUsername() + " " + player1Position);
        } else if (clientHandler.equals(player2)) {
            player2Position = Math.max(0, player2Position - 1);
            logger.info("Позиция игрока " + player2.getUsername() + " " + player2Position);
        }

    }

    public void movePlayerRight(ClientHandler clientHandler) {
        if (clientHandler.equals(player1)) {
            player1Position = Math.min(2, player1Position + 1);
            logger.info("Позиция игрока " + player1.getUsername() + " " + player1Position);
        } else if (clientHandler.equals(player2)) {
            player2Position = Math.min(2, player2Position + 1);
            logger.info("Позиция игрока " + player2.getUsername() + " " + player2Position);
        }
    }

    /**
     * Инициализация игрового поля
     */
    private void initialazeGameField() {
        logger.info("Инициализируются поля для игры в лобби " + this.id);
        for (int i = 0; i < 10; i++) {
            logger.info("Инициализация для i = " + i);
            String row = generateRow();
            player1Field.add(row);
            player2Field.add(row);
        }

    }

    private String generateRow() {

        StringBuilder row = new StringBuilder();

        // с вероятностью 30 процентов генерируем препятствие
        if (Math.random() < 0.3) {
            int randomNumber = (int) (Math.random() * 3) + 1;
            logger.info("Индекс - " + randomNumber);
            for (int i = 1; i <= 3; i++) {
                if (i == randomNumber) {
                    row.append("X");
                } else {
                    row.append(" ");
                }
            }
        }

        return row.toString();
    }

}
