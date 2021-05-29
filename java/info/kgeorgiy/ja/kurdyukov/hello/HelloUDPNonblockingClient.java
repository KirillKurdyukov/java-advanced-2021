package info.kgeorgiy.ja.kurdyukov.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.io.IOException;
import java.util.stream.IntStream;

public class HelloUDPNonblockingClient implements HelloClient {

    @Override
    public void run(String host, int port, String prefix, int threads, int requests) {
        try (UnblockingCommunication communicationServer = new UnblockingCommunication(host, port, prefix, requests)){
            IntStream.range(0, threads).forEach(communicationServer::createCommunicate);
            communicationServer.start();
        } catch (IOException e) {
            System.err.println("Error create selector or found address. " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        UtilityUDP.mainClient(args, new HelloUDPNonblockingClient());
    }

}
