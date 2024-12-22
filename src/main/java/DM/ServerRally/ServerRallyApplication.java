package DM.ServerRally;

import DM.ServerRally.server.Server;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.io.IOException;

@SpringBootApplication
public class ServerRallyApplication {

	public static void main(String[] args) throws IOException {
		SpringApplication.run(ServerRallyApplication.class, args);
	}
	@Bean
	public CommandLineRunner startServer(Server server) {
		return args -> {
			server.start();
		};
	}

}
