package info.kgeorgiy.ja.kurdyukov.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.stream.IntStream;

public class HelloUDPNonblockingServer implements HelloServer {

    private Selector selector;
    private ExecutorService workers;
    private ExecutorService listener;
    private DatagramChannel channel;

    private Queue<ByteBuffer> queueRequests; // clear buffers
    private ArrayBlockingQueue<PairData> queueResponse;

    private static class PairData {
        private final ByteBuffer buffer;
        private SocketAddress address;

        public PairData(ByteBuffer buffer) {
            this.buffer = buffer;
        }

    }

    public boolean init(int port) {
        try {
            selector = Selector.open();
        } catch (IOException e) {
            System.err.println("Error create selector. " + e.getMessage());
            return false;
        }
        SocketAddress address = new InetSocketAddress(port);
        try {
            channel = DatagramChannel.open();
            try {
                channel.setOption(StandardSocketOptions.SO_REUSEADDR, true)
                        .bind(address)
                        .configureBlocking(false)
                        .register(selector,
                                SelectionKey.OP_READ);
            } catch (Throwable e) {
                channel.close();
                return false;
            }
        } catch (IOException e) {
            System.err.println("Can't create datagram channel. " + e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public void start(int port, int threads) {
        workers = Executors.newFixedThreadPool(threads);
        listener = Executors.newSingleThreadExecutor();
        queueRequests = new ArrayDeque<>(threads);
        queueResponse = new ArrayBlockingQueue<>(threads);
        IntStream.range(0, threads)
                .forEach(i -> queueRequests
                        .add(ByteBuffer
                                .allocate(UtilityUDP.BUFFER_SIZE)));
        if (!init(port))
            return;
        listener.submit(() -> {
            while (!Thread.interrupted() && channel.isOpen()) {
                try {
                    selector.select(UtilityUDP.TIMEOUT);
                } catch (IOException e) {
                    System.err.println("Too long wait server. " + e.getMessage());
                }
                for (final var it = selector.selectedKeys().iterator(); it.hasNext(); ) {
                    SelectionKey key = it.next();
                    makeResponse(key);
                    makeRequest(key);
                    it.remove();
                }
            }
        });
    }

    private void makeResponse(SelectionKey key) {
        if (!key.isReadable())
            return;
        if (queueRequests.isEmpty()) {
            key.interestOps(SelectionKey.OP_WRITE);
            return;
        }
        ByteBuffer buffer = queueRequests.poll();
        PairData data = new PairData(buffer);
        try {
            data.address = channel.receive(buffer);
        } catch (IOException e) {
            System.err.println("Error receive message. " + e.getMessage());
        }
        workers.submit(() -> {
            String response = "Hello, " +  UtilityUDP.getDecodeString(buffer);
            UtilityUDP.setInBufferData(buffer, response);
            queueResponse.add(data);
            synchronized (key) {
                key.interestOps(SelectionKey.OP_WRITE);
            }
        });
    }

    private void makeRequest(SelectionKey key) {
        if (!key.isWritable())
            return;
        if (queueResponse.isEmpty()) {
            key.interestOps(SelectionKey.OP_READ);
            return;
        }
        PairData data = queueResponse.poll();
        try {
            channel.send(data.buffer, data.address);
        } catch (IOException e) {
            System.err.println("Error send message. " + e.getMessage());
        }
        queueRequests.add(data.buffer.clear());
        if (!queueResponse.isEmpty()) {
            key.interestOpsAnd(~SelectionKey.OP_READ);
        }
    }

    @Override
    public void close() {
        try {
            selector.close();
            channel.close();
            UtilityUDP.stopService(listener, workers);
        } catch (IOException e) {
            System.err.println("Error close selector. " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        UtilityUDP.mainServer(args, new HelloUDPNonblockingServer());
    }

}

