/* Joel Castro
 * CS380 - Project 3.5
 *
 * Implementing IPv6.
 * Server will accept valid IPv4 packets, verify the checksum, and reply ”good” or ”bad”.
 */
package ipv6client;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Random;

public class Ipv6Client {
    byte version; //Always 6
    byte trafiicClass; //Don’t implement
    byte flowLabel; //Don’t implement
    short payloadLength; //Just data
    byte nextHeader; //Set to UDP protocol value = 17
    byte hopLimit; //20
    byte[] sourceAddress; //My IP address 71.93.222.15
    byte[] destinationAddress; //55.55.555.55
    static byte[] data;

    public static void main(String[] args) throws Exception {
        Socket socket = new Socket("55.55.555.55", 11111);
        OutputStream os = socket.getOutputStream();
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));


        for (int i = 0; i < 10; i++) {
            Random rand = new Random();

            data = new byte[rand.nextInt(65515)]; //65535 - 20
            rand.nextBytes(data); //Generates random bytes
            Ipv6Client packet = new Ipv6Client(data);
            byte[] raw = packet.getRaw();

            for (int j = 0; j < 33; j += 8) {
                System.out.print(String.format("%8s", Integer.toBinaryString(raw[j] & 0xFF)).replace(' ', '0') + " ");
                System.out.print(String.format("%8s", Integer.toBinaryString(raw[j + 1] & 0xFF)).replace(' ', '0') + " ");
                System.out.print(String.format("%8s", Integer.toBinaryString(raw[j + 2] & 0xFF)).replace(' ', '0') + " ");
                System.out.print(String.format("%8s", Integer.toBinaryString(raw[j + 3] & 0xFF)).replace(' ', '0') + " ");
                System.out.print(String.format("%8s", Integer.toBinaryString(raw[j + 4] & 0xFF)).replace(' ', '0') + " ");
                System.out.print(String.format("%8s", Integer.toBinaryString(raw[j + 5] & 0xFF)).replace(' ', '0') + " ");
                System.out.print(String.format("%8s", Integer.toBinaryString(raw[j + 6] & 0xFF)).replace(' ', '0') + " ");
                System.out.println(String.format("%8s", Integer.toBinaryString(raw[j + 7] & 0xFF)).replace(' ', '0'));
            }

            os.write(raw);
            System.out.println("Server (packet " + (i + 1) + ")> " + in.readLine());
        }
    }

    public Ipv6Client(byte[] d) throws IOException {
        version = 6;
        trafiicClass = 0; //Don’t implement
        flowLabel = 0; //Don’t implement
        payloadLength = (short) (d.length);
        nextHeader = 17;
        hopLimit = 20;
        sourceAddress = new byte[]{71, 0, 0, 15};
        destinationAddress = new byte[]{55, 55, 555, 55};
        data = d;
    }

    public byte[] makeHeaderWithoutChecksum() throws IOException {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        b.write((version << 4)); //shift version 4 bits
        b.write(trafiicClass); //bits 8 - 15
        b.write(flowLabel); //bits 16 - 23
        b.write(flowLabel); //bits 24 - 31
        writeShort(b, payloadLength);
        b.write(nextHeader);
        b.write(hopLimit);

        //10 bytes of 0's, 2 bytes of 1's, IP address
        b.write(0);
        b.write(0);
        b.write(0);
        b.write(0);
        b.write(0);
        b.write(0);
        b.write(0);
        b.write(0);
        b.write(0);
        b.write(0);
        b.write(0xFF);
        b.write(0xFF);
        b.write(sourceAddress);
        b.write(0);
        b.write(0);
        b.write(0);
        b.write(0);
        b.write(0);
        b.write(0);
        b.write(0);
        b.write(0);
        b.write(0);
        b.write(0);
        b.write(0xFF);
        b.write(0xFF);
        b.write(destinationAddress);
        return b.toByteArray();
    }

    public void writeShort(OutputStream out, Short value) throws IOException {
        out.write((byte) (0xFF & value >> 8));
        out.write((byte) (0xFF & value));
    }

    public byte[] makeHeader() throws IOException {
        byte[] header = makeHeaderWithoutChecksum();
        return header;
    }

    public byte[] getRaw() throws IOException {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        b.write(makeHeader());
        b.write(data);
        return b.toByteArray();
    }
}