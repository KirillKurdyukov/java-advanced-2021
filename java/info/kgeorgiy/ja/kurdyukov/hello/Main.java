package info.kgeorgiy.ja.kurdyukov.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;

public class Main {
    public static void main(String[] args) {
        HelloServer hs = new HelloUDPNonblockingServer();
        hs.start(8080, 1);
        new HelloUDPClient().run("localhost", 8080, "HELLO", 1, 3);
        hs.close();
    }
}
