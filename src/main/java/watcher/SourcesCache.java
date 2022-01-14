package watcher;

import model.util.BankType;

import java.util.Map;
import java.util.Set;

public class SourcesCache {
    private final Map<BankType, Set<String>> foldersCache;
    private final Map<BankType, Set<String>> remotesCache;

    public SourcesCache(Map<BankType, Set<String>> foldersCache, Map<BankType, Set<String>> remotesCache) {
        this.foldersCache = foldersCache;
        this.remotesCache = remotesCache;
    }
}
