package net.mossan.java.reversi.server;

import com.corundumstudio.socketio.ClientOperations;
import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOServer;
import net.mossan.java.reversi.common.jsonExchange.RoomDetail;
import net.mossan.java.reversi.common.jsonExchange.ServerState;
import org.json.JSONException;
import org.json.JSONObject;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ReversiServer {
    private final Map<UUID, ServerGameRoom> gameRoomMap = new HashMap<>();
    private final Map<UUID, ServerGameRoom> userMap = new HashMap<>();

    private SocketIOServer socket;

    @Option(name = "-h", aliases = {"--host"}, usage = "listen hostname")
    private String hostname = "localhost";

    @Option(name = "-p", aliases = {"--port"}, usage = "listen tcp port")
    private int port = 50000;

    private ReversiServer() {
    }

    public static void main(String[] args) {
        ReversiServer server = new ReversiServer();
        CmdLineParser parser = new CmdLineParser(server);
        try {
            parser.parseArgument(args);
            server.execute();
        } catch (CmdLineException ignore) {
            System.err.println("Usage:");
            System.err.println(" java -jar ReversiServer.jar [Options]");
            System.err.println();
            System.err.println("Options:");
            parser.printUsage(new OutputStreamWriter(System.err), null, o -> !o.option.isArgument());
        }
    }

    private void execute() {
        Configuration configuration = new Configuration();
        configuration.setHostname(this.hostname);
        configuration.setPort(this.port);
        this.socket = new SocketIOServer(configuration);

        this.socket.addConnectListener(client -> {
            if (!this.userMap.containsKey(client.getSessionId())) {
                this.userMap.put(client.getSessionId(), null);
                System.out.println(String.format("Connect: %s as %s", client.getRemoteAddress(), client.getSessionId()));
                try {
                    this.sendServerState(client);
                } catch (JSONException ignore) {
                }
            } else {
                System.out.println(String.format("ReConnect: %s as %s", client.getRemoteAddress(), client.getSessionId()));
            }
        });

        this.socket.addDisconnectListener(client -> {
            if (this.userMap.containsKey(client.getSessionId())) {
                System.out.println(String.format("Disconnect: %s as %s", client.getRemoteAddress(), client.getSessionId()));
            }
        });

        this.socket.addEventListener(
                "getServerState",
                JSONObject.class,
                (client, data, ackSender) -> this.sendServerState(client)
        );

        this.socket.addEventListener(
                "createRoom",
                JSONObject.class,
                (client, data, ackSender) -> {
                    this.createRoom();
                    this.sendServerState(this.socket.getBroadcastOperations());
                }
        );

        this.socket.start();
    }

    private void sendServerState(ClientOperations clientOperations) throws JSONException {
        Map<UUID, RoomDetail> roomDetailMap = new HashMap<>();
        for (Map.Entry<UUID, ServerGameRoom> entry : gameRoomMap.entrySet()) {
            ServerGameRoom r = entry.getValue();
            int seatedPlayerCount = r.getSeatedPlayerCount();
            boolean inGame = r.getInGame();
            RoomDetail detail = new RoomDetail(seatedPlayerCount, inGame);
            roomDetailMap.put(entry.getKey(), detail);
        }

        ServerState state = new ServerState(roomDetailMap);
        clientOperations.sendEvent("ServerState", state.toJSONObject().toString());
    }

    private void createRoom() {
        ServerGameRoom newRoom = new ServerGameRoom(8, this.socket);
        this.gameRoomMap.put(newRoom.uuid, newRoom);
    }
}
