package info.kgeorgiy.ja.kurdyukov.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.Selector;

public class HelloUDPNNonblockingServer implements HelloServer {
    private Selector selector;
    private SocketAddress address;

    public boolean init(int port) {
        try {
            selector = Selector.open();
        } catch (IOException e) {
            System.err.println("Error create selector. " + e.getMessage());
            return false;
        }
        address = new InetSocketAddress(port);
        return true;
    }

    private void createChannel(int i) {

    }

    @Override
    public void start(int port, int threads) {
        if (!init(port))
            return;

    }

    @Override
    public void close() {

    }

    public static void main(String[] args) {
        UtilityUDP.mainServer(args, new HelloUDPNNonblockingServer());
    }
}
