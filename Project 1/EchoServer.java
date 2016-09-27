/* Joel Castro
 * CS380 - Project 1
 *
 * Implementing an echo server and client
 */
package echoserver;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import javax.net.ServerSocketFactory;

public final class EchoServer {

    public static void main(String[] args) throws Exception {
        ServerSocket serverSocket = ServerSocketFactory.getDefault().createServerSocket(11111);
        
        //repeatedly wait for connections, and process
        while (true) {
            try (Socket socket = serverSocket.accept()) {
                System.out.println("Client connected: " + socket.getInetAddress());
                
                //info going to client
                PrintStream out = new PrintStream(socket.getOutputStream());
                //info coming from client
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                
                String s;

                //while connected wait for input
                try {
                    while ((s = in.readLine()) != null) {
                        out.println(s);
                    }
                } catch (Exception e) {
                }
                out.close();
                in.close();
                /*  USEFUL NOTE:
                    The try with resources will close the resource created between
                    the () so you don't need to close it at the end of the block */
            }
        }
    }
}