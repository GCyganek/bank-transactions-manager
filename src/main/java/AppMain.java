import IOC.ImporterModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import controller.TransactionsManagerAppController;
import importer.Importer;
import javafx.application.Application;
import javafx.stage.Stage;


/**
 * In case of "JavaFX runtime components are missing, and are required to run this application" error
 * start app from Main class
 */
public class AppMain extends Application {

    public void run(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        Injector injector = Guice.createInjector(
                new ImporterModule()
        );

        TransactionsManagerAppController controller = new TransactionsManagerAppController(primaryStage,
                injector.getInstance(Importer.class));
        controller.initRootLayout(injector);
    }
}
