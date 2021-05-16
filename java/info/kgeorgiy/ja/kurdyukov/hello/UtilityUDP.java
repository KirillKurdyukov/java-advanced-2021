package info.kgeorgiy.ja.kurdyukov.hello;

import java.net.DatagramPacket;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;

public class UtilityUDP {

    public static String getData(DatagramPacket packet) {
        return new String(packet.getData(),
                packet.getOffset(),
                packet.getLength(),
                StandardCharsets.UTF_8);
    }

    public static DatagramPacket getPacket(String message, SocketAddress address) {
        byte[] bytesData = message.getBytes(StandardCharsets.UTF_8);
        return new DatagramPacket(bytesData,
                bytesData.length,
                address
        );
    }
}