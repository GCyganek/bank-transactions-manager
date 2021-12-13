package controller;

import com.google.inject.Injector;
import importer.Importer;
import io.reactivex.rxjava3.core.Observable;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.BankTransaction;
import model.DocumentType;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Optional;

public class TransactionsManagerAppController {

    private final Stage primaryStage;
    private final Importer importer;
    private Injector injector;

    @Inject
    public TransactionsManagerAppController(@Named("primaryStage") Stage primaryStage, Importer importer) {
        this.primaryStage = primaryStage;
        this.importer = importer;
    }

    public void initRootLayout(Injector injector) {
        try {
            this.injector = injector;

            this.primaryStage.setTitle("Bank Transactions Manager");

            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(TransactionsManagerAppController.class.getResource("../view/TransactionsManagerView.fxml"));

            fxmlLoader.setControllerFactory(injector::getInstance);

            BorderPane rootLayout = fxmlLoader.load();

            TransactionsManagerViewController controller = fxmlLoader.getController();
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
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(TransactionsManagerViewController.class.getResource("../view/AddStatementView.fxml"));
            BorderPane page = fxmlLoader.load();

            Stage stage = buildStage("New transaction", new Scene(page), primaryStage, Modality.WINDOW_MODAL);

            AddStatementViewController addStatementViewController = fxmlLoader.getController();
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
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(TransactionsManagerViewController.class.getResource("../view/ImportErrorView.fxml"));
            BorderPane page = fxmlLoader.load();

            Stage stage = buildStage("Error", new Scene(page), primaryStage, Modality.WINDOW_MODAL);

            ErrorViewController errorViewController = fxmlLoader.getController();
            errorViewController.setStage(stage);
            errorViewController.setErrorMessage(errorMsg, reason);

            stage.showAndWait();
        } catch (IOException e) {
            System.out.println("Can't load new window");
            e.printStackTrace();
        }
    }

    public Optional<BigDecimal> showEditTransactionWindow(BankTransaction bankTransaction) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(TransactionsManagerAppController.class.getResource("../view/EditTransactionView.fxml"));

            fxmlLoader.setControllerFactory(injector::getInstance);

            AnchorPane page = fxmlLoader.load();

            Stage stage = buildStage("Edit transaction", new Scene(page), primaryStage, Modality.WINDOW_MODAL);

            EditTransactionViewController editTransactionViewController = fxmlLoader.getController();
            editTransactionViewController.setStage(stage);
            editTransactionViewController.setData(bankTransaction);

            stage.showAndWait();

            return Optional.of(editTransactionViewController.getFinalAmount());

        } catch (IOException e) {
            e.printStackTrace();
        }

        return Optional.empty();
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
