package com.tsukiseele.koradownload;

import com.tsukiseele.koradownload.base.DownloadTask;

public abstract class SimpleTaskCallback implements TaskCallback {
	private final int KEEP_TIME = 500;
	private long updateTime;
	
	@Override
	public void onPause(DownloadTask task) {
	
	}
	
	@Override
	public void onCancel(DownloadTask task) {
	
	}
	
	@Override
	public void onStart(DownloadTask task) {
	
	}

	@Override
	public final void onUpdate(DownloadTask task) {
		if (System.currentTimeMillis() - updateTime > KEEP_TIME) {
			updateTime = System.currentTimeMillis();
			onProgress(task);
		}
	}
	
	public void onProgress(DownloadTask task) {
	
	}
	
	@Override
	public void onFinish(DownloadTask task) {
	
	}
}
