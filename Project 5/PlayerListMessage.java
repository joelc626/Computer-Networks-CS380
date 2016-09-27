/* Joel Castro
 * CS380 - Project 5
 *
 * Driver: TicTacToeClient.java
 * Using Java’s Serializable interface to serialize application data and send it across a network.
 * Create a client program for playing Tic-tac-toe across a network.
 * Connect to a server and send serialized commands and messages to the server,
 * then receive serialized responses back.
 */

public final class PlayerListMessage extends Message {

    private static final long serialVersionUID = 0L;

    private final String[] players;

    public PlayerListMessage(String[] players) {
        super(MessageType.PLAYER_LIST);
        this.players = players;
    }

    public String[] getPlayers() {
        return players;
    }
}
