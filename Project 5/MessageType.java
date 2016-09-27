/* Joel Castro
 * CS380 - Project 5
 *
 * Driver: TicTacToeClient.java
 * Using Java’s Serializable interface to serialize application data and send it across a network.
 * Create a client program for playing Tic-tac-toe across a network.
 * Connect to a server and send serialized commands and messages to the server,
 * then receive serialized responses back.
 */

public enum MessageType {

    CONNECT, COMMAND, BOARD, MOVE, ERROR, START_GAME, PLAYER_LIST;

    private static final long serialVersionUID = 0L;
}
