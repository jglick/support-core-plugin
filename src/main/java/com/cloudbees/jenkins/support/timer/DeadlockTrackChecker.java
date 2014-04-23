package com.cloudbees.jenkins.support.timer;

import hudson.Extension;
import hudson.model.PeriodicWork;
import jenkins.model.Jenkins;

import java.io.File;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @author Steven Chrisou
 */
@Extension
public class DeadlockTrackChecker extends PeriodicWork {

    final SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd-HHmmss");

    static final File deadLockFolder = new File(Jenkins.getInstance().getRootDir(), "/support");

    @Override
    public long getRecurrencePeriod() {
        return TimeUnit.SECONDS.toMillis(15);
    }

    @Override
    protected void doRun() throws Exception {
        ThreadMXBean mbean = ManagementFactory.getThreadMXBean();
        long[] deadLocks;
        try {
            deadLocks = mbean.findDeadlockedThreads();
        } catch (UnsupportedOperationException x) {
            deadLocks = null;
        }

        if (deadLocks != null && deadLocks.length != 0) {
            PrintWriter builder = new PrintWriter(new File(deadLockFolder,
                    "DeadlockDetected-" + format.format(new Date()) + ".txt"));
            try {
                builder.println("==============");
                builder.println("Deadlock Found");
                builder.println("==============");
                ThreadInfo[] deadLockThreads = mbean.getThreadInfo(deadLocks, Integer.MAX_VALUE);

                for (ThreadInfo threadInfo : deadLockThreads) {
                    builder.println(threadInfo);
                }
            } finally {
                builder.close();
            }
        }
    }
}
