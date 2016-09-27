/* Joel Castro
 * CS380 - Project 5
 *
 * Driver: TicTacToeClient.java
 * Using Java’s Serializable interface to serialize application data and send it across a network.
 * Create a client program for playing Tic-tac-toe across a network.
 * Connect to a server and send serialized commands and messages to the server,
 * then receive serialized responses back.
 */

import java.io.Serializable;

public abstract class Message implements Serializable {

    private static final long serialVersionUID = 0L;

    private final MessageType type;

    public Message(MessageType type) {
        this.type = type;
    }

    public MessageType getType() {
        return type;
    }

    @Override
    public String toString() {
        return type.toString();
    }
}
