package info.kgeorgiy.ja.kurdyukov.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HelloUPDClient implements HelloClient {

    @Override
    public void run(String s, int i, String s1, int i1, int i2) {
        ExecutorService workers = Executors.newFixedThreadPool()
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

        HelloUPDClient client = new HelloUPDClient();

        client.run(args[0],
                Integer.parseInt(args[1]),
                args[2],
                Integer.parseInt(args[3]),
                Integer.parseInt(args[4]));

    }
}
