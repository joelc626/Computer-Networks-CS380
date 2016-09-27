/* Joel Castro
 * CS380 - Project 5
 *
 * Driver: TicTacToeClient.java
 * Using Java’s Serializable interface to serialize application data and send it across a network.
 * Create a client program for playing Tic-tac-toe across a network.
 * Connect to a server and send serialized commands and messages to the server,
 * then receive serialized responses back.
 */

public final class StartGameMessage extends Message {

    private static final long serialVersionUID = 0L;

    private final String player2;

    public StartGameMessage(String player2) {
        super(MessageType.START_GAME);
        this.player2 = player2;
    }

    public String getPlayer2() {
        return player2;
    }
}
