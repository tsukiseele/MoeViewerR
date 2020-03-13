package com.tsukiseele.koradownload;

import com.tsukiseele.koradownload.base.DownloadTask;

import java.util.List;

public interface TaskGroupCallback {
	void onUpdate( DownloadTask task, int finishCount, List<DownloadTask> failedTask);
	void onSuccessful(List<DownloadTask> failedTasks);
}
