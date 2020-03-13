package com.tsukiseele.koradownload.base;

import com.tsukiseele.koradownload.TaskCallback;
import com.tsukiseele.koradownload.KoraDownload;
import com.tsukiseele.koradownload.util.Util;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class DownloadTask implements Runnable, Downloadable {
	private static final int DEFAULT_RETRY_COUNT = 3;
	private static final int DEFAULT_TIMEOUT_MILLISECONDS = 10000;
	
	// 等待中
	public static final int STATE_WAIT = 4;
	// 正在开始
	public static final int STATE_START = 0;
	// 下载中
	public static final int STATE_PROGRESS = 1;
	// 下载暂停
	public static final int STATE_PAUSE = 2;
	// 下载取消
	public static final int STATE_CANCEL = 3;
	// 下载完成
	public static final int STATE_SUCCESS = 5;
	// 下载出错
	public static final int STATE_ERROR = 6;

	private Info info;
	
	private DownloadTask(Info info) {
		this.info = info;
	}
	
	/**
	 * When an object implementing interface <code>Runnable</code> is used
	 * to newTask a thread, starting the thread causes the object's
	 * <code>run</code> method to be called in that separately executing
	 * thread.
	 * <p>
	 * The general contract of the method <code>run</code> is that it may
	 * take any action whatsoever.
	 *
	 * @see Thread#run()
	 */
	@Override
	public void run() {
		OkHttpClient client = new OkHttpClient.Builder()
				.readTimeout(info.timeout, info.timeUnit)
				.connectTimeout(info.timeout, info.timeUnit)
				.build();
		Request.Builder request = new Request.Builder()
				.url(info.url)
				.headers(Headers.of(info.headers));
		// 已经存在下载进度则进行断点续传
		if (info.currentLength > 0)
			request.addHeader("Range", "bytes=" + info.currentLength + "-");

		// 开始下载
		Response response = null;
		BufferedInputStream bis = null;
		RandomAccessFile raf = null;
		try {
			notifyStart();
			if (!info.path.getParentFile().isDirectory()) 
				info.path.getParentFile().mkdirs();
			
			response = client.newCall(request.build()).execute();
			
			if (response.isSuccessful()) {
				if (info.totalLength == 0) info.totalLength = response.body().contentLength();
				
				bis = new BufferedInputStream(response.body().byteStream());
				
				raf = new RandomAccessFile(info.path, "rw");
				raf.seek(info.currentLength);
				
				byte[] buff = new byte[4096];
				int length;
				while ((length = bis.read(buff)) != -1) {
					
					raf.write(buff, 0, length);
					info.currentLength += length;
					
					if (isState(STATE_CANCEL) || isState(STATE_PAUSE)) {
						close(bis);
						close(raf);
						close(response);
						if (isState(STATE_CANCEL))
							notifyCancel();
						else
							notifyPause();
						return;
					}
					notifyUpdate();
				}
				notifySuccess();
			}
		} catch (Exception e) {
			notifyError(e);
			e.printStackTrace();
		} finally {
			close(bis);
			close(raf);
			close(response);
		}
		notifyFinish();
	}
	
	@Override
	public String toString() {
		return "url: " + info.url +
				", path: " + info.path;
	}

	@Override
	public int hashCode() {
		return info.url.hashCode();
	}
	
	public Info info() {
		return info;
	}
	
	public boolean isState(int state) {
		return info.state == state;
	}
	
	public void pause() {
		info.state = STATE_PAUSE;
	}
	
	public void cancel() {
		info.state = STATE_CANCEL;
	}
	
	private void notifyStart() {
		info.state = STATE_START;
		for (TaskCallback callback : info.callbacks)
			callback.onStart(this);
	}
	
	private void notifyPause() {
		info.state = STATE_PAUSE;
		for (TaskCallback callback : info.callbacks)
			callback.onPause(this);
	}
	
	private void notifyCancel() {
		info.state = STATE_CANCEL;
		for (TaskCallback callback : info.callbacks)
			callback.onCancel(this);
	}
	
	private void notifyUpdate() {
		info.state = STATE_PROGRESS;
		for (TaskCallback callback : info.callbacks)
			callback.onUpdate(this);
	}
	
	private void notifySuccess() {
		info.state = STATE_SUCCESS;
		for (TaskCallback callback : info.callbacks)
			callback.onSuccessful(this);
	}
	
	private void notifyError(Throwable e) {
		info.state = STATE_ERROR;
		for (TaskCallback callback : info.callbacks)
			callback.onFailed(this, e);
		if (++info.currentRetryCount < info.maxRetryCount) {
			KoraDownload.resume(this);
		}
	}
	// 任务结束时将任务移除下载队列
	private void notifyFinish() {
		// 这里不更新Finish状态
		for (TaskCallback callback : info.callbacks)
			callback.onFinish(this);
//		KoraDownload.cancel(this);
	}
	
	private static boolean close(Closeable closeable) {
		try {
			if (closeable != null) {
				closeable.close();
				return true;
			}
		} catch (IOException e) {}
		return false;
	}
	
	@Override
	public boolean isGroup() {
		return false;
	}
	
	public void execute() {
		KoraDownload.execute(this);
	}
	
	public static class Builder {
		private Info info;
		private boolean isMkdirs;
		
		public Builder(Info info) {
			this.info = info;
		}
		
		public Builder(String url) {
			info = new Info();
			info.url = url;
			// 初始化默认值
			info.timeout = DEFAULT_TIMEOUT_MILLISECONDS;
			info.timeUnit = TimeUnit.MILLISECONDS;
			info.maxRetryCount = DEFAULT_RETRY_COUNT;
		}
		
		public Builder toFile(String dir, String filename) {
			info.path = new File(dir, filename);
			return this;
		}
		
		public Builder toFile(String path) {
			info.path = new File(path);
			return this;
		}
		
		public Builder toDir(String dir) {
			info.path = new File(dir, Util.getUrlFilename(info.url));
			return this;
		}
		
		public Builder retryCount(int retryCount) {
			info.maxRetryCount = retryCount;
			return this;
		}
		
		public Builder timeout(int timeout, TimeUnit timeUnit) {
			info.timeout = timeout;
			info.timeUnit = timeUnit;
			return this;
		}
		
		public Builder timeout(int timeout) {
			info.timeout = timeout;
			return this;
		}
		
		public Builder addCallback(TaskCallback callback) {
			info.callbacks.add(callback);
			return this;
		}
		public Builder addParam(String key, String value) {
			info.params.put(key, value);
			return this;
		}
		public Builder addHeader(String key, String value) {
			info.headers.put(key, value);
			return this;
		}
		
		public Builder addHeaders(Map<String, String> headers) {
			info.headers.putAll(headers);
			return this;
		}
		
		public Builder setHeaders(Map<String, String> headers) {
			info.headers = headers;
			return this;
		}
		
		public Builder mkdirs() {
			this.isMkdirs = true;
			return this;
		}
		
		public DownloadTask build() {
			if (isMkdirs)
				info.path.getParentFile().mkdirs();
			return new DownloadTask(info);
		}
	}
	
	public static class Info implements Serializable {
		// 源文件连接
		private String url;
		// 下载路径
		private File path;
		// 当前状态
		private int state = STATE_WAIT;
		// 当前长度
		private long currentLength = 0;
		// 文件总长度
		private long totalLength;
		// 连接/读取 超时时间 (ms)
		private int timeout;
		private TimeUnit timeUnit;
		// 最大重试次数
		private int maxRetryCount;
		// 当前重试次数
		private int currentRetryCount;
		// 请求头
		private Map<String, String> headers = new HashMap<>();
		// 额外参数
		private Map<String, String> params = new HashMap<>();
		// 回调集合
		private transient List<TaskCallback> callbacks = new ArrayList<>();
		
		public Map<String, String> getParams() {
			return params;
		}
		
		public String getParam(String key) {
			return params.get(key);
		}
		
		public List<TaskCallback> getCallbacks() {
			return callbacks;
		}
		
		public String getUrl() {
			return url;
		}
		
		public File getPath() {
			return path;
		}
		
		public int getState() {
			return state;
		}
		
		public void setState(int state) {
			this.state = state;
		}
		
		public long getCurrentLength() {
			return currentLength;
		}
		
		public long getTotalLength() {
			return totalLength;
		}
		
		public int getMaxRetryCount() {
			return maxRetryCount;
		}
		
		public int getCurrentRetryCount() {
			return currentRetryCount;
		}
		
		public Map<String, String> getHeaders() {
			return headers;
		}
	}
}
