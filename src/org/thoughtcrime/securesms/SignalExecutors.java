package org.thoughtcrime.securesms;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class SignalExecutors {

  public static final Executor DATABASE = newBoundedCachedThreadPool(1);

  private static Executor newBoundedCachedThreadPool(int maxThreads) {
    ThreadPoolExecutor executor = new ThreadPoolExecutor(maxThreads, maxThreads, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
    executor.allowCoreThreadTimeOut(true);
    return executor;
  }
}
