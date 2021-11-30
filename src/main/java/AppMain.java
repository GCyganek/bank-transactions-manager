import controller.TransactionsManagerAppController;
import javafx.application.Application;
import javafx.stage.Stage;
import session.HibernateSessionService;

public class AppMain extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        HibernateSessionService.openSession();
        TransactionsManagerAppController controller = new TransactionsManagerAppController(primaryStage);
        controller.initRootLayout();
    }
}
