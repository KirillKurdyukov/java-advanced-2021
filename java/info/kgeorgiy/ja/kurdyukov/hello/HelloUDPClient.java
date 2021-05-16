package info.kgeorgiy.ja.kurdyukov.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

public class HelloUDPClient implements HelloClient {

    private static class CommunicationServer implements AutoCloseable {
        private final int threads;
        private final String prefix;
        private final int requests;
        private final ExecutorService workers;
        private final SocketAddress address;

        private CommunicationServer(String host,
                                    int port,
                                    String prefix,
                                    ExecutorService workers,
                                    int threads,
                                    int requests) throws UnknownHostException {
            this.prefix = prefix;
            this.workers = workers;
            this.threads = threads;
            this.requests = requests;
            address = new InetSocketAddress(InetAddress.getByName(host), port);
        }

        private void communicate(int i) {
            workers.submit(() -> {
                try (DatagramSocket socket = new DatagramSocket()) {
                    int sizeBuffer = socket.getReceiveBufferSize();
                    socket.setSoTimeout(100);
                    IntStream.range(0, requests).forEach(j ->
                            doRequestAndGetResponse(i, socket, sizeBuffer, j)
                    );
                } catch (IOException e) {
                    System.err.println("Error create client socket. " + e.getMessage());
                }
            });
        }

        private void doRequestAndGetResponse(int i, DatagramSocket socket, int sizeBuffer, int j) {
            String request = prefix + i + "_" + j;
            byte[] bytesRequest = request.getBytes(StandardCharsets.UTF_8);
            DatagramPacket packetRequest = new DatagramPacket(bytesRequest,
                    bytesRequest.length,
                    address
            );
            DatagramPacket packetResponse = new DatagramPacket(new byte[sizeBuffer], sizeBuffer);
            while (!socket.isClosed() && !Thread.interrupted()) {
                try {
                    socket.send(packetRequest);
                    socket.receive(packetResponse);
                    break;
                } catch (IOException ignored) {}
            }
            String answerServer = new String(packetResponse.getData(),
                    packetResponse.getOffset(),
                    packetResponse.getLength(),
                    StandardCharsets.UTF_8);
            System.out.println(answerServer);
        }

        @Override
        public void close() {
            workers.shutdown();
            try {
                if (!workers.awaitTermination((long) threads * requests, TimeUnit.SECONDS)) {
                    System.err.println("Client too long.");
                }
                workers.shutdownNow();
            } catch (InterruptedException ignored) {
            }
        }
    }

    @Override
    public void run(String host, int port, String prefix, int threads, int requests) {
        try (
                CommunicationServer communicationServer = new CommunicationServer(host,
                        port,
                        prefix,
                        Executors.newFixedThreadPool(threads),
                        threads,
                        requests)
        ) {
            IntStream.range(0, threads).forEach(communicationServer::communicate);
        } catch (UnknownHostException e) {
            System.err.println("Error found address server error");
        }
    }

    public static void main(String[] args) {

        if (args == null || args.length != 5 || Arrays.stream(args).anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("Correct usage:\n" +
                    "1.Name or ip - address of computer on which the server is running;\n" +
                    "2.Port number to send requests to\n" +
                    "3.Request prefix (string)\n" +
                    "4.Number of parallel requests streams.\n" +
                    "5.The number of requests in each thread.");
        }

        HelloUDPClient client = new HelloUDPClient();

        client.run(args[0],
                Integer.parseInt(args[1]),
                args[2],
                Integer.parseInt(args[3]),
                Integer.parseInt(args[4])
        );

    }
}
