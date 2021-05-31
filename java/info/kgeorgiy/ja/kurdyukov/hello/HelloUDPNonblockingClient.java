package info.kgeorgiy.ja.kurdyukov.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.stream.IntStream;

public class HelloUDPNonblockingClient implements HelloClient {

    private static class ContextData {

        private final int numThread;
        private int numRequest = 0;
        private final ByteBuffer buffer;

        public ContextData(int numThread, int bufferSize) {
            this.numThread = numThread;
            this.buffer = ByteBuffer.allocate(bufferSize);
        }

        public ByteBuffer getData() {
            return buffer;
        }

        public boolean isReady(int requests) {
            return numRequest == requests;
        }

    }

    private static class UnblockingCommunication implements AutoCloseable {

        private final Selector selector;
        private final SocketAddress socketAddress;
        private final String prefix;
        private final int requests;

        public UnblockingCommunication(String host, int port, String prefix, int requests) throws IOException {
            this.selector = Selector.open();
            this.socketAddress = new InetSocketAddress(InetAddress.getByName(host), port);
            this.prefix = prefix;
            this.requests = requests;
        }

        public void createCommunicate(int thread) {
            try {
                DatagramChannel channel = DatagramChannel.open();
                try {
                    channel.setOption(StandardSocketOptions.SO_REUSEADDR, true)
                            .connect(socketAddress)
                            .configureBlocking(false)
                            .register(selector,
                                    SelectionKey.OP_WRITE,
                                    new ContextData(thread, UtilityUDP.BUFFER_SIZE));
                } catch (Throwable e) {
                    channel.close();
                    throw e;
                }
            } catch (IOException e) {
                System.err.println("Can't create datagram channel. " + e.getMessage());
            }
        }

        private void makeRequest(SelectionKey key) {
            if (!key.isWritable())
                return;
            DatagramChannel channel = (DatagramChannel) key.channel();
            ContextData data = (ContextData) key.attachment();
            String request = UtilityUDP.generateMessage(data.numThread,
                    data.numRequest,
                    prefix);
            ByteBuffer buffer = data.getData();
            UtilityUDP.setInBufferData(buffer, request);
            try {
                channel.write(buffer);
            } catch (IOException e) {
                System.err.println("Error send message: " + request + " " + e.getMessage());
                return;
            }
            key.interestOps(SelectionKey.OP_READ);
        }

        private void makeResponse(SelectionKey key) {
            if (!key.isReadable())
                return;
            DatagramChannel channel = (DatagramChannel) key.channel();
            ContextData data = (ContextData) key.attachment();
            ByteBuffer buffer = data.getData().clear();
            try {
                channel.receive(buffer);
            } catch (IOException e) {
                System.err.println("Error receive data: " + e.getMessage());
            }
            String response = UtilityUDP.getDecodeString(buffer);
            String request = UtilityUDP.generateMessage(data.numThread, data.numRequest, prefix);
            if (response.contains(request)) {
                data.numRequest++;
                UtilityUDP.log(request, response);
            }
            if (!data.isReady(requests)) {
                key.interestOps(SelectionKey.OP_WRITE);
            } else try {
                channel.close();
            } catch (IOException e) {
                System.err.println("Error close channel. " + e.getMessage());
            }
        }

        public void start(int threads) {
            try {
                IntStream.range(0, threads).forEach(this::createCommunicate);
                do {
                    try {
                        selector.select(UtilityUDP.TIMEOUT);
                    } catch (IOException e) {
                        System.err.println("Too long wait. " + e.getMessage());
                    }
                    if (!selector.selectedKeys().isEmpty()) {
                        for (final var it = selector.selectedKeys().iterator(); it.hasNext(); ) {
                            final SelectionKey key = it.next();
                            makeRequest(key);
                            makeResponse(key);
                            it.remove();
                        }
                    } else {
                        selector.keys().forEach(this::makeRequest);
                    }
                } while (!Thread.interrupted() && !selector.keys().isEmpty());
            } finally {
                selector.keys().forEach(key -> {
                    Channel channel = key.channel();
                    if (channel.isOpen())
                        try {
                            channel.close();
                        } catch (IOException e) {
                            System.err.println("Error close channel. " + e.getMessage());
                        }
                });
            }
        }

        @Override
        public void close() throws IOException {
            if (selector != null)
                selector.close();
        }
    }

    @Override
    public void run(String host, int port, String prefix, int threads, int requests) {
        try (UnblockingCommunication communicationServer = new UnblockingCommunication(host, port, prefix, requests)) {
            communicationServer.start(threads);
        } catch (IOException e) {
            System.err.println("Error create selector or found address. " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        UtilityUDP.mainClient(args, new HelloUDPNonblockingClient());
    }

}
