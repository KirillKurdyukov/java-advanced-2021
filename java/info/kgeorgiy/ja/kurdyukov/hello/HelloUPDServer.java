package info.kgeorgiy.ja.kurdyukov.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;

public class HelloUPDServer implements HelloServer {

    public static void main(String[] args) {
        if (args == null ||
                args.length != 2 ||
                args[0] == null ||
                args[1] == null) {
            throw new IllegalArgumentException("Correct usage:\n " +
                    "1.Port number on which requests will be received\n" +
                    "2.The number of worker threads that will process requests\n");
        }

        HelloUPDServer server = new HelloUPDServer();
        server.start(Integer.parseInt(args[0]),
                Integer.parseInt(args[1]));
    }

    @Override
    public void start(int i, int i1) {

    }

    @Override
    public void close() {

    }

}
