package com.tsukiseele.koradownload;

import com.tsukiseele.koradownload.base.DownloadTask;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.List;
import java.util.ArrayList;

public class KoraDownload {
	// 任务集合
	private final static Map<Integer, DownloadTask> tasks = new HashMap<>();
	
	// 线程池
//	private final static ThreadPoolExecutor threadPool = new ThreadPoolExecutor(3, Integer.MAX_VALUE, 5L, TimeUnit.SECONDS, new LinkedBlockingDeque<>());
	private final static ThreadPoolExecutor threadPool = new ThreadPoolExecutor(3, Integer.MAX_VALUE, 5L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

	public static synchronized boolean execute(DownloadTask downloadTask) {
		int id = downloadTask.hashCode();
		if (!tasks.containsKey(id)) {
			tasks.put(id, downloadTask);
			threadPool.execute(downloadTask);
			return true;
		}
		return false;
	}
	
	public static void pause(String url) {
		int id = url.hashCode();
		if (tasks.containsKey(id)) {
			tasks.get(id).pause();
		}
	}
	
	public static void pause(DownloadTask task) {
		pause(task.info().getUrl());
	}
	
	public static boolean resume(String url) {
		int id = url.hashCode();
		if (tasks.containsKey(id)) {
			threadPool.execute(tasks.get(id));
			return true;
		}
		return false;
	}
	
	public static void resume(DownloadTask task) {
		resume(task.info().getUrl());
	}
	
	public static void restart(DownloadTask downloadTask) {
		cancel(downloadTask);
		execute(downloadTask);
	}
	
	public static synchronized void cancel(DownloadTask downloadTask) {
		int id = downloadTask.hashCode();
		if (tasks.containsKey(id)) {
			downloadTask.cancel();
			threadPool.remove(downloadTask);
			tasks.remove(id);
		}
	}
	
	public static ThreadPoolExecutor getThreadPool() {
		return threadPool;
	}
	
	public static DownloadTask.Builder newTask(String url) {
		return new DownloadTask.Builder(url);
	}
	
	public static List<DownloadTask> getTasks() {
		return new ArrayList<>(tasks.values());
	}
}
