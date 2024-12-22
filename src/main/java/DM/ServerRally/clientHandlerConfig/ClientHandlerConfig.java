package DM.ServerRally.clientHandlerConfig;


import DM.ServerRally.executor.ClientHandler;
import DM.ServerRally.user.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class ClientHandlerConfig {

    @Bean
    @Scope(scopeName = "prototype")
    public ClientHandler getClientHandler(UserService userService) {
        return new ClientHandler(userService);
    }

}
