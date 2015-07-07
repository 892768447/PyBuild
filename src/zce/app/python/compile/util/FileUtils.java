/*
 * Copyright (C) 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package zce.app.python.compile.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.OptionalDataException;
import java.io.OutputStreamWriter;
import java.io.StreamCorruptedException;
import java.lang.reflect.Method;
import java.nio.channels.FileChannel;
import java.util.HashMap;

import zce.app.python.compile.MainActivity.BuildTask;

//import zce.app.python.compile.util.Log;

/**
 * 
 * @author ざ凍結の→愛
 */
public class FileUtils {

	// private static final String TAG = "FileUtils";

	private FileUtils() {
		// Utility class.
	}

	public static void toSaveFile(File file, byte[] buffer) {
		try {
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(buffer);
			fos.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	public static HashMap<String, String> readCommand(InputStream input) {
		try {
			HashMap<String, String> map = new HashMap<String, String>();
			ObjectInputStream objectInputStream = new ObjectInputStream(input);
			map = (HashMap<String, String>) objectInputStream.readObject();
			objectInputStream.close();
			return map;
		} catch (OptionalDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (StreamCorruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static byte[] toByteArray(InputStream in) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		BufferedInputStream bis = null;
		byte[] bytes = null;
		try {
			bis = new BufferedInputStream(in);
			int buf_size = 1024;
			byte[] buffer = new byte[buf_size];
			int len = 0;
			while ((len = bis.read(buffer, 0, buf_size)) != -1) {
				bos.write(buffer, 0, len);
			}
			bytes = bos.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				bis.close();
				bos.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return bytes;
	}

	public static int chmod(File path, int mode) throws Exception {
		Class<?> fileUtils = Class.forName("android.os.FileUtils");
		Method setPermissions = fileUtils.getMethod("setPermissions",
				String.class, int.class, int.class, int.class);
		return (Integer) setPermissions.invoke(null, path.getAbsolutePath(),
				mode, -1, -1);
	}

	/**
	 * 
	 * @param src
	 *            the source file
	 * @param dst
	 *            the destination file
	 * @return if the copy was successfull
	 */
	public static boolean copyFile(File src, File dst) {

		FileChannel inChannel = null;
		FileChannel outChannel = null;

		boolean complete = true;

		try {
			inChannel = new FileInputStream(src).getChannel();
			outChannel = new FileOutputStream(dst).getChannel();
		} catch (FileNotFoundException e) {
			// Log.w("AndroidLib: " + "File not found or no R/W permission"
			// + e.getMessage());
			complete = false;
		}

		try {
			inChannel.transferTo(0, inChannel.size(), outChannel);
		} catch (Exception e) {
			// Log.w("AndroidLib: " + "Error during copy" + e.getMessage());
			complete = false;
		}

		try {
			if (inChannel != null) {
				inChannel.close();
			}
			if (outChannel != null) {
				outChannel.close();
			}
		} catch (IOException e) {
			// Log.w("AndroidLib: " + "Error when closing files" +
			// e.getMessage());
			complete = false;
		}

		return complete;
	}

	/**
	 * @param path
	 *            the absolute path to the file to save
	 * @param text
	 *            the text to write
	 * @return if the file was saved successfully
	 */
	public static boolean writeTextFile(String path, String text) {
		File file = new File(path);
		OutputStreamWriter writer;
		BufferedWriter out;
		String eol_text = text;
		try {
			writer = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
			out = new BufferedWriter(writer);
			out.write(eol_text);
			out.close();
		} catch (OutOfMemoryError e) {
			// Log.w(TAG + ": Out of memory error" + e.getMessage());
			return false;
		} catch (IOException e) {
			// Log.w(TAG + ": Can't write to file " + path + e.getMessage());
			return false;
		}
		return true;
	}

	/**
	 * @param file
	 *            the file to read
	 * @return the content of the file as text
	 */
	public static String readTextFile(File file) {
		InputStreamReader reader;
		BufferedReader in;
		StringBuffer text = new StringBuffer();
		int c;
		try {
			reader = new InputStreamReader(new FileInputStream(file), "UTF-8");
			in = new BufferedReader(reader);
			do {
				c = in.read();
				if (c != -1) {
					text.append((char) c);
				}
			} while (c != -1);
			in.close();
		} catch (IOException e) {
			// Log.w(TAG + ": Can't read file " + file.getName() +
			// e.getMessage());
			return null;
		} catch (OutOfMemoryError e) {
			// Log.w(TAG + ": File is to big to read" + e.getMessage());
			return null;
		}

		// detect end of lines
		String content = text.toString();
		int windows = content.indexOf("\r\n");
		int macos = content.indexOf("\r");

		if (windows != -1) {
			content = content.replaceAll("\r\n", "\n");
		} else {
			if (macos != -1) {
				content = content.replaceAll("\r", "\n");
			} else {
			}
		}
		return content;
	}

	/**
	 * 删除之前的项目
	 * 
	 * @param file
	 */
	public static void deleteDir(BuildTask buildTask, File file) {
		if (file == null || !file.exists()) {
			return;
		}
		boolean success = true;
		for (File j_file : file.listFiles()) {
			if (!buildTask.getRun()) {
				success = false;
				break;
			}
			if (j_file.isFile()) {
				j_file.delete();// 删除文件
			} else if (j_file.isDirectory()) {
				// 递归删除文件夹
				deleteDir(buildTask, j_file);
			}
		}
		if (success) {
			file.delete();// 删除目录本身
		}
	}
}
