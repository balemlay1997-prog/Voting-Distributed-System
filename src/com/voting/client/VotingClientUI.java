package com.voting.client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class VotingClientUI extends Application {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 5000;

    @Override
    public void start(Stage stage) {
        stage.setTitle("Distributed Voting System");

        Label headerLabel = new Label("Cast Your Vote");
        headerLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white;");

        TextField voterIdField = new TextField();
        voterIdField.setPromptText("Enter Voter ID");
        voterIdField.setMaxWidth(240);
        voterIdField.setStyle("-fx-background-radius: 8; -fx-padding: 10;");

        RadioButton rbA = new RadioButton("Candidate A");
        RadioButton rbB = new RadioButton("Candidate B");
        rbA.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
        rbB.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");

        ToggleGroup group = new ToggleGroup();
        rbA.setToggleGroup(group);
        rbB.setToggleGroup(group);

        Button voteButton = new Button("Submit Vote");
        voteButton.setDefaultButton(true);
        voteButton.setStyle(
            "-fx-background-color: #ffffff; " +
            "-fx-text-fill: #1a73e8; " +
            "-fx-font-weight: bold; " +
            "-fx-background-radius: 20; " +
            "-fx-padding: 10 24 10 24;"
        );

        Label statusLabel = new Label("Status: Ready");
        statusLabel.setWrapText(true);
        statusLabel.setMaxWidth(260);
        statusLabel.setAlignment(Pos.CENTER);
        statusLabel.setStyle("-fx-text-fill: white; -fx-font-size: 13px;");

        voteButton.setOnAction(event -> {
            String voterId = voterIdField.getText().trim();
            RadioButton selected = (RadioButton) group.getSelectedToggle();

            // Validate all client-side input before opening a socket connection.
            if (voterId.isEmpty()) {
                statusLabel.setText("Status: Voter ID cannot be empty.");
                return;
            }

            if (!voterId.matches("^[a-zA-Z0-9]+$")) {
                statusLabel.setText("Status: Invalid ID! Use letters and numbers only.");
                return;
            }

            if (selected == null) {
                statusLabel.setText("Status: Please select a candidate!");
                return;
            }

            String vote = selected == rbA ? "A" : "B";
            boolean submitted = sendVote(voterId, vote, statusLabel);
            if (submitted) {
                voterIdField.clear();
                group.selectToggle(null);
            }
        });

        VBox layout = new VBox(15, headerLabel, voterIdField, rbA, rbB, voteButton, statusLabel);
        layout.setPadding(new Insets(25));
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: linear-gradient(to bottom right, #1a73e8, #0d47a1);");

        Scene scene = new Scene(layout, 320, 400);
        stage.setScene(scene);
        stage.show();
    }

    private boolean sendVote(String id, String vote, Label status) {
        // Each vote uses a short-lived socket: connect, send voter ID/vote, read response.
        try (
            Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))
        ) {
            out.println(id);
            out.println(vote);

            String response = in.readLine();
            status.setText("Server: " + response);
            return response != null && response.toLowerCase().contains("successfully");
        } catch (Exception ex) {
            status.setText("Status: Connection Error. Start the server first.");
            return false;
        }
    }

    public static void main(String[] args) {
        launch();
    }
}
