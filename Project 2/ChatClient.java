/* Joel Castro
 * CS380 - Project 2
 *
 * Implementing a simple chat client
 */
package chatclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;

public class ChatClient implements Runnable {
    private static Socket socket = null;
    private static PrintStream outs = null;
    private static BufferedReader inr = null;
    private static Scanner s = null;
    private static boolean closed = false;

    public static void main(String[] args) throws Exception {
        socket = new Socket("55.55.555.55", 11111);
        s = new Scanner(System.in);
        outs = new PrintStream(socket.getOutputStream());
        inr = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        
        //Create a thread to read from the server
        new Thread(new ChatClient()).start();
        while (!closed) {
            outs.println(s.nextLine());
        }
        
        //Close the output stream, close the input stream, close the socket
        outs.close();
        inr.close();
        socket.close();       
    }
    
    @Override
    public void run() {
        String serverLines;
        try {
            while ((serverLines = inr.readLine()) != null) {
                System.out.println(serverLines);
            }
            closed = true;
        } catch (IOException ex) {
            System.out.println("PROBLEM: " + ex);
        }
    }
}