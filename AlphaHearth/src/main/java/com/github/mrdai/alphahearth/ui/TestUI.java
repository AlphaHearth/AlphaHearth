package com.github.mrdai.alphahearth.ui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

/**
 * User Interface for running test game.
 */
public class TestUI extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("AlphaHearth - Test Panel");

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));


    }

    /** Creates a new {@code GridPane} for player configuring */
    private GridPane playerConfig() {
        final GridPane playerGrid = new GridPane();

        Label typeText = new Label("Type:");
        playerGrid.add(typeText, 0, 0);

        ChoiceBox typeSelect = new ChoiceBox();
        typeSelect.getItems().addAll("Random", "Rule-based", "Monte Carlo Search");
        typeSelect.setOnAction(event -> {
            if (typeSelect.valueProperty().get().equals("Monte Carlo Search")) {
                // Add additional configurations
                Label policyText = new Label("Policy:");
                playerGrid.add(policyText, 0, 1);

                ChoiceBox policySelect = new ChoiceBox();
                policySelect.getItems().addAll("Random", "Rule-based");
                playerGrid.add(policySelect, 1, 1);

                Label iterNumText = new Label("# of Iters.:");
                playerGrid.add(iterNumText, 0, 2);


            }
        });

        return playerGrid;
    }
}
