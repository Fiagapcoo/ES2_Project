package com.es2.project;

import java.util.ArrayList;

public class AppStateBackupService extends Object{
    AppStateManager stateManager;
    ArrayList<AppState> snapshots = new ArrayList<>();

    public AppStateBackupService(AppStateManager stateManager) {
        this.stateManager = stateManager;
    }

    public void takeSnapshot() {
        AppState snapshot = stateManager.saveState();
        snapshots.add(snapshot);
    }

    public void restoreSnapshot(int index, StorageManager storageManager) throws Exception {
        if (index < 0 || index >= snapshots.size()) {
            throw new Exception("Snapshot não existe!");
        }
        AppState snapshot = snapshots.get(index);
        stateManager.restore(snapshot, storageManager);
    }

    public int totalSnapshots() {
        return snapshots.size();
    }
}
