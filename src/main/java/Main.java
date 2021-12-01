/**
 * Workaround to fix "JavaFX runtime components are missing, and are required to run this application"
 * for reference see: https://github.com/nus-cs2103-AY2021S1/forum/issues/128
 */
public class Main {
    public static void main(String[] args) {
        new AppMain().run(args);
    }
}
