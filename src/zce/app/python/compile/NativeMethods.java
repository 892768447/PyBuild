package zce.app.python.compile;

public class NativeMethods {

	static {
		System.loadLibrary("compile");
	}

	public native String encrypt(String data);

	public native String decrypt(String data);

}
