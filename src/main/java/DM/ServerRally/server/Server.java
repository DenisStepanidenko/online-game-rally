package DM.ServerRally.server;

import DM.ServerRally.executor.ClientHandler;
import DM.ServerRally.serverConfig.ServerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


public class Server {
    private static final int PORT = ServerConfig.PORT.getPort();
    private static final Logger logger = LoggerFactory.getLogger(Server.class);
    private static final List<ClientHandler> players = new ArrayList<>();

    public static void main(String[] args) throws IOException {

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            logger.info("Сервер запущен на порту " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                logger.info("Присоединился клиент по адресу " + clientSocket.getInetAddress());

            }

        }
    }


}
