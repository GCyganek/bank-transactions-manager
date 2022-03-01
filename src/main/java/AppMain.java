import IOC.ImporterModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import controller.TransactionsManagerAppController;
import javafx.application.Application;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * In case of error "JavaFX runtime components are missing, and are required to run this application"
 * start app from Main class
 */
public class AppMain extends Application {
    private static final String DEFAULT_LOGGER_CFG_NAME = "logger.cfg.xml";

    public void run(String[] args) {
        initLogger();
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setResizable(false);
        Injector injector = Guice.createInjector(
                new ImporterModule(primaryStage)
        );

        TransactionsManagerAppController controller =
                injector.getInstance(TransactionsManagerAppController.class);
        controller.initRootLayout(injector);
    }

    private void initLogger() {
        if (System.getProperty("log4j.configurationFile") == null) {
            try {
                System.setProperty("log4j.configurationFile", this.getClass().getResource(DEFAULT_LOGGER_CFG_NAME).getPath());
            } catch (Exception ignored) {
                System.out.println("Failed to load logger configuration, using defaults.");
                return;
            }
        }

        Logger logger = LogManager.getRootLogger();
        logger.info("Using config from " + System.getProperty("log4j.configurationFile"));
    }
}
