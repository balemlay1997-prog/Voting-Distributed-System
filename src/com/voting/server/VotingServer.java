package com.voting.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

public class VotingServer {

    private static final int PORT = 5000;
    private static final Set<String> votedUsers = new HashSet<>();
    private static int votesA = 0;
    private static int votesB = 0;

    public static void main(String[] args) {
        System.out.println("Voting Server Started on port " + PORT + "...");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket socket = serverSocket.accept();
                // Handle every client on its own thread so multiple voters can connect at once.
                new Thread(() -> handleClient(socket)).start();
            }
        } catch (IOException e) {
            System.out.println("Server Error: " + e.getMessage());
        }
    }

    private static void handleClient(Socket socket) {
        try (
            socket;
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
        ) {
            String voterId = in.readLine();
            String vote = in.readLine();

            if (voterId == null || vote == null) {
                out.println("Invalid request.");
                return;
            }

            // Synchronize access to shared vote state to prevent duplicate votes in race conditions.
            synchronized (votedUsers) {
                if (votedUsers.contains(voterId)) {
                    out.println("You already voted!");
                    return;
                }

                if (vote.equalsIgnoreCase("A")) {
                    votesA++;
                } else if (vote.equalsIgnoreCase("B")) {
                    votesB++;
                } else {
                    out.println("Invalid candidate.");
                    return;
                }

                votedUsers.add(voterId);
                out.println("Vote submitted successfully!");
            }

            System.out.println("Current Results:");
            System.out.println("A = " + votesA);
            System.out.println("B = " + votesB);
        } catch (IOException e) {
            System.out.println("Client Error: " + e.getMessage());
        }
    }
}
