package controller;

import com.google.inject.Injector;
import controller.sources.SourceAdditionWindowController;
import controller.sources.TransactionSourcesViewController;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.BankTransaction;
import model.util.SourceType;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Optional;

public class TransactionsManagerAppController {

    private final Stage primaryStage;
    private Injector injector;
    private final TransactionSourcesViewController transactionSourcesViewController;

    @Inject
    public TransactionsManagerAppController(@Named("primaryStage") Stage primaryStage,
                                            TransactionSourcesViewController transactionSourcesViewController)
    {
        this.primaryStage = primaryStage;
        this.transactionSourcesViewController = transactionSourcesViewController;
        this.transactionSourcesViewController.setAppController(this);

        primaryStage.setOnCloseRequest(t -> {
            Platform.exit();
            System.exit(0);
        });
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

            Scene scene = new Scene(rootLayout);
            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (IOException e) {
            showErrorWindow("Can't load new window", e.getMessage());
        }
    }
    public Optional<SourceAdditionWindowController> showAddSourceWindow(SourceType sourceType) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            Stage stage = buildStage(
                    fxmlLoader, "Add source", getFxmlViewFilename(sourceType),
                    primaryStage, Modality.APPLICATION_MODAL
            );
            SourceAdditionWindowController sourceAdditionWindowController = fxmlLoader.getController();
            sourceAdditionWindowController.setStage(stage);
            sourceAdditionWindowController.setAppController(this);

            stage.showAndWait();

            return Optional.of(sourceAdditionWindowController);
        } catch (IOException e) {
            showErrorWindow("Can't load new window", e.getMessage());
        }
        return Optional.empty();
    }

    private String getFxmlViewFilename(SourceType sourceType) {
        return switch (sourceType) {
            case REST_API -> "AddRemoteSourceWindow.fxml";
            case DIRECTORY -> "AddDirectorySourceWindow.fxml";
        };
    }

    public void showTransactionSourcesWindow() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            Stage stage = buildStage(
                    fxmlLoader, "Transaction sources manager", "TransactionSourcesWindow.fxml",
                    primaryStage, Modality.WINDOW_MODAL
            );
            TransactionSourcesViewController transactionSourcesViewController = fxmlLoader.getController();
            transactionSourcesViewController.setStage(stage);

            stage.showAndWait();
        } catch (IOException e) {
            showErrorWindow("Can't load new window", e.getMessage() + " " + e.getCause());
        }
    }

    public AddStatementViewController showAddStatementView() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader();
        Stage stage = buildStage(fxmlLoader, "New transaction", "AddStatementView.fxml", primaryStage, Modality.WINDOW_MODAL);

        AddStatementViewController addStatementViewController = fxmlLoader.getController();
        addStatementViewController.setStage(stage);

        stage.showAndWait();

        return addStatementViewController;
    }

    public void showStatisticsView() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            Stage stage = buildStage(fxmlLoader, "Statistics", "StatisticsView.fxml", primaryStage, Modality.WINDOW_MODAL);

            StatisticsViewController statisticsViewController = fxmlLoader.getController();
            statisticsViewController.setStage(stage);
            statisticsViewController.showData();

            stage.showAndWait();
        } catch (IOException e) {
            showErrorWindow("Can't load new window", e.getMessage());
        }
    }

    public void showErrorWindow(String errorMsg, String reason) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            Stage stage = buildStage(fxmlLoader, "Error", "ErrorView.fxml", primaryStage, Modality.APPLICATION_MODAL);

            ErrorViewController errorViewController = fxmlLoader.getController();
            errorViewController.setStage(stage);
            errorViewController.setErrorMessage(errorMsg, reason);

            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Optional<BigDecimal> showEditTransactionWindow(BankTransaction bankTransaction) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            Stage stage = buildStage(fxmlLoader,"Edit transaction", "EditTransactionView.fxml",
                    primaryStage, Modality.WINDOW_MODAL);

            EditTransactionViewPresenter editTransactionViewPresenter = fxmlLoader.getController();
            editTransactionViewPresenter.setStage(stage);
            editTransactionViewPresenter.setData(bankTransaction);
            editTransactionViewPresenter.setAppController(this);

            stage.showAndWait();

            if (editTransactionViewPresenter.isEditApproved()) {
                return Optional.of(editTransactionViewPresenter.getFinalAmount());
            }

        } catch (IOException e) {
            showErrorWindow("Can't load new window", e.getMessage());
        }

        return Optional.empty();
    }


    private Stage buildStage(FXMLLoader fxmlLoader, String title, String viewPath,
                             Stage initOwner, Modality initModality) throws IOException
    {
        fxmlLoader.setLocation(TransactionsManagerAppController.class.getResource("../view/" + viewPath));
        fxmlLoader.setControllerFactory(injector::getInstance);
        Scene stageScene = new Scene(fxmlLoader.load());

        Stage stage = new Stage();
        stage.setTitle(title);
        stage.setScene(stageScene);
        stage.initOwner(initOwner);
        stage.initModality(initModality);
        return stage;
    }
}
