package com.example;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NIOClient {

    private final SocketChannel socketChannel;
    private final ByteBuffer byteBuffer;

    public static void main(String[] args) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(3);
        CountDownLatch latch = new CountDownLatch(3);
        for (int i = 0; i < 3; i++) {
            executor.submit(() -> {
                try {
                    NIOClient client = new NIOClient();

                    client.sendMessage("Hello, NIO!");
                    client.sendMessage("Hello");
                    client.sendMessage("END");
                } catch (IOException e) {
                    // noop
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();
    }

    public NIOClient() throws IOException {
        socketChannel = SocketChannel.open(new InetSocketAddress("localhost", 8080));
        byteBuffer = ByteBuffer.allocate(1024);
    }

    public void sendMessage(String message) throws IOException {
        socketChannel.write(StandardCharsets.UTF_8.encode(message));

        byteBuffer.clear();
        socketChannel.read(byteBuffer);
        String received = new String(byteBuffer.array(), 0, byteBuffer.position(), StandardCharsets.UTF_8);
        System.out.println("Received: " + received);
    }
}