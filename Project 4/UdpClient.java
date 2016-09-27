/* Joel Castro
 * CS380 - Project 4
 *
 * Implementing UDP on top of IPv4
 * Create IPv4 packets, then create UDP packets as the data for the IPv4 packets.
 * Server will first check the IPv4 headers to ensure the IPv4 packet is
 * constructed correctly, then check the IPv4 data to get the UDP packet and
 * check the header and data.
 */
package udpclient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Random;

public class UdpClient {
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

    public static void main(String[] args) throws Exception {
        Socket socket = new Socket("55.55.555.55", 11111);
        OutputStream os = socket.getOutputStream();
        InputStream in = socket.getInputStream();
        Random rand = new Random();        
        byte[] DEADBEEF = {(byte) 0xDE, (byte) 0xAD, (byte) 0xBE, (byte) 0xEF};
        int size;
        long totalTime = 0;

        //First packet (IPv4)
        UdpClient singlePacket = new UdpClient(DEADBEEF);
        byte[] singlePacketRaw = singlePacket.getRaw();
        os.write(singlePacketRaw);

        byte[] bytePort = {(byte) in.read(), (byte) in.read()};

        //PRINT PORT!!!
//        int port = ((bytePort[0] << 8) | bytePort[1] & 0xFF) & 0xFFFF;
//        System.out.println("port: " + port); //random port

        size = 2;
        
        //Send 10 UDP Packets
        for (int i = 0; i < 10; i++) {
            data = new byte[size];
            rand.nextBytes(data); //Generates random bytes

            //UDP Packet
            UdpClient UDPP = new UdpClient(data, bytePort);
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

            //Send and print response and time
            os.write(UDPPRaw);
            long start = System.currentTimeMillis();
            byte[] msg = {(byte) in.read(), (byte) in.read(), (byte) in.read(), (byte) in.read()};
            long finish = System.currentTimeMillis();
            System.out.printf("Response (packet " + (i + 1) + "): 0x%X%X%X%X\n", msg[0], msg[1], msg[2], msg[3]);
            totalTime += finish - start;
            System.out.println((finish - start) + "ms");
            //Double up size for next packet
            size *= 2;
        }
        System.out.format("Average RTT: %.1fms\n", totalTime/10.0);
    }

    private UdpClient(byte[] d) throws IOException {
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

    private UdpClient(byte[] d, byte[] p) throws IOException {
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
        sourcePort = new byte[]{(byte) 0x56, (byte) 0xCE};
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