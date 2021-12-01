package controller;

import com.google.inject.Injector;
import importer.Importer;
import io.reactivex.rxjava3.core.Observable;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.BankTransaction;
import model.DocumentType;

import javax.inject.Inject;
import java.io.IOException;

public class TransactionsManagerAppController {

    private final Stage primaryStage;
    private Importer importer;

    public TransactionsManagerAppController(Stage primaryStage, Importer importer) {
        this.primaryStage = primaryStage;
        this.importer = importer;
    }

//    @Inject
//    public void setImporter(Importer importer) {
//        this.importer = importer;
//    }

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

            Scene scene = new Scene(page);
            Stage stage = new Stage();
            stage.setTitle("New transaction");
            stage.setScene(scene);
            stage.initOwner(primaryStage);
            stage.initModality(Modality.WINDOW_MODAL);

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
}
