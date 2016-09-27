/* Joel Castro
 * CS380 - Project 3
 *
 * Driver: Ipv4Client.java
 * Implementing IPv4.
 * Server will accept valid IPv4 packets, verify the checksum, and reply ”good” or ”bad”.
 */

package ipv4client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class Ipv4Packet {
    byte version; //Always 4
    byte headerLength; //5
    byte tos; //Do not implement
    short totalLength; //HL + Data
    short identification; //Do not implement
    short fragmentation; //Do not implement
    byte ttl; //50
    byte protocol; //Assuming TCP for all packets (0x06)
    short checksum;
    byte[] sourceAddress; //My IP address 71.93.222.15
    byte[] destinationAddress; //55.55.555.55
    byte[] data;
        
    public Ipv4Packet(byte[] d) throws IOException {
        version = 4;
        headerLength = 5;
        tos = 0; //Do not implement
        totalLength = (short)(20 + d.length);
        identification = 0; //Do not implement
        fragmentation = 0x4000; //Assuming no fragmentation
        ttl = 50;
        protocol = 0x06; //Assuming TCP for all packets
        sourceAddress = new byte[] {71, 0, 0, 15};
        destinationAddress = new byte[] {55, 55, 555, 55};
        checksum = calChecksum();
        data = d;
    }
    
    public short calChecksum() throws IOException {
        byte[] headerWithoutChecksum = makeHeaderWithoutChecksum();
        return checksum(headerWithoutChecksum);
    }
    
    public byte[] makeHeaderWithoutChecksum() throws IOException {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        b.write((version << 4) | headerLength); //shift version 4 bits
        b.write(tos);
        writeShort(b, totalLength);
        writeShort(b, identification);
        writeShort(b, fragmentation);
        b.write(ttl);
        b.write(protocol);
        writeShort(b, (short)0);
        b.write(sourceAddress);
        b.write(destinationAddress);
        return b.toByteArray();
    }
    
    public short checksum(byte[] d) {
        int sum = 0;
        for (int i = 0; i < d.length; i += 2) {
            int current = 0xFF & d[i];
            current <<= 8;
            int next = 0xFF & d[i +1];
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
    
    public void writeShort(OutputStream out, Short value) throws IOException {
        out.write((byte) (0xFF & value >> 8));
        out.write((byte) (0xFF & value));
    }
    
    public byte[] getRaw() throws IOException {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        b.write(makeHeader());
        b.write(data);
        return b.toByteArray();
    }
    
    public byte[] makeHeader() throws IOException {
        byte[] header = makeHeaderWithoutChecksum();
        header[10] = (byte) (0xFF & (checksum >> 8)); //shift and add 0's in front
        header[11] = (byte) (0xFF & checksum);
        return header;
    }
}