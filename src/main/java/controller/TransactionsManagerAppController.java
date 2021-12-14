package controller;

import com.google.inject.Injector;
import importer.Importer;
import io.reactivex.rxjava3.core.Observable;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.BankTransaction;
import model.DocumentType;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;

public class TransactionsManagerAppController {

    private final Stage primaryStage;
    private final Importer importer;


    @Inject
    public TransactionsManagerAppController(@Named("primaryStage") Stage primaryStage, Importer importer) {
        this.primaryStage = primaryStage;
        this.importer = importer;
    }

    public void initRootLayout(Injector injector) {
        try {
            this.primaryStage.setTitle("Bank Transactions Manager");

            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(TransactionsManagerAppController.class.getResource("../view/TransactionsManagerView.fxml"));

            loader.setControllerFactory(injector::getInstance);

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

    public Observable<BankTransaction> showAddStatementView() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(TransactionsManagerViewController.class.getResource("../view/AddStatementView.fxml"));
            BorderPane page = loader.load();

            Stage stage = buildStage("New transaction", new Scene(page), primaryStage, Modality.WINDOW_MODAL);

            AddStatementViewController addStatementViewController = loader.getController();
            addStatementViewController.setStage(stage);

            stage.showAndWait();

            if (addStatementViewController.checkIfFileAvailable()) {
                return importer.importBankStatement(addStatementViewController.getBankType(), DocumentType.CSV,
                        addStatementViewController.getFile().getAbsolutePath());
            }

            return Observable.empty();

        } catch (IOException e) {
            System.out.println("Can't load new window");
            e.printStackTrace();
        }
        return Observable.empty();
    }

    public void showErrorWindow(String errorMsg, String reason) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(TransactionsManagerViewController.class.getResource("../view/ImportErrorView.fxml"));
            BorderPane page = loader.load();

            Stage stage = buildStage("Error", new Scene(page), primaryStage, Modality.WINDOW_MODAL);

            ErrorViewController errorViewController = loader.getController();
            errorViewController.setStage(stage);
            errorViewController.setErrorMessage(errorMsg, reason);

            stage.showAndWait();
        } catch (IOException e) {
            System.out.println("Can't load new window");
            e.printStackTrace();
        }
    }

    private Stage buildStage(String title, Scene scene, Stage initOwner, Modality initModality) {
        Stage stage = new Stage();
        stage.setTitle(title);
        stage.setScene(scene);
        stage.initOwner(initOwner);
        stage.initModality(initModality);
        return stage;
    }
}
