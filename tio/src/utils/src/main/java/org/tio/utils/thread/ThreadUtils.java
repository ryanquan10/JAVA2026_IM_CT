/*
 * njmfpvbwaiow本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动pjgsyn
 */
package org.tio.utils.thread;

import java.util.concurrent.CompletionService;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

import cn.hutool.core.thread.ExecutorBuilder;
import cn.hutool.core.thread.GlobalThreadPool;
import cn.hutool.core.thread.ThreadUtil;

/**
 * 
 * @author tanyaowu 2017年10月19日 上午9:41:46
 */
public class ThreadUtils {
    public static String stackTrace() {
	StackTraceElement[] elements = Thread.currentThread().getStackTrace();
	StringBuilder buf = new StringBuilder();
	for (StackTraceElement element : elements) {
	    buf.append("\r\n	").append(element.getClassName()).append(".").append(element.getMethodName())
		    .append("(").append(element.getFileName()).append(":").append(element.getLineNumber()).append(")");
	}
	return buf.toString();
    }

    public static ExecutorService newExecutor(int corePoolSize) {
	ExecutorBuilder builder = ExecutorBuilder.create();
	if (corePoolSize > 0) {
	    builder.setCorePoolSize(corePoolSize);
	}
	return builder.build();
    }

    public static ExecutorService newExecutor() {
	return ExecutorBuilder.create().useSynchronousQueue().build();
    }

    public static ThreadPoolExecutor newExecutorByBlockingCoefficient(float blockingCoefficient) {
	if (blockingCoefficient >= 1 || blockingCoefficient < 0) {
	    throw new IllegalArgumentException("[blockingCoefficient] must between 0 and 1, or equals 0.");
	}

	// 最佳的线程数 = CPU可用核心数 / (1 - 阻塞系数)
	int poolSize = (int) (Runtime.getRuntime().availableProcessors() / (1 - blockingCoefficient));
	return ExecutorBuilder.create().setCorePoolSize(poolSize).setMaxPoolSize(poolSize).setKeepAliveTime(0L).build();
    }

    public static void timeExe(TimeHandler th, long delay, long time) {
	ThreadUtil.execAsync(new TioRun() {
	    public void run() {
		try {
		    Thread.sleep(delay);
		    while (true) {
			try {
			    th.handler();
			} catch (Exception e) {
			}
			Thread.sleep(time);
		    }
		} catch (InterruptedException e) {
		}
	    }
	}, true);

    }

    public static <T> CompletionService<T> newCompletionService() {
	return new ExecutorCompletionService<>(GlobalThreadPool.getExecutor());
    }

    public static <T> CompletionService<T> newCompletionService(ExecutorService executor) {
	return new ExecutorCompletionService<>(executor);
    }

    public static interface TimeHandler {
	public void handler();
    }

    /**
     * 新建一个CountDownLatch，一个同步辅助类，在完成一组正在其他线程中执行的操作之前，它允许一个或多个线程一直等待。
     *
     * @param threadCount 线程数量
     * @return CountDownLatch
     */
    public static CountDownLatch newCountDownLatch(int threadCount) {
	return new CountDownLatch(threadCount);
    }

    /**
     * 创建新线程，非守护线程，正常优先级，线程组与当前线程的线程组一致
     *
     * @param runnable {@link Runnable}
     * @param name     线程名
     * @return {@link Thread}
     * @since 3.1.2
     */
    public static Thread newThread(Runnable runnable, String name) {
	final Thread t = newThread(runnable, name, false);
	if (t.getPriority() != Thread.NORM_PRIORITY) {
	    t.setPriority(Thread.NORM_PRIORITY);
	}
	return t;
    }

    /**
     * 创建新线程
     *
     * @param runnable {@link Runnable}
     * @param name     线程名
     * @param isDaemon 是否守护线程
     * @return {@link Thread}
     * @since 4.1.2
     */
    public static Thread newThread(Runnable runnable, String name, boolean isDaemon) {
	final Thread t = new Thread(null, runnable, name);
	t.setDaemon(isDaemon);
	return t;
    }

}
