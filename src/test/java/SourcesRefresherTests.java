import io.reactivex.rxjava3.observers.TestObserver;
import javafx.collections.ObservableList;
import model.util.BankType;
import model.util.SourceType;
import org.junit.jupiter.api.*;
import watcher.SourceObserver;
import watcher.SourceUpdate;
import watcher.SourcesRefresher;
import watcher.builder.SourceObserverBuilder;
import watcher.exceptions.DuplicateSourceException;
import watcher.exceptions.InvalidSourceConfigException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class SourcesRefresherTests {

    private SourcesRefresher sourcesRefresher;
    private static SourceObserver directoryObserver;

    private static final Path observedDirectoryPath = Path.of(System.getProperty("user.dir") + "/santanderTestDir");


    @BeforeAll
    public static void initDirectoryObserver() throws IOException, InvalidSourceConfigException {
        Files.createDirectory(observedDirectoryPath);
        directoryObserver = SourceObserverBuilder.with()
                .withSourceType(SourceType.DIRECTORY)
                .withBankType(BankType.SANTANDER)
                .withDescription(observedDirectoryPath.toString())
                .build();
    }

    @BeforeEach
    public void initSourcesRefresher() {
        this.sourcesRefresher = new SourcesRefresher();
    }

    @AfterEach
    public void deleteObservedDirectoryFiles() throws IOException {
        Files.walk(observedDirectoryPath).map(Path::toFile).filter(File::isFile).forEach(File::delete);
    }

    @AfterAll
    public static void deleteObservedDirectory() throws IOException {
        Files.delete(observedDirectoryPath);
    }

    @Test
    void updatesAreNotFetchedFromInactiveDirectorySource() throws DuplicateSourceException, IOException {
        directoryObserver.setActive(false);
        sourcesRefresher.addSourceObserver(directoryObserver);
        Files.createFile(Path.of(observedDirectoryPath + "/test.csv"));
        TestObserver<SourceUpdate> test = sourcesRefresher.getUpdates().test();

        test.assertComplete();
        test.assertNoErrors();
        test.assertValueCount(0);
    }

    @Test
    void updatesAreFetchedFromActiveDirectorySource() throws DuplicateSourceException, IOException {
        sourcesRefresher.addSourceObserver(directoryObserver);
        Files.createFile(Path.of(observedDirectoryPath + "/test.csv"));
        TestObserver<SourceUpdate> test = sourcesRefresher.getUpdates().test();

        test.assertComplete();
        test.assertNoErrors();
        test.assertValueCount(1);
        List<SourceUpdate> values = test.values();

        assertEquals(directoryObserver, values.get(0).getSourceObserver());
    }

    @Test
    void cacheIsClearedWhenSourceIsRemoved() throws InterruptedException, IOException, DuplicateSourceException {
        sourcesRefresher.addSourceObserver(directoryObserver);
        Files.createFile(Path.of(observedDirectoryPath + "/test.csv"));
        sourcesRefresher.startPeriodicalUpdateChecks(100, TimeUnit.MILLISECONDS);

        Thread.sleep(500);

        sourcesRefresher.stopPeriodicalUpdateChecks();
        ObservableList<SourceUpdate> availableUpdates = sourcesRefresher.getAvailableUpdates();
        assertEquals(1, availableUpdates.size());

        sourcesRefresher.removeSourceObserver(directoryObserver);
        assertTrue(sourcesRefresher.getSourceObservers().isEmpty());

        availableUpdates = sourcesRefresher.getAvailableUpdates();
        assertTrue(availableUpdates.isEmpty());
    }

    @Test
    void cacheIsClearedWhenUpdatesAreRequested() throws DuplicateSourceException, IOException, InterruptedException {
        sourcesRefresher.addSourceObserver(directoryObserver);
        Files.createFile(Path.of(observedDirectoryPath + "/test.csv"));
        sourcesRefresher.startPeriodicalUpdateChecks(100, TimeUnit.MILLISECONDS);

        Thread.sleep(500);

        sourcesRefresher.stopPeriodicalUpdateChecks();
        ObservableList<SourceUpdate> availableUpdates = sourcesRefresher.getAvailableUpdates();
        assertEquals(1, availableUpdates.size());

        TestObserver<SourceUpdate> getUpdatesTest = sourcesRefresher.getCachedSourceUpdates().test();
        getUpdatesTest.assertComplete();
        getUpdatesTest.assertValueCount(1);

        availableUpdates = sourcesRefresher.getAvailableUpdates();
        assertTrue(availableUpdates.isEmpty());
    }

    @Test
    void duplicateSourceCantBeAdded() throws DuplicateSourceException {
        sourcesRefresher.addSourceObserver(directoryObserver);
        assertThrows(DuplicateSourceException.class, () -> sourcesRefresher.addSourceObserver(directoryObserver));
    }
}
