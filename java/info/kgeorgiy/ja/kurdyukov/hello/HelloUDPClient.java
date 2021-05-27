package info.kgeorgiy.ja.kurdyukov.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.io.IOException;
import java.net.*;
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
                    socket.setSoTimeout(UtilityUDP.TIMEOUT);
                    int sizeBuffer = socket.getReceiveBufferSize();
                    IntStream.range(0, requests).forEach(j ->
                            doRequestAndGetResponse(i, socket, sizeBuffer, j)
                    );
                } catch (IOException e) {
                    System.err.println("Error create client socket. " + e.getMessage());
                }
            });
        }

        private void doRequestAndGetResponse(int i, DatagramSocket socket, int sizeBuffer, int j) {
            String request = UtilityUDP.generateMessage(i, j, prefix);
            DatagramPacket packetRequest = UtilityUDP.getPacket(request, address);
            DatagramPacket packetResponse = new DatagramPacket(new byte[sizeBuffer], sizeBuffer);
            while (!socket.isClosed() && !Thread.interrupted()) {
                try {
                    socket.send(packetRequest);
                    socket.receive(packetResponse);
                } catch (IOException ignored) {}
                String answerServer = UtilityUDP.getData(packetResponse);
                if (answerServer.contains(request)) {
                    UtilityUDP.log(request, answerServer);
                    break;
                }
            }
        }

        @Override
        public void close() {
            workers.shutdown();
            try {
                if (!workers.awaitTermination((long) threads * requests, TimeUnit.SECONDS)) {
                    System.err.println("Client too long.");
                }
                workers.shutdownNow();
            } catch (InterruptedException ignored) {}
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
            System.err.println("Error found address server. " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        UtilityUDP.mainClient(args, new HelloUDPClient());
    }

}
