package com.example;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;

public class NIOEchoServer {

    public static void main(String[] args) throws Exception {
        Selector selector = Selector.open();
        ServerSocketChannel serverSocket = ServerSocketChannel.open();
        serverSocket.bind(new InetSocketAddress("localhost", 8080));
        serverSocket.configureBlocking(false);
        serverSocket.register(selector, SelectionKey.OP_ACCEPT);
        ByteBuffer buffer = ByteBuffer.allocate(1024);

        System.out.println("Now listening on 8080 ...");
        while (true) {
            selector.select();
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> iter = selectedKeys.iterator();
            while (iter.hasNext()) {
                SelectionKey key = iter.next();

                if (key.isAcceptable()) {
                    SocketChannel socketChannel = serverSocket.accept();
                    socketChannel.configureBlocking(false);
                    socketChannel.register(selector, SelectionKey.OP_READ);
                    System.out.println("Connected " + socketChannel.getRemoteAddress());
                }

                if (key.isReadable()) {
                    SocketChannel socketChannel = (SocketChannel) key.channel();
                    socketChannel.read(buffer);
                    String message = new String(buffer.array(), 0, buffer.position(), StandardCharsets.UTF_8);
                    System.out.println(message);

                    buffer.flip();
                    socketChannel.write(buffer);
                    buffer.clear();

                    if (message.trim().equalsIgnoreCase("END")) {
                        System.out.println("Connection closed " + socketChannel.getRemoteAddress());
                        socketChannel.close();
                    }
                }

                iter.remove();
            }
        }
    }
}