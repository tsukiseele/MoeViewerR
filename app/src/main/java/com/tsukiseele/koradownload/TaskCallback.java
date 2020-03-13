package com.tsukiseele.koradownload;

import com.tsukiseele.koradownload.base.DownloadTask;

public interface TaskCallback {
	void onFailed(DownloadTask task, Throwable e);
	void onSuccessful(DownloadTask task);
	void onPause(DownloadTask task);
	void onCancel(DownloadTask task);
	void onStart(DownloadTask task);
	void onUpdate(DownloadTask task);
	void onFinish(DownloadTask task);
}
