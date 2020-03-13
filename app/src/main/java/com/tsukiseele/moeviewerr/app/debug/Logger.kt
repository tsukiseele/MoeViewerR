package com.tsukiseele.moeviewerr.app.debug

class Logger/*
	public static enum Level {
		VERBOSE(0),
		DEBUG(1),
		INFO(2),
		WARN(3),
		ERROR(4),
		CLOSE(5);

		private int level;
		private Level(int level) {
			this.level = level;
		}
		public int getLevel() {
			return level;
		}
	}
	private static Level level = level.VERBOSE;
	public static Level getLevel() {
		return level;
	}
	public static void setLevel(Level le) {
		level = le;
	}
	public static void v(String tag, String message) {
		if (level.getLevel() < 1) {
			Log.v(tag, message);
		}
	}
	public static void d(String tag, String message) {
		if (level.getLevel() < 2) {
			Log.d(tag, message);
		}
	}
	public static void i(String tag, String message) {
		if (level.getLevel() < 3) {
			Log.i(tag, message);
		}
	}
	public static void w(String tag, String message) {
		if (level.getLevel() < 4) {
			Log.w(tag, message);
		}
	}
	public static void e(String tag, String message) {
		if (level.getLevel() < 5) {
			Log.e(tag, message);
		}
	}
	public static void e(Exception e) {
		if (level.getLevel() < 5) {
			Log.e(e.toString(), e.getMessage());
		}
	}
	public static void showException(Context context, Exception e) {
		ToastUtil.makeText(context, context.getClass().getName() + ":\n" + e.getMessage(), Toast.LENGTH_LONG).show();
	}
	public static void showException(Context context, Exception e, String message) {
		ToastUtil.makeText(context, message + ":\n" + context.getClass().getName() + ":\n" + e.getMessage(), Toast.LENGTH_LONG).show();
	}*//*
	public static boolean printDebugLog(String tag, String message) {
		FileOutputStream fileOut = null;
		PrintStream debugLogWrite = null;
		try {
			fileOut = new FileOutputStream(Const.DEBUG_LOG_ABS_PATH, true);
			debugLogWrite = new PrintStream(fileOut, true, "UTF_8");
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS z");
			debugLogWrite.printf("%s\nTag : %s\nMsg : %s\n", dateFormat.format(new Date()), tag, message);
		} catch (FileNotFoundException e) {
			Logger.e(e);
		} catch (UnsupportedEncodingException e) {
			Logger.e(e);
		} finally {
			try {
				debugLogWrite.close();
				fileOut.close();
			}
			catch (IOException e) {
				Logger.e(e);
			}
		}
		return true;
	}*/
