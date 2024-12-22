package DM.ServerRally.server;

import DM.ServerRally.executor.ClientHandler;
import DM.ServerRally.serverConfig.ServerConfig;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

@Component
@Getter
public class Server {
    private final int PORT = ServerConfig.PORT.getPort();
    private final Logger logger = LoggerFactory.getLogger(Server.class);
    private final List<ClientHandler> players = new ArrayList<>();


    private final ApplicationContext context;

    @Autowired
    public Server(ApplicationContext context) {
        this.context = context;
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            logger.info("Сервер запущен на порту " + PORT);

            while (true) {

                try {
                    Socket clientSocket = serverSocket.accept();
                    logger.info("Присоединился клиент по адресу " + clientSocket.getInetAddress());

                    // добавляем нового игрока
                    ClientHandler clientHandler = context.getBean(ClientHandler.class);
                    boolean flag = clientHandler.initializeSocket(clientSocket);
                    if (flag) {
                        logger.info("Открытие сокетов ввода/вывода у клиента " + clientSocket.getInetAddress() + " произошло успешно.");
                        players.add(clientHandler);
                        logger.info("Клиент " + clientSocket.getInetAddress() + " добавлен в список участников сервера");
                        logger.info("Количество клиентов на сервере " + players.size());
                        clientHandler.setServer(this);
                        clientHandler.start();
                    } else {
                        try {
                            logger.info("Соединение с клиентом " + clientSocket.getInetAddress() + " закрыто");
                            clientSocket.close();
                        } catch (IOException ex) {
                            logger.error("Произошла ошибка при закрытии сокета " + ex.getMessage());
                        }
                    }
                } catch (IOException ex) {
                    logger.error("Произошла ошибка при подключении к клиенту " + ex.getMessage());
                }
            }
        } catch (IOException ex) {
            logger.error("Произошла ошибка при работе сервера " + ex.getMessage());
        }
    }
}



