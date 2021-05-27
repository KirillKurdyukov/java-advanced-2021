package info.kgeorgiy.ja.kurdyukov.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;

public class HelloUDPNNonblockingServer implements HelloServer {
    @Override
    public void start(int i, int i1) {

    }

    @Override
    public void close() {

    }

    public static void main(String[] args) {
        UtilityUDP.mainServer(args, new HelloUDPNNonblockingServer());
    }
}
