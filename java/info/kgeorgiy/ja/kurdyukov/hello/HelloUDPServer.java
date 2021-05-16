package info.kgeorgiy.ja.kurdyukov.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

public class HelloUDPServer implements HelloServer {

    private ExecutorService workers;
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
        server.start(Integer.parseInt(args[0]),
                Integer.parseInt(args[1]));
    }

    public boolean init(int port) {
        try {
            socket = new DatagramSocket(port);
            socket.setSoTimeout(1000);
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
        if (!init(port))
            return;
        IntStream.range(0, threads).forEach(i -> workers.submit(() -> {
            while (!socket.isClosed() && !Thread.interrupted()) {
                DatagramPacket packetRequest = new DatagramPacket(new byte[sizeBuffer], sizeBuffer);
                try {
                    socket.receive(packetRequest);
                } catch (IOException ignored) {
                    continue;
                }
                String data = new String(packetRequest.getData(),
                        packetRequest.getOffset(),
                        packetRequest.getLength(),
                        StandardCharsets.UTF_8);
                data = "Hello, " + data;
                byte[] bytesData = data.getBytes(StandardCharsets.UTF_8);
                SocketAddress address = new InetSocketAddress(packetRequest.getAddress(), packetRequest.getPort());
                DatagramPacket packetResponse = new DatagramPacket(bytesData,
                        bytesData.length,
                        address
                );
                try {
                    socket.send(packetResponse);
                } catch (IOException ignored) {}
            }
        }));
    }

    @Override
    public void close() {
        workers.shutdown();
        socket.close();
        try {
            if (!workers.awaitTermination(10, TimeUnit.SECONDS))
                workers.shutdownNow();
        } catch (InterruptedException ignored) {}
    }

}
