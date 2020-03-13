package com.tsukiseele.koradownload.base;

import com.tsukiseele.koradownload.KoraDownload;
import com.tsukiseele.koradownload.SimpleTaskCallback;
import com.tsukiseele.koradownload.TaskGroupCallback;

import java.util.ArrayList;
import java.util.List;

public class DownloadTasks implements Downloadable {
	// 所有任务集合
	private List<DownloadTask> tasks = new ArrayList<>();
	// 失败任务集合
	private List<DownloadTask> failedTasks = new ArrayList<>();
	// 当前任务组的监听
	private List<TaskGroupCallback> callbacks = new ArrayList<>();
	// 每个子任务所注入的监听回调
	private SimpleTaskCallback injectCallback;
	
	private int finishCount = 0;
	
	private SimpleTaskCallback getCallback() {
		if (injectCallback == null)
			injectCallback = new SimpleTaskCallback() {
				@Override
				public void onFailed(DownloadTask task, Throwable e) {
					failedTasks.add(task);
				}
				
				@Override
				public void onSuccessful(DownloadTask task) {
					finishCount++;
				}
				
				@Override
				public void onFinish(DownloadTask task) {
					for (TaskGroupCallback callback : callbacks)
						callback.onUpdate(task, finishCount, failedTasks);
					
					if (tasks.size() == finishCount + failedTasks.size())
						for (TaskGroupCallback callback : callbacks)
							callback.onSuccessful(failedTasks);
				}
			};
		return injectCallback;
	}
	
	public void addCallback(TaskGroupCallback callback) {
		for (DownloadTask task : tasks) {
			// 初始化任务组回调
			task.info().getCallbacks().add(getCallback());
		}
		this.callbacks.add(callback);
	}
	
	public void addTask(DownloadTask task) {
		tasks.add(task);
	}
	
	public DownloadTasks() {}
	
	public DownloadTasks(List<DownloadTask> tasks) {
		this.tasks = tasks;
	}
	
	public List<DownloadTask> getAll() {
		return tasks;
	}
	
	public DownloadTask get(int index) {
		return tasks.get(index);
	}
	
	public int size() {
		return tasks.size();
	}
	
	public void execute() {
		for (DownloadTask task : tasks) {
			KoraDownload.execute(task);
		}
	}
	
	@Override
	public boolean isGroup() {
		return true;
	}
}
