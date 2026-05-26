module DistributedVotingSystem {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.voting.client to javafx.graphics, javafx.fxml;
}
