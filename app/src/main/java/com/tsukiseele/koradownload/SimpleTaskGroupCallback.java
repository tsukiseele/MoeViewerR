package com.tsukiseele.koradownload;
import com.tsukiseele.koradownload.base.DownloadTask;
import java.util.List;

public class SimpleTaskGroupCallback implements TaskGroupCallback {

	@Override
	public final void onUpdate(DownloadTask task, int finishCount, List<DownloadTask> failedTask) {
		onProgress(task, finishCount, failedTask);
	}
	
	public final void onProgress(DownloadTask task, int finishCount, List<DownloadTask> failedTask) {
		
	}

	@Override
	public void onSuccessful(List<DownloadTask> failedTasks) {
		
	}
}
