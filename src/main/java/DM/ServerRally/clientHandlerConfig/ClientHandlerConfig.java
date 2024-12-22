package DM.ServerRally.clientHandlerConfig;


import DM.ServerRally.controllers.GameManager;
import DM.ServerRally.executor.ClientHandler;
import DM.ServerRally.user.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class ClientHandlerConfig {

    @Bean
    @Scope(scopeName = "prototype")
    public ClientHandler getClientHandler(UserService userService, GameManager gameManager) {
        return new ClientHandler(userService, gameManager);
    }

}
