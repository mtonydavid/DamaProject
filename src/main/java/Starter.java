import client.ChessBoardClient;
import server.Server;

import java.io.IOException;

public class Starter {
    public static void main(String[] args) {
        if(args.length < 1) {
            System.out.println("Usage: java Starter -s (for server) or -c (for client)");
            return;
        }
        switch (args[0]){
            case "-s":
                System.out.printf("Starting server");
                try {
                    Server.main(args);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                break;
            case "-c":
                System.out.printf("Starting client");
                ChessBoardClient.main(args);
                break;
            default:
                System.out.printf("Invalid argument");
        }
    }
}
