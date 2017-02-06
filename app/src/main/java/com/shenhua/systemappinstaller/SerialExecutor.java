package com.shenhua.systemappinstaller;

import java.util.Queue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 序列执行器
 * Created by shenhua on 2/6/2017.
 * Email shenhuanet@126.com
 */
public class SerialExecutor {

    final Queue<Runnable> tasks = new LinkedBlockingQueue<>();
    final Executor executor;
    Runnable active;

    public SerialExecutor(Executor executor) {
        this.executor = executor;
    }

    public void addRun(Runnable runnable) {
        tasks.add(runnable);
    }

    public synchronized void execute(final Runnable runnable) {
        try {
            runnable.run();
        } finally {
            scheduleNext();
        }
    }

    protected void scheduleNext() {
        if ((active = tasks.poll()) != null) {
            this.execute(active);
        }
    }
}
