package DM.ServerRally.executor;

import DM.ServerRally.server.Server;
import DM.ServerRally.user.model.User;
import DM.ServerRally.user.service.UserService;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Objects;
import java.util.Optional;


@Getter
@Setter
public class ClientHandler extends Thread {
    private final Logger logger = LoggerFactory.getLogger(ClientHandler.class);
    private Server server;
    private final UserService userService;
    private Socket clientSocket = new Socket();
    private BufferedReader input;
    private PrintWriter output;
    private String username;
    private String password;


    public ClientHandler(UserService userService) {
        this.userService = userService;

    }

    public boolean initializeSocket(Socket clientSocket) {
        this.clientSocket = clientSocket;
        try {
            input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            output = new PrintWriter(clientSocket.getOutputStream(), true);
            return true;
        } catch (IOException ex) {
            logger.error("Произошла ошибка при открытии потоков ввода/вывода " + ex.getMessage());
            return false;
        }
    }


    @Override
    public void run() {
        try {
            String message;
            while ((message = input.readLine()) != null) {
                logger.info("Получено от " + clientSocket.getInetAddress() + " сообщение " + message);

                // обработка сообщений
                if ("CONNECT".equals(message)) {
                    output.println("CONNECT_ACK");
                    logger.info("Отправлено сообщение CONNECT_ACK клиенту " + clientSocket.getInetAddress());
                } else if (message.startsWith("USER/")) {
                    String[] parts = message.split("/");
                    String username = parts[1];
                    Optional<User> optionalUser = userService.findUserByUsername(username);
                    this.username = username;

                    if (optionalUser.isPresent()) {
                        this.password = optionalUser.get().getPassword();
                        output.println("USER_ACK_CHECK");
                        logger.info("Отправлено сообщение USER_ACK_CHECK клиенту " + clientSocket.getInetAddress());
                    } else {
                        output.println("USER_ACK_CREATE");
                        logger.info("Отправлено сообщение USER_ACK_CREATE клиенту " + clientSocket.getInetAddress());
                    }

                } else if (message.startsWith("PASS/")) {
                    String[] parts = message.split("/");

                    String password = parts[1];

                    // нужно посмотреть, это новый пользователь, или же существующий
                    if (Objects.isNull(this.password)) {
                        this.password = password;
                        userService.saveUser(this.username, password);
                        output.println("PASS_ACK_SUCCESS");
                        logger.info("Отправлено сообщение PASS_ACK_SUCCESS клиенту " + clientSocket.getInetAddress());
                    } else {
                        if (this.password.equals(password)) {
                            output.println("PASS_ACK_SUCCESS");
                            logger.info("Отправлено сообщение PASS_ACK_SUCCESS клиенту " + clientSocket.getInetAddress());
                        } else {
                            output.println("PASS_ACK_FAIL");
                            logger.info("Отправлено сообщение PASS_ACK_FAIL клиенту " + clientSocket.getInetAddress());
                        }
                    }
                }


            }

        } catch (IOException e) {
            logger.error("Ошибка при обработке клиента: " + e.getMessage());
        } finally {
            try {
                // перед закрытием сокета удалим из списка игроков
                server.getPlayers().remove(this);
                logger.info("Клиент " + this.getClientSocket().getInetAddress() + " удалён из списка клиентов сервера");
                logger.info("Количество клиентов на сервере " + server.getPlayers().size());
                logger.info("Соединение с клиентом " + clientSocket.getInetAddress() + " закрыто");
                clientSocket.close();
                logger.info("Сокет с клиентом " + clientSocket.getInetAddress() + " успешно закрыт");
                this.interrupt();
            } catch (IOException e) {
                logger.error("Ошибка при закрытии сокета: " + e.getMessage());
            }
        }
    }


    /**
     * Нужно, чтобы удалять из players при закрытии соединения клиента
     * Пользователей сравниваем по clientSocket.getInetAddress()
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClientHandler that = (ClientHandler) o;
        return Objects.equals(clientSocket.getInetAddress(), that.clientSocket.getInetAddress());
    }

    @Override
    public int hashCode() {
        return Objects.hash(clientSocket.getInetAddress());
    }
}
