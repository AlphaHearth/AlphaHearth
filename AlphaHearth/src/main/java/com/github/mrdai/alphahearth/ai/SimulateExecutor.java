package com.github.mrdai.alphahearth.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimulateExecutor {
    private static final Logger LOG = LoggerFactory.getLogger(SimulateExecutor.class);

    private final int threadNum;
    private final Thread[] threads;

    public SimulateExecutor(int threadNum) {
        this.threadNum = threadNum;
        this.threads = new Thread[threadNum];
    }


}
