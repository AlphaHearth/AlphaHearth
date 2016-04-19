package com.github.mrdai.alphahearth.mcts;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class SimulateExecutor {
    private static final Logger LOG = LoggerFactory.getLogger(SimulateExecutor.class);

    private final int threadNum;
    private final List<Thread> threads;

    public SimulateExecutor(int threadNum) {
        this.threadNum = threadNum;
        this.threads = new ArrayList<>(threadNum);
    }

    public void execute(Runnable r) {
        if (r == null)
            throw new NullPointerException();

        if (!threads.isEmpty())
            waitToFinish();

        LOG.info("Adding new task...");
        for (int i = 0; i < threadNum; i++) {
            threads.add(new Thread(r));
            threads.get(i).start();
        }
    }

    public void waitToFinish() {
        LOG.info("Waiting for the last task to finish...");
        while (!threads.isEmpty()) {
            try {
                threads.get(0).join();
            } catch (InterruptedException e) {
                // Interrupted...
                LOG.warn("The main thread is interrupted. Interrupting all worker threads...");
                interrupt();
                threads.clear();
                return;
            }
            threads.remove(0);
        }
        LOG.info("The last task is finished.");
    }

    public void interrupt() {
        for (Thread thread : threads)
            thread.interrupt();
    }

}
