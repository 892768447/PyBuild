/**
 * @author zce
 */
package zce.app.python.compile.util;

public class Log {

	private Log() {
	}

	private static String getTag() {
		StackTraceElement[] stackTraceElements = Thread.currentThread()
				.getStackTrace();
		String fullClassName = stackTraceElements[4].getClassName();
		String className = fullClassName.substring(fullClassName
				.lastIndexOf(".") + 1);
		int lineNumber = stackTraceElements[4].getLineNumber();
		return className + ":" + lineNumber;
	}

	public static void v(String message) {
		android.util.Log.v(getTag(), message);
	}

	public static void e(String message) {
		android.util.Log.e(getTag(), message);
	}

	public static void w(String message) {
		android.util.Log.w(getTag(), message);
	}

	public static void d(String message) {
		android.util.Log.d(getTag(), message);
	}

	public static void i(String message) {
		android.util.Log.i(getTag(), message);
	}
}
