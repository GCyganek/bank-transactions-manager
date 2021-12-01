package controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;

public class TransactionsManagerAppController {

    private final Stage primaryStage;

    public TransactionsManagerAppController(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public void initRootLayout() {
        try {
            this.primaryStage.setTitle("Bank Transactions Manager");

            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(TransactionsManagerAppController.class.getResource("../view/TransactionsManagerView.fxml"));
            BorderPane rootLayout = loader.load();

            TransactionsManagerViewController controller = loader.getController();
            controller.setAppController(this);
            controller.fetchDataFromDatabase();

            Scene scene = new Scene(rootLayout);
            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
