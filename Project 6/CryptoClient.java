/* Joel Castro
 * CS380 - Project 6
 * 
 * Use the Java Cryptography Extension to send data securely across a network.
 * Public key is as raw data: "public.bin" file.
 * Generate a symmetric session key to use for communication and
 * send it encrypted with the given public key.
 */
package cryptoclient;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.security.Key;
import java.security.interfaces.RSAPublicKey;
import java.util.Random;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;

public class CryptoClient {

    byte version; //Always 4
    byte headerLength; //5
    byte tos; //Do not implement
    short totalLength; //HL + HL + Data
    short identification; //Do not implement
    short fragmentation; //Do not implement
    byte ttl; //50
    byte protocol; //Assuming TCP for all packets (0x06)
    short checksum;
    byte[] sourceAddress; //My IP address 71.93.222.15
    byte[] destinationAddress; //55.55.555.55
    static byte[] data;
    //UDP fields
    byte[] sourcePort;
    byte[] destinationPort;
    short UDPtotalLength; //HL + Data
    short UDPchecksum;
    static byte[] bytePort = {(byte) 0x2B, (byte) 0x67}; //port 11111

    public static void main(String[] args) throws Exception {
        Socket socket = new Socket("55.55.555.55", 11111);
        OutputStream os = socket.getOutputStream();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        InputStream in = socket.getInputStream();

        //Step 1 - Deserialize the given RSA public key from the file.
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream("public.bin"));
        Object o = ois.readObject();
        RSAPublicKey rsaPublicKey = (RSAPublicKey) o;

        //Step 2 - Generate an AES session key using KeyGenerator as
        //shown in the JCE example on Blackboard.
        Cipher aesCipher = Cipher.getInstance("AES");
        Key key = KeyGenerator.getInstance("AES").generateKey();

        //Step 3 - Serialize the session key, encrypt it with the given
        //public key, and send this cipher text as the data in an IPv4
        //packet as in project 4 when you sent the 0xDEADBEEF value.
        Cipher rsaCipher = Cipher.getInstance("RSA");
        oos.writeObject(key);
        rsaCipher.init(Cipher.ENCRYPT_MODE, rsaPublicKey);
        byte[] encryptKey = rsaCipher.doFinal(baos.toByteArray());

        //First packet (IPv4)
        CryptoClient keyPacket = new CryptoClient(encryptKey);
        byte[] keyPacketRaw = keyPacket.getRaw();
        os.write(keyPacketRaw);

        //Step 4 - I will send back 0xCAFEBABE if I successfully received your
        //key, otherwise you will get an error code just like in project 4.
        byte[] msg = {(byte) in.read(), (byte) in.read(), (byte) in.read(), (byte) in.read()};
        System.out.printf("Response (sending key packet): 0x%X%X%X%X\n\n", msg[0], msg[1], msg[2], msg[3]);

        //Step 5 - Now, you must follow the process of project 4 by sending 10
        //UDPpackets (destination port should be set to 11111 this time)
        //startingwith length 2 and doubling each packet, but encrypt the
        //entire packet using the session key. I will send back 0xCAFEBABE
        //after each packet if I can decrypt it correctly. Since you are
        //encrypting the entire packet, we can assume I decrypted it correctly
        //if I see a proper UDP/IPv4 packet on my side after decryption.
        Random rand = new Random();
        int size = 2;
        long totalTime = 0;
        int numOfPackets = 10;
        
        for (int i = 0; i < numOfPackets; i++) {
            data = new byte[size];
            rand.nextBytes(data); //Generates random bytes

            //UDP Packet
            CryptoClient UDPP = new CryptoClient(data, bytePort);
            byte[] UDPPRaw = UDPP.getUDPRaw();

            /* Print EVERYTHING!!! Good for debugging!
             System.out.println("IP Header:");
             for (int j = 0; j < 25; j += 4) {
             System.out.print(String.format("%8s", Integer.toBinaryString(UDPPRaw[j] & 0xFF)).replace(' ', '0') + " ");
             System.out.print(String.format("%8s", Integer.toBinaryString(UDPPRaw[j + 1] & 0xFF)).replace(' ', '0') + " ");
             System.out.print(String.format("%8s", Integer.toBinaryString(UDPPRaw[j + 2] & 0xFF)).replace(' ', '0') + " ");
             System.out.println(String.format("%8s", Integer.toBinaryString(UDPPRaw[j + 3] & 0xFF)).replace(' ', '0'));
             if (j == 16) {
             System.out.println("UDP Header:");
             }
             }
             System.out.println("DATA:");
             for (int j = 28; j < 28 + data.length; j++) {
             System.out.print(String.format("%8s", Integer.toBinaryString(UDPPRaw[j] & 0xFF)).replace(' ', '0') + " ");
             if ((j - 27) % 4 == 0) {
             System.out.println();
             }
             }
             System.out.println();
             */

            //Encrypt whole packet with my AES key first then send
            aesCipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] encryptedUDPPRAW = aesCipher.doFinal(UDPPRaw);
            os.write(encryptedUDPPRAW);
            
