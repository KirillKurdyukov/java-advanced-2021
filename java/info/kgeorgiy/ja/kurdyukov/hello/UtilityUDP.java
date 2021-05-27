package info.kgeorgiy.ja.kurdyukov.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;
import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.net.DatagramPacket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;

public class UtilityUDP {

    public static final int TIMEOUT = 100;

    public static void log(String request, String response) {
        System.out.println("Request: " + request);
        System.out.println("Response: " + response);
    }

    public static String decodeBuffer(ByteBuffer buffer) {
        return StandardCharsets.UTF_8.decode(buffer).toString();
    }

    public static String generateMessage(int numThread, int numRequest, String prefix) {
        return prefix + numThread + "_" + numRequest;
    }

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

    public static void mainClient(String[] args, HelloClient client) {

        if (args == null
                || args.length != 5
                || Arrays.stream(args).anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("Correct usage:\n" +
                    "1.Name or ip - address of computer on which the server is running;\n" +
                    "2.Port number to send requests to\n" +
                    "3.Request prefix (string)\n" +
                    "4.Number of parallel requests streams.\n" +
                    "5.The number of requests in each thread.");
        }

        try {
            client.run(args[0],
                    Integer.parseInt(args[1]),
                    args[2],
                    Integer.parseInt(args[3]),
                    Integer.parseInt(args[4])
            );
        } catch (NumberFormatException e) {
            System.err.println("Incorrect integer argument. " + e.getMessage());
        }
    }

    public static void mainServer(String[] args, HelloServer server) {
        if (args == null ||
                args.length != 2 ||
                args[0] == null ||
                args[1] == null) {
            throw new IllegalArgumentException("Correct usage:\n " +
                    "1.Port number on which requests will be received\n" +
                    "2.The number of worker threads that will process requests\n");
        }

        try {
            server.start(Integer.parseInt(args[0]),
                    Integer.parseInt(args[1]));
        } catch (NumberFormatException e) {
            System.err.println("Incorrect integer arguments. " + e.getMessage());
        }
    }

}