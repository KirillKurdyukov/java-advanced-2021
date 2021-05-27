package info.kgeorgiy.ja.kurdyukov.hello;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

public class CommunicationServer implements AutoCloseable {

    private final Selector selector;
    private final SocketAddress socketAddress;
    private final String prefix;
    private final int requests;

    public CommunicationServer(String host, int port, String prefix, int requests) throws IOException {
        this.selector = Selector.open();
        this.socketAddress = new InetSocketAddress(InetAddress.getByName(host), port);
        this.prefix = prefix;
        this.requests = requests;
    }

    private class ContextData {
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

        public boolean isReady() {
            return numRequest == requests;
        }

    }

    public void createCommunicate(int thread) {
        try {
            DatagramChannel channel = DatagramChannel.open();
            channel.setOption(StandardSocketOptions.SO_REUSEADDR, true)
                    .connect(socketAddress)
                    .configureBlocking(false)
                    .register(selector,
                            SelectionKey.OP_WRITE,
                            new ContextData(thread, channel.socket().getReceiveBufferSize()));
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
        try {
            channel.send(ByteBuffer.wrap(request.getBytes(StandardCharsets.UTF_8)), socketAddress);
        } catch (IOException e) {
            System.err.println("Error send message: " + request + " " + e.getMessage());
            return;
        }
        key.interestOps(SelectionKey.OP_WRITE);
    }

    private void makeResponse(SelectionKey key) {
        if (!key.isReadable())
            return;
        DatagramChannel channel = (DatagramChannel) key.channel();
        ContextData data = (ContextData) key.attachment();
        ByteBuffer buffer = data.getData();
        buffer.clear();
        try {
            channel.receive(buffer);
        } catch (IOException e) {
            System.err.println("Error receive data: " + e.getMessage());
        }
        String response = UtilityUDP.decodeBuffer(buffer);
        String request = UtilityUDP.generateMessage(data.numThread, data.numRequest, prefix);
        if (response.contains(request)) {
            data.numRequest++;
            UtilityUDP.log(request, response);
        }
        if (!data.isReady()) {
            key.interestOps(SelectionKey.OP_WRITE);
        } else try {
            channel.close();
        } catch (IOException e) {
            System.err.println("Error close channel. " + e.getMessage());
        }
    }

    public void start() {
        while(!Thread.interrupted() && !selector.keys().isEmpty()) {
            try {
                selector.select(UtilityUDP.TIMEOUT);
            } catch (IOException e) {
                System.err.println("Too long wait. " + e.getMessage());
            }
            if (!selector.selectedKeys().isEmpty()) {
                for (final Iterator<SelectionKey> it = selector.selectedKeys().iterator(); it.hasNext(); ) {
                    final SelectionKey key = it.next();
                    try {
                        makeRequest(key);
                        makeResponse(key);
                    } finally {
                        it.remove();
                    }
                }
            }
        }
    }

    @Override
    public void close() throws IOException {
        selector.close();
    }
}