            //Step 6 - Have your program output the server's response and
            //round trip time as in project 4 for each of the 10 packets,
            //then output the average round trip time for all 10 packets.
            long start = System.currentTimeMillis();
            byte[] msg2 = {(byte) in.read(), (byte) in.read(), (byte) in.read(), (byte) in.read()};
            long finish = System.currentTimeMillis();
            System.out.printf("Response (packet " + (i + 1) + "): 0x%X%X%X%X\n", msg2[0], msg2[1], msg2[2], msg2[3]);
            totalTime += finish - start;
            System.out.println((finish - start) + "ms");
            //Double up size for next packet
            size *= 2;
        }
        System.out.format("Average RTT: %.1fms\n", totalTime / (double)numOfPackets);
    }

    private CryptoClient(byte[] d) throws IOException {
        version = 4;
        headerLength = 5;
        tos = 0; //Do not implement
        totalLength = (short) (20 + d.length);
        identification = 0; //Do not implement
        fragmentation = 0x4000; //Assuming no fragmentation
        ttl = 50;
        protocol = 0x11; //Assuming UDP for all packets
        sourceAddress = new byte[]{71, 0, 0, 15};
        destinationAddress = new byte[]{55, 55, 555, 55};
        checksum = calChecksum();
        data = d;
    }

    private CryptoClient(byte[] d, byte[] p) throws IOException {
        //IP Header
        version = 4;
        headerLength = 5;
        tos = 0; //Do not implement
        totalLength = (short) (20 + 8 + d.length);
        identification = 0; //Do not implement
        fragmentation = 0x4000; //Assuming no fragmentation
        ttl = 50;
        protocol = 0x11; //Assuming UDP for all packets
        sourceAddress = new byte[]{71, 0, 0, 15};
        destinationAddress = new byte[]{55, 55, 555, 55};
        checksum = calChecksum();

        //UDP Header
        sourcePort = new byte[]{(byte) 0x2B, (byte) 0x67};
        destinationPort = p;
        UDPtotalLength = (short) (8 + d.length);
        UDPchecksum = calUDPChecksum();

        data = d;
    }

    private short calChecksum() throws IOException {
        return checksum(makeHeaderWithoutChecksum());
    }

    private short calUDPChecksum() throws IOException {
        return checksum(UDPChecksum());
    }

    private byte[] makeHeaderWithoutChecksum() throws IOException {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        b.write((version << 4) | headerLength); //shift version 4 bits
        b.write(tos);
        writeShort(b, totalLength);
        writeShort(b, identification);
        writeShort(b, fragmentation);
        b.write(ttl);
        b.write(protocol);
        writeShort(b, (short) 0);
        b.write(sourceAddress);
        b.write(destinationAddress);
        return b.toByteArray();
    }

    private byte[] UDPChecksum() throws IOException {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        b.write(sourceAddress);
        b.write(destinationAddress);
        b.write(0);
        b.write(protocol);
        writeShort(b, UDPtotalLength);
        b.write(data);
        b.write(sourcePort);
        b.write(destinationPort);
        writeShort(b, UDPtotalLength);
        writeShort(b, (short) 0);

        return b.toByteArray();
    }

    private byte[] makeUDPHeaderWithoutChecksum() throws IOException {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        b.write((version << 4) | headerLength); //shift version 4 bits
        b.write(tos);
        writeShort(b, totalLength);
        writeShort(b, identification);
        writeShort(b, fragmentation);
        b.write(ttl);
        b.write(protocol);
        writeShort(b, (short) 0);
        b.write(sourceAddress);
        b.write(destinationAddress);

        //UDP fields
        b.write(sourcePort);
        b.write(destinationPort);
        writeShort(b, UDPtotalLength);
        writeShort(b, (short) 0);

        return b.toByteArray();
    }

    private short checksum(byte[] d) {
        int sum = 0;
        for (int i = 0; i < d.length; i += 2) {
            int current = 0xFF & d[i];
            current <<= 8;
            int next = 0xFF & d[i + 1];
            current |= next;
            sum += current;
            if ((0xFFFF0000 & sum) != 0) {
                sum &= 0xFFFF; //mask
                sum++;
            }
        }
        sum = ~(sum & 0xFFFF);
        return (short) sum;
    }

    private void writeShort(OutputStream out, Short value) throws IOException {
        out.write((byte) (0xFF & value >> 8));
        out.write((byte) (0xFF & value));
    }

    private byte[] getRaw() throws IOException {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        b.write(makeHeader());
        b.write(data);
        return b.toByteArray();
    }

    private byte[] getUDPRaw() throws IOException {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        b.write(makeUDPHeader());
        b.write(data);
        return b.toByteArray();
    }

    private byte[] makeHeader() throws IOException {
        byte[] header = makeHeaderWithoutChecksum();
        header[10] = (byte) (0xFF & (checksum >> 8)); //shift and add 0's in front
        header[11] = (byte) (0xFF & checksum);
        return header;
    }

    private byte[] makeUDPHeader() throws IOException {
        byte[] header = makeUDPHeaderWithoutChecksum();
        header[10] = (byte) (0xFF & (checksum >> 8)); //shift and add 0's in front
        header[11] = (byte) (0xFF & checksum);

        header[26] = (byte) (0xFF & (UDPchecksum >> 8)); //shift and add 0's in front
        header[27] = (byte) (0xFF & UDPchecksum);
        return header;
    }
}