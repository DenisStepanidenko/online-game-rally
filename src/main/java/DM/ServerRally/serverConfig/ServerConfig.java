package DM.ServerRally.serverConfig;

public enum ServerConfig {
    PORT(8082);

    private final int port;

    ServerConfig(int port) {
        this.port = port;
    }

    public int getPort() {
        return port;
    }

}
