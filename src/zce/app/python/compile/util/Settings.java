package zce.app.python.compile.util;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class Settings {

	/*
	 * 程序名
	 */
	public static String APPNAME = "APPNAME";
	public static String appName = "";
	/**
	 * 包名
	 */
	public static String PACKAGENAME = "PACKAGENAME";
	public static String packageName = "";
	/**
	 * 版本号
	 */
	public static String VERSIONCODE = "VERSIONCODE";
	public static String versionCode = "";
	/**
	 * 版本名
	 */
	public static String VERSIONNAME = "VERSIONNAME";
	public static String versionName = "";
	/**
	 * 图标路径
	 */
	public static String ICONPATH = "ICONPATH";
	public static String iconPath = "";
	/**
	 * 待打包文件或文件夹
	 */
	public static String PROJECTPATH = "PROJECTPATH";
	public static String projectPath = "";

	public static void saveSettings(SharedPreferences sharedPreferences,
			String key, String value) {
		Editor editor = sharedPreferences.edit();
		editor.putString(key, value);
		editor.commit();
	}

	public static void getSettings(SharedPreferences sharedPreferences) {
		appName = sharedPreferences.getString(APPNAME, "");
		packageName = sharedPreferences.getString(PACKAGENAME, "");
		versionCode = sharedPreferences.getString(VERSIONCODE, "1");
		versionName = sharedPreferences.getString(VERSIONNAME, "1.0");
		iconPath = sharedPreferences.getString(ICONPATH, "");
		projectPath = sharedPreferences.getString(PROJECTPATH, "");
	}

}
