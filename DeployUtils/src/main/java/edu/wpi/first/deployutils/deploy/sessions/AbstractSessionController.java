package edu.wpi.first.deployutils.deploy.sessions;

import java.util.concurrent.Semaphore;

import javax.inject.Inject;

import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import edu.wpi.first.deployutils.deploy.StorageService;

public abstract class AbstractSessionController implements SessionController {
    private Semaphore semaphore;
    private Logger log;
    private int semI;

    @Inject
    public AbstractSessionController(int maxConcurrent, StorageService storage) {
        if (storage != null) {
            storage.addSessionForCleanup(this);
        }
        semaphore = new Semaphore(maxConcurrent);
        semI = 0;
    }

    protected int acquire() {
        int sem = semI++;
        getLogger().debug("Acquiring Semaphore " + sem + " (" + semaphore.availablePermits() + " available)");
        long before = System.currentTimeMillis();
        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        long time = System.currentTimeMillis() - before;
        getLogger().debug("Semaphore " + sem + " acquired (took " + time + "ms)");
        return sem;
    }

    protected void release(int sem) {
        semaphore.release();
        log.debug("Semaphore " + sem + " released");
    }

    protected Logger getLogger() {
        if (log == null) log = Logging.getLogger(toString());
        return log;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "[]";
    }
}
