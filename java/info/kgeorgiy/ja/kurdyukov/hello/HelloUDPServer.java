package info.kgeorgiy.ja.kurdyukov.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class HelloUDPServer implements HelloServer {

    private ExecutorService workers;
    private ExecutorService listener;
    private DatagramSocket socket;
    private int sizeBuffer;

    public static void main(String[] args) {
        if (args == null ||
                args.length != 2 ||
                args[0] == null ||
                args[1] == null) {
            throw new IllegalArgumentException("Correct usage:\n " +
                    "1.Port number on which requests will be received\n" +
                    "2.The number of worker threads that will process requests\n");
        }

        HelloUDPServer server = new HelloUDPServer();
        try {
            server.start(Integer.parseInt(args[0]),
                    Integer.parseInt(args[1]));
        } catch (NumberFormatException e) {
            System.err.println("Incorrect integer arguments. " + e.getMessage());
        }
    }

    public boolean init(int port) {
        try {
            socket = new DatagramSocket(port);
            socket.setSoTimeout(100);
        } catch (SocketException e) {
            System.err.println("Error create client socket. " + e.getMessage());
            return false;
        }
        try {
            sizeBuffer = socket.getReceiveBufferSize();
        } catch (SocketException e) {
            System.err.println("Server port error. " + e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public void start(int port, int threads) {
        workers = Executors.newFixedThreadPool(threads);
        listener = Executors.newSingleThreadExecutor();
        if (!init(port))
            return;
        listener.submit(() -> {
            while (!socket.isClosed() && !Thread.interrupted()) {
                DatagramPacket packetCommunicate = new DatagramPacket(new byte[sizeBuffer], sizeBuffer);
                try {
                    socket.receive(packetCommunicate);
                } catch (IOException ignored) {
                    continue;
                }
                workers.submit(() -> {
                    String data = UtilityUDP.getData(packetCommunicate);
                    packetCommunicate.setData(("Hello, " + data).getBytes(StandardCharsets.UTF_8));
                    try {
                        socket.send(packetCommunicate);
                    } catch (IOException ignored) {}
                });
            }
        });
    }

    @Override
    public void close() {
        workers.shutdown();
        listener.shutdown();
        socket.close();
        try {
            if (!workers.awaitTermination(10, TimeUnit.SECONDS))
                workers.shutdownNow();
            if (!listener.awaitTermination(10, TimeUnit.SECONDS))
                listener.shutdownNow();
        } catch (InterruptedException ignored) {}
    }

}
