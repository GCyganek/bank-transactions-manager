import IOC.ImporterModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import controller.TransactionsManagerAppController;
import javafx.application.Application;
import javafx.stage.Stage;


/**
 * In case of error "JavaFX runtime components are missing, and are required to run this application"
 * start app from Main class
 */
public class AppMain extends Application {

    public void run(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        Injector injector = Guice.createInjector(
                new ImporterModule(primaryStage)
        );

        TransactionsManagerAppController controller =
                injector.getInstance(TransactionsManagerAppController.class);
        controller.initRootLayout(injector);
    }
}
