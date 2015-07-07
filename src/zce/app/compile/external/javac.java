package zce.app.compile.external;

import com.sun.tools.javac.Main;

public class javac {

	/**
	 * @param args
	 */
	@SuppressWarnings("static-access")
	public static void main(String[] args) {
		Main mm = new Main();
		mm.compile(args);
	}

}
