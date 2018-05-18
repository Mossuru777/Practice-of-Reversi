package net.mossan.java.reversi.client;

import io.socket.client.IO;
import io.socket.client.Socket;
import net.mossan.java.reversi.client.boarddrawer.BoardDrawerType;
import net.mossan.java.reversi.common.jsonExchange.RoomDetail;
import net.mossan.java.reversi.common.jsonExchange.ServerState;
import org.json.JSONException;
import org.json.JSONObject;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;
import java.util.function.Supplier;

public class ReversiClient {
    private final Supplier<Scanner> scannerSupplier;

    private Socket socket;
    private ServerState serverState;
    private boolean initialized = false;

    @Argument(metaVar = "server-url", usage = "connect server url", required = true)
    private URI url;

    @Option(name = "-d", aliases = {"--drawer"}, usage = "board drawer type")
    private BoardDrawerType boardDrawerType = BoardDrawerType.GUI;

    private ReversiClient(Supplier<Scanner> scannerSupplier) {
        this.scannerSupplier = scannerSupplier;
    }

    public static void main(String[] args) {
        Supplier<Scanner> scannerSupplier = () -> new Scanner(new BufferedReader(new InputStreamReader(System.in)));
        ReversiClient client = new ReversiClient(scannerSupplier);
        CmdLineParser parser = new CmdLineParser(client);
        try {
            parser.parseArgument(args);
            client.execute();
        } catch (CmdLineException e) {
            System.err.println("Usage:");
            System.err.println(" java -jar ReversiClient.jar [Options] server-url");
            System.err.println();
            System.err.println("Arguments:");
            parser.printUsage(new OutputStreamWriter(System.err), null, o -> o.option.isArgument());
            System.err.println();
            System.err.println("Options:");
            parser.printUsage(new OutputStreamWriter(System.err), null, o -> !o.option.isArgument());
        }
    }

    private void execute() {
        assert this.socket == null : "Multiple socket can't create.";
        this.socket = IO.socket(this.url);
        this.socket
                .on(Socket.EVENT_CONNECT, args -> System.out.println("*** Connected. ***"))
                .on("ServerState", args -> {
                    try {
                        JSONObject obj = new JSONObject((String) args[0]);
                        this.serverState = new ServerState(obj);
                        if (!this.initialized) {
                            this.initialized = true;
                            new Thread(this::displayMenu).start();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        this.disconnect();
                        System.exit(1);
                    }
                });

        Runtime.getRuntime().addShutdownHook(new Thread(this::disconnect));
        this.socket.open();
    }

    private void displayMenu() {
        boolean loop = true;
        while (loop) {
            System.out.println("*** Select Action ***");
            System.out.println("1. Display Room List");
            System.out.println("2. Create Room");
            System.out.println("3. Enter Room");
            System.out.println("9. Disconnect");
            System.out.println();
            System.out.print("> ");

            Scanner scanner = this.scannerSupplier.get();
            final int action = scanner.nextInt();
            scanner.nextLine();

            System.out.println();
            switch (action) {
                case 1:
                    this.displayRoomList(true);
                    break;
                case 2:
                    this.createRoom();
                    break;
                case 3:
                    this.enterRoom();
                    break;
                case 9:
                    loop = false;
                    this.disconnect();
                    break;
                default:
                    break;
            }
        }
        System.exit(0);
    }

    private String[] displayRoomList(boolean pause) {
        System.out.println("[Room List]");
        System.out.println("------------------------------------------------------------------");
        System.out.println("| No |               NameSpace               | Players | In Game |");

        String[] nameSpaceArray = new String[this.serverState.roomDetailMap.size()];
        int i = 0;
        for (Map.Entry<UUID, RoomDetail> entry : this.serverState.roomDetailMap.entrySet()) {
            final String nameSpace = entry.getKey().toString();
            final int players = entry.getValue().seatedPlayerCount;
            final boolean inGame = entry.getValue().inGame;
            System.out.println("|----|---------------------------------------|---------|---------|");
            System.out.println(String.format("| %2d | /%36s |    %d    |  %-5b  |", i + 1, nameSpace, players, inGame));
            nameSpaceArray[i] = nameSpace;
            ++i;
        }

        System.out.println("------------------------------------------------------------------");

        if (pause) {
            System.out.println("Press enter to continue...");
            Scanner scanner = this.scannerSupplier.get();
            scanner.nextLine();
        } else {
            System.out.println();
        }
        return nameSpaceArray;
    }

    private void createRoom() {
        this.socket.emit("createRoom");
    }

    private void enterRoom() {
        Scanner scanner = this.scannerSupplier.get();
        String selectNameSpace = null;
        while (selectNameSpace == null) {
            System.out.println("*** Select Room Number. (-1 to return menu.) ***");
            String[] nameSpaceArray = this.displayRoomList(false);
            System.out.print("> ");

            int select = scanner.nextInt();
            scanner.nextLine();
            if (select >= 1 && select <= nameSpaceArray.length) {
                selectNameSpace = nameSpaceArray[select - 1];
            } else if (select == -1) {
                return;
            }
        }

        final Socket nameSpaceSocket = IO.socket(this.url.resolve(String.format("/%s", selectNameSpace)));
        ClientGameRoom.executeAndWait(nameSpaceSocket, boardDrawerType, this.scannerSupplier);
    }

    private void disconnect() {
        if (this.socket != null) {
            this.socket.off();
            this.socket.close();
            this.socket = null;
            System.out.println("*** Disconnected. ***");
        }
    }
}
