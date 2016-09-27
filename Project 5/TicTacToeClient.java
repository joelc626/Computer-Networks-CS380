/* Joel Castro
 * CS380 - Project 5
 *
 * Driver: TicTacToeClient.java
 * Using Java’s Serializable interface to serialize application data and send it across a network.
 * Create a client program for playing Tic-tac-toe across a network.
 * Connect to a server and send serialized commands and messages to the server,
 * then receive serialized responses back.
 */

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TicTacToeClient implements Runnable {

    private static Socket socket;
    private static ObjectOutputStream toServer;
    private static ObjectInputStream fromServer;
    private static Scanner s;
    private static String opp;
    private static Message incomingMSG;
    private static MessageType msgType;
    private static ConnectMessage connM;
    private static CommandMessage commM;
    private static BoardMessage boardM;
    private static MoveMessage moveM;
    private static ErrorMessage errorM;
    private static StartGameMessage startM;
    private static PlayerListMessage playerListM;

    public static void main(String[] args) throws Exception {
        socket = new Socket("55.55.555.55", 11111);
        toServer = new ObjectOutputStream(socket.getOutputStream());
        fromServer = new ObjectInputStream(socket.getInputStream());
        s = new Scanner(System.in);

        //Send your player name
        System.out.println("Enter your user name");
        connM = new ConnectMessage(s.nextLine());
        toServer.writeObject(connM);

        //Send command to get list of players
        commM = new CommandMessage(CommandMessage.Command.LIST_PLAYERS);
        toServer.writeObject(commM);

        //Create a thread to read from the server
        new Thread(new TicTacToeClient()).start();

        //Start a game
        while (true) {
            opp = s.nextLine();
            if (opp.trim().isEmpty()) {
                startM = new StartGameMessage(null);
                toServer.writeObject(startM);
                break;
            } else {
                for (String name : playerListM.getPlayers()) {
                    if (name.equals(opp)) {
                        startM = new StartGameMessage(opp);
                        toServer.writeObject(startM);
                        break;
                    }
                }
                System.out.println("Not a valid player. Try again.");
            }
        }

        //Read move or command
        while (true) {
            String move = s.nextLine().toUpperCase().trim();
            switch (move) {
                case "EXIT":
                    commM = new CommandMessage(CommandMessage.Command.EXIT);
                    System.out.println("\nYou got to go?\nOK");
                    toServer.writeObject(commM);
                    break;
                case "SURRENDER":
                    commM = new CommandMessage(CommandMessage.Command.SURRENDER);
                    System.out.println("\nNEVER GIVE UP! NEVER SURRENDER!\nBut if you want to surrender... I guess :(");
                    toServer.writeObject(commM);
                    break;
                default:
                    Scanner tempS = new Scanner(move);
                    if (move.length() > 2 && move.contains(" ")) {
                        try {
                            byte row = tempS.nextByte();
                            byte col = tempS.nextByte();
                            moveM = new MoveMessage(row, col);
                            toServer.writeObject(moveM);
                        } catch (InputMismatchException e) {
                            System.out.println("Incorrect entry. Try again.");
                        }
                    } else {
                        System.out.println("Incorrect entry. Try again.");
                    }
                    break;
            }
        }
    }

    @Override
    public void run() {
        try {
            //Read the moment an object is sent to me and cast it
            while ((incomingMSG = (Message) fromServer.readObject()) != null) {
                msgType = incomingMSG.getType();
                switch (msgType) {
                    case BOARD:
                        boardM = (BoardMessage) incomingMSG;
                        if (boardM.getStatus() == BoardMessage.Status.IN_PROGRESS) {
                            printBoard(boardM.getBoard());
                            System.out.println("\nPlayer '1', if you give up enter 'EXIT' or 'SURRENDER'");
                            System.out.print("else enter your move (row[0-2] column[0-2]): ");
                        } else {
                            printBoard(boardM.getBoard());
                            System.out.println(boardM.getStatus());
                            System.exit(0);
                        }
                        break;
                    case ERROR:
                        errorM = (ErrorMessage) incomingMSG;
                        System.out.println("\n" + errorM.getError());
                        System.exit(0);
                        break;
                    case PLAYER_LIST:
                        playerListM = (PlayerListMessage) incomingMSG;
                        System.out.println("Player List:");
                        for (String name : playerListM.getPlayers()) {
                            System.out.println(name);
                        }
                        System.out.println("Enter name of your opponent or just "
                                + "enter an empty line to play against the server");
                        break;
                    default:
                        System.out.println("Houston We Have A Problem!");
                }
            }
        } catch (IOException | ClassNotFoundException ex) {
            System.out.println("ex: " + ex);
            Logger.getLogger(TicTacToeClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void printBoard(byte[][] b) {
        System.out.println();
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                System.out.print(b[row][col] + " ");
            }
            System.out.println();
        }
    }
}