/* Joel Castro
 * CS380 - Project 5
 *
 * Driver: TicTacToeClient.java
 * Using Java’s Serializable interface to serialize application data and send it across a network.
 * Create a client program for playing Tic-tac-toe across a network.
 * Connect to a server and send serialized commands and messages to the server,
 * then receive serialized responses back.
 */

public final class MoveMessage extends Message {

    private static final long serialVersionUID = 0L;

    private final byte row;
    private final byte col;

    public MoveMessage(byte row, byte col) {
        super(MessageType.MOVE);
        this.row = row;
        this.col = col;
    }

    public byte getRow() {
        return row;
    }

    public byte getCol() {
        return col;
    }
}
