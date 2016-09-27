/* Joel Castro
 * CS380 - Project 3
 * 
 * Driver: Ipv4Client.java
 * Implementing IPv4.
 * Server will accept valid IPv4 packets, verify the checksum, and reply ”good” or ”bad”.
 */

package ipv4client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Random;

public class Ipv4Client {
    public static void main(String[] args) throws Exception {
        Socket socket = new Socket("55.55.555.55", 11111);
        OutputStream os = socket.getOutputStream();
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        
        int size = 65515; //65535 - 20
        
        for (int i = 0; i < 10; i++) {
            Random rand = new Random();

            byte[] data = new byte[rand.nextInt(size)];
            rand.nextBytes(data); //Generates random bytes
            Ipv4Packet packet = new Ipv4Packet(data);
            byte[] raw = packet.getRaw();

            for (int j = 0; j < 17; j += 4) {
                System.out.print(String.format("%8s", Integer.toBinaryString(raw[j] & 0xFF)).replace(' ', '0') + " ");
                System.out.print(String.format("%8s", Integer.toBinaryString(raw[j+1] & 0xFF)).replace(' ', '0') + " ");
                System.out.print(String.format("%8s", Integer.toBinaryString(raw[j+2] & 0xFF)).replace(' ', '0') + " ");
                System.out.println(String.format("%8s", Integer.toBinaryString(raw[j+3] & 0xFF)).replace(' ', '0'));
            }
            os.write(raw);
            System.out.println("Server> " + in.readLine());
        }
    }
}