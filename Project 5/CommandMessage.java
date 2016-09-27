/* Joel Castro
 * CS380 - Project 5
 *
 * Driver: TicTacToeClient.java
 * Using Java’s Serializable interface to serialize application data and send it across a network.
 * Create a client program for playing Tic-tac-toe across a network.
 * Connect to a server and send serialized commands and messages to the server,
 * then receive serialized responses back.
 */

public final class CommandMessage extends Message {

    private static final long serialVersionUID = 0L;

    private final Command cmd;

    public CommandMessage(Command cmd) {
        super(MessageType.COMMAND);
        this.cmd = cmd;
    }

    public Command getCommand() {
        return cmd;
    }

    public static enum Command {

        LIST_PLAYERS, EXIT, SURRENDER;

        private static final long serialVersionUID = 0L;
    }
}
