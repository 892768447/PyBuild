package zce.app.python.compile;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import org.apache.commons.codec.binary.Base64Codec;

import zce.app.python.compile.MainActivity.BuildTask;
import zce.app.python.compile.util.Log;
import android.os.Handler;
import android.os.Message;

//import zce.app.python.compile.util.Log;

/**
 * {@link #Cres(String)} {@link #Cjava(String)} {@link #Cdex(String)}
 * {@link #Capk(String)} {@link #Sapk(String)}
 * 
 * @author zce
 * 
 */
public class CompileTask {

	private Handler handler;
	private Message message;
	private File fileDir;
	private String path;
	private String appName;
	private String dexPath;
	private String packageName;
	private BuildTask buildTask;
	private String errorData;

	public CompileTask(BuildTask buildTask, File fileDir,String dexPath, String appName,
			String packageName, Handler handler) {
		this.buildTask = buildTask;
		this.fileDir = fileDir;
		this.dexPath = dexPath;
		this.appName = appName;
		this.packageName = packageName.replace(".", "/");
		this.path = fileDir.getAbsolutePath();
		this.handler = handler;
	}

	/**
	 * 处理资源文件
	 * 
	 * @param command
	 * @return
	 */
	public boolean Cres(Base64Codec base, NativeMethods nativeMethods,
			String command) {
		if (!buildTask.getRun()) {
			return false;
		}
		try {
			// Log.v(new String(base.decode(nativeMethods.decrypt(
			// command).getBytes("UTF-8")), "UTF-8"));
			sendMsg("正在处理资源文件\n");
			return compile(new String(base.decode(nativeMethods
					.decrypt(command).getBytes("UTF-8")), "UTF-8"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		sendMsg("处理资源文件失败\n");
		return false;
	}

	/**
	 * 编译java文件
	 * 
	 * @param command
	 * @return
	 */
	public boolean Cjava(Base64Codec base, NativeMethods nativeMethods,
			String command) {
		if (!buildTask.getRun()) {
			return false;
		}
		try {
			// Log.v(new String(base.decode(nativeMethods.decrypt(
			// command).getBytes("UTF-8")), "UTF-8"));
			sendMsg("正在编译代码文件\n");
			return compile(new String(base.decode(nativeMethods
					.decrypt(command).getBytes("UTF-8")), "UTF-8"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		sendMsg("编译源代码失败\n");
		return false;
	}

	/**
	 * 打包成dex
	 * 
	 * @param command
	 * @return
	 */
	public boolean Cdex(Base64Codec base, NativeMethods nativeMethods,
			String command) {
		if (!buildTask.getRun()) {
			return false;
		}
		try {
			sendMsg("正在生成dex文件\n");
			return compile(new String(base.decode(nativeMethods
					.decrypt(command).getBytes("UTF-8")), "UTF-8"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		sendMsg("生成dex文件失败\n");
		return false;
	}

	/**
	 * 编译成apk
	 * 
	 * @param command
	 * @return
	 */
	public boolean Capk(Base64Codec base, NativeMethods nativeMethods,
			String command) {
		if (!buildTask.getRun()) {
			return false;
		}
		try {
			sendMsg("正在编译成apk\n");
			return compile(new String(base.decode(nativeMethods
					.decrypt(command).getBytes("UTF-8")), "UTF-8"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		sendMsg("编译apk文件失败\n");
		return false;
	}

	/**
	 * 签名apk
	 * 
	 * @param command
	 * @return
	 */
	public boolean Sapk(Base64Codec base, NativeMethods nativeMethods,
			String command) {
		if (!buildTask.getRun()) {
			return false;
		}
		try {
			sendMsg("正在签名apk文件\n");
			return compile(new String(base.decode(nativeMethods
					.decrypt(command).getBytes("UTF-8")), "UTF-8"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		sendMsg("签名apk文件失败\n");
		return false;
	}

	/**
	 * 执行命令函数
	 * 
	 * @param command
	 * @return
	 */
	private boolean compile(final String command) {
		// TODO Auto-generated method stub
		if (!buildTask.getRun()) {
			return false;
		}
		setErrorData("");
		boolean success = true;
		try {
			ProcessBuilder pb = new ProcessBuilder("/system/bin/sh");
			pb.redirectErrorStream(true);
			pb.directory(fileDir);// 设置shell的当前目录

			// 设置目录
			// Log.i("ROOTHOME", path);
			// pb.environment().put("ROOTHOME", path);

			// 设置项目目录
			// Log.i("PROJECT: " + new File(path, "Project").getAbsolutePath());
			pb.environment().put("PROJECT",
					new File(path, "Project").getAbsolutePath());
			// 设置程序名字
			// Log.i("NAME: " + appName);
			pb.environment().put("NAME", appName);

			// 设置程序包名
			// Log.i("PACKAGENAME: " + packageName);
			pb.environment().put("PACKAGENAME", packageName);

			// 设置android.jar路径
			// Log.i("ANDROIDJAR: "
			// + new File(path, "android.jar").getAbsolutePath());
			pb.environment().put("ANDROIDJAR",
					new File(path, "android.jar").getAbsolutePath());

			// 设置tools.jar
			// Log.i("TOOLJAR: " + new File(path,
			// "tools.jar").getAbsolutePath());
//			pb.environment().put("TOOLJAR",
//					new File(path, "tools.jar").getAbsolutePath());
			pb.environment().put("TOOLJAR", dexPath);

			// 设置工具类名
			// Log.i("TOOL: " + "zce.app.compile.external");
			pb.environment().put("TOOL", "zce.app.compile.external");

			// 设置jars路径
			// Log.i("JARS: "
			// +
			// "libs/android-support-v4.jar:libs/guava-r06.jar:libs/locale_platform.jar:libs/nineoldandroids-2.4.0.jar:libs/script.jar");
			pb.environment()
					.put("JARS",
							"libs/android-support-v4.jar:libs/guava-r06.jar:libs/locale_platform.jar:libs/nineoldandroids-2.4.0.jar:libs/script.jar");

			// 设置aapt路径
			// Log.i("AAPT: " + new File(path, "aapt").getAbsolutePath());
			pb.environment().put("AAPT",
					new File(path, "aapt").getAbsolutePath());

			// // 设置JAVAC路径
			// Log.i("JAVAC", new File(path,
			// "javac").getAbsolutePath());
			// pb.environment().put("JAVAC",
			// new File(path, "javac").getAbsolutePath());
			//
			// // 设置DEX路径
			// Log.i("DEX", new File(path, "dex").getAbsolutePath());
			// pb.environment()
			// .put("DEX", new File(path, "dex").getAbsolutePath());
			//
			// // 设置BUILD路径
			// Log.i("BUILD", new File(path,
			// "apkbuilder").getAbsolutePath());
			// pb.environment().put("BUILD",
			// new File(path, "apkbuilder").getAbsolutePath());
			//
			// // 设置SIGN路径
			// Log.i("SIGN", new File(path,
			// "signer").getAbsolutePath());
			// pb.environment().put("SIGN",
			// new File(path, "signer").getAbsolutePath());

			Process proc = pb.start();
			// 获取输入流，可以通过它获取SHELL的输出。
			BufferedReader in = new BufferedReader(new InputStreamReader(
					proc.getInputStream()));
			BufferedReader err = new BufferedReader(new InputStreamReader(
					proc.getErrorStream()));
			// 获取输出流，可以通过它向SHELL发送命令。
			PrintWriter out = new PrintWriter(new BufferedWriter(
					new OutputStreamWriter(proc.getOutputStream())), true);
			// for (String c : command.split("\n")) {
			// out.println("echo " + c);
			// }
			out.println(command);
			out.println("exit");
			String line;
			while ((line = in.readLine()) != null) {
				if (!buildTask.getRun()) {
					success = false;
					break;
				}
				sendMsg("result: " + line + "\n"); // 打印输出结果
				if (line.indexOf("error") > -1
						|| line.indexOf("not exist") > -1) {
					success = false;
					Log.w(line + "\n");
					errorData += line + "\n";
					sendMsg("编译发生错误");
					break;
				}
			}
			while ((line = err.readLine()) != null) {
				if (!buildTask.getRun()) {
					success = false;
					break;
				}
				success = false;
				Log.w(line + "\n");
				errorData += line + "\n";
				sendMsg("error: " + line + "\n"); // 打印错误输出结果
			}
			in.close();
			out.close();
			proc.destroy();
			// sendMsg("命令结束: " + command);
		} catch (IOException e) {
			success = false;
			errorData += e.getMessage() + "\n";
			sendMsg(e.getMessage());
			e.printStackTrace();
		}
		return success;
	}

	private void sendMsg(String msg) {
		message = new Message();
		message.obj = msg;
		handler.sendMessage(message);
	}

	public String getErrorData() {
		return errorData;
	}

	public void setErrorData(String errorData) {
		this.errorData = errorData;
	}

}
