package zce.app.python.compile.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.codec.binary.Base64Codec;

import zce.app.python.compile.MainActivity.BuildTask;
import zce.app.python.compile.NativeMethods;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

//import zce.app.python.compile.util.Log;

public class CreateProject {

	private static List<File> fileList;

	public CreateProject() {
	}

	/**
	 * 转png
	 * 
	 * @param filesDir
	 * @param iconPath
	 * @return
	 */
	private static boolean toPng(File filesDir, File iconPath) {
		Bitmap bitmap = BitmapFactory.decodeFile(iconPath.getAbsolutePath());
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(new File(filesDir, "tmp.png"));
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
			fos.flush();
			fos.close();
			bitmap.recycle();
			bitmap = null;
			return true;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 解密脚本加密
	 * 
	 * @param filePath
	 * @param nativeMethods
	 * @param base
	 * @param inputStream
	 * @return
	 */
	public static boolean decrypt(File filePath, NativeMethods nativeMethods,
			Base64Codec base, InputStream inputStream) {
		boolean success = false;
		try {
			FileUtils.toSaveFile(filePath, base.decode(nativeMethods.decrypt(
					new String(FileUtils.toByteArray(inputStream), "UTF-8"))
					.getBytes("UTF-8")));
			success = true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return success;
	}

	/**
	 * 获取所有java文件
	 * 
	 * @param file
	 */
	private static void getFileList(File file) {
		for (File j_file : file.listFiles()) {
			if (j_file.isFile()) {
				fileList.add(j_file.getAbsoluteFile());
			} else if (j_file.isDirectory()) {
				getFileList(j_file.getAbsoluteFile());
			}
		}
	}

	/**
	 * 处理java文件替换包名
	 * 
	 * @param filesDir
	 * @param packageName
	 * @return
	 */
	private static boolean delWithJavaFile(BuildTask buildTask, File filesDir,
			String whichMethod, String packageName) {
		boolean success = false;
		fileList = new ArrayList<File>();
		// 遍历获取所有java文件
		getFileList(new File(filesDir, "Project/src"));
		// 读取所有文件进行包名替换
		for (File j_file : fileList) {
			if (!buildTask.getRun()) {
				break;
			}
			if (FileUtils
					.writeTextFile(
							j_file.getAbsolutePath(),
							FileUtils.readTextFile(j_file).replace(
									"$packagename$", packageName))) {
				success = true;
			} else {
				success = false;
				break;
			}
		}
		if (!buildTask.getRun()) {
			success = false;
		}
		return success;
	}

	/**
	 * 复制java文件
	 * 
	 * @param src_dir
	 * @param t_file
	 * @param file_name
	 * @param packageName
	 * @return
	 */
	private static boolean copyJavaFile(File src_dir, File t_file,
			String file_name, String packageName) {
		boolean success = false;
		try {
			// 先从PythonOne删除该文件再复制过去再修改
			File tmp = new File(src_dir, file_name);
			if (tmp.exists()) {
				tmp.delete();// 删除
			}
			// 复制
			if (!FileUtils.copyFile(t_file, tmp)) {
				// 失败
				success = false;
			} else {
				success = true;
				// 修改
				// if (FileUtils.writeTextFile(tmp.getAbsolutePath(), FileUtils
				// .readTextFile(tmp)
				// .replace("$packagename$", packageName))) {
				//
				// }
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			success = false;
		}
		return success;
	}

	/**
	 * 复制xml文件
	 * 
	 * @param filesDir
	 * @param dir
	 * @param t_file
	 * @param file_name
	 * @param args
	 * @return
	 */
	private static boolean copyXmlFile(File filesDir, String dir, File t_file,
			String file_name, Object... args) {
		boolean success = false;
		try {
			// 先从PythonOne删除该文件再复制过去再修改
			File tmp = new File(new File(filesDir, dir), file_name);
			if (tmp.exists()) {
				tmp.delete();// 删除
			}
			// 复制
			if (!FileUtils.copyFile(t_file, tmp)) {
				// 失败
				success = false;
			} else {
				if (FileUtils.writeTextFile(tmp.getAbsolutePath(),
						String.format(FileUtils.readTextFile(tmp), args))) {
					success = true;
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			success = false;
		}
		return success;
	}

	/**
	 * 从模版中复制文件
	 * 
	 * @param filesDir
	 * @param packageName
	 * @param appName
	 * @param versionCode
	 * @param versionName
	 * @param iconPath
	 * @return
	 */
	public static boolean copyFromTemplate(BuildTask buildTask, File filesDir,
			String whichMethod, String packageName, String appName,
			String versionCode, String versionName, File iconPath) {
		boolean success = false;
		// 在gen下创建包名文件夹
		File gen_dir = new File(filesDir, "Project/gen/"
				+ packageName.replace(".", "/"));
		if (!gen_dir.exists()) {
			gen_dir.mkdirs();
		}
		// 创建src目录下包名目录
		File src_dir = new File(filesDir, "Project/src/"
				+ packageName.replace(".", "/"));
		if (!src_dir.exists()) {
			src_dir.mkdirs();
		}
		// 替换图标文件
		if (iconPath != null) {
			if (toPng(filesDir, iconPath)) {
				FileUtils.copyFile(new File(filesDir, "tmp.png"), new File(
						filesDir, "Project/res/drawable/icon.png"));
				FileUtils.copyFile(new File(filesDir, "tmp.png"), new File(
						filesDir, "Project/res/drawable/script_logo_48.png"));
			}
		}
		// 从模版中解压资源文件
		// try {
		// Log.v("whichMethod: " + whichMethod);
		// unzip(new FileInputStream(new File(filesDir, "Python" + whichMethod
		// + ".zip")), new File(filesDir, "Project").getAbsolutePath());
		// success = true;
		// } catch (FileNotFoundException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// success = false;
		// }
		// 从模版中复制代码文件
		for (File t_file : new File(filesDir, "Template_" + whichMethod)
				.listFiles()) {
			if (!buildTask.getRun()) {
				success = false;
				break;
			}
			String file_name = t_file.getName();
			if (file_name.endsWith(".java")) {
				// java文件
				if (!copyJavaFile(src_dir, t_file, file_name, packageName)) {
					success = false;
					break;
				} else {
					success = true;
				}
			} else if (file_name.equals("strings.xml")) {
				if (!copyXmlFile(filesDir, "Project/res/values", t_file,
						file_name, appName, appName)) {
					success = false;
					break;
				} else {
					success = true;
				}
			} else if (file_name.equals("AndroidManifest.xml")) {
				if (!copyXmlFile(filesDir, "Project", t_file, file_name,
						packageName, versionCode, versionName, packageName,
						packageName)) {
					success = false;
					break;
				} else {
					success = true;
				}
			}
		}
		// 处理java文件替换包名
		if (delWithJavaFile(buildTask, filesDir, whichMethod, packageName)) {
			success = true;
		} else {
			success = false;
		}
		if (!buildTask.getRun()) {
			success = false;
		}
		return success;
	}

	/**
	 * 解压数据
	 * 
	 * @param inputStream
	 * @param path
	 * @return
	 */
	public static boolean unzip(BuildTask buildTask, InputStream inputStream,
			String path) {
		boolean success = false;
		try {
			byte[] buffer = new byte[1024];
			ZipInputStream zip = new ZipInputStream(inputStream);
			ZipEntry zipEntry;
			while ((zipEntry = zip.getNextEntry()) != null) {
				if (!buildTask.getRun()) {
					break;
				}
				File file = new File(path, zipEntry.getName());
				if (zipEntry.isDirectory()) {
					file.mkdirs();// 如果是文件夹并且不存在则创建
					FileUtils.chmod(file, 0755);
				} else {
					if (!file.getParentFile().exists()) {
						file.getParentFile().mkdirs();
						FileUtils.chmod(file.getParentFile(), 0755);
					}
					// if (file.lastModified() < zipEntry.getTime()) {
					if (file.length() < 10240) {
						// 文件不存在或者文件的大小小于1M才覆盖
						OutputStream output = new BufferedOutputStream(
								new FileOutputStream(file), 1024);
						int len;
						while ((len = zip.read(buffer, 0, 1024)) != -1) {
							output.write(buffer, 0, len);
						}
						output.flush();
						output.close();
						file.setLastModified(zipEntry.getTime());
						FileUtils.chmod(file, 0755);
					}
					// }
				}
			}
			zip.close();
			if (!buildTask.getRun()) {
				success = false;
			} else {
				success = true;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return success;
	}

	/**
	 * 
	 * @param zos
	 * @param file
	 * @param basePath
	 */
	private static boolean zipFile(ZipOutputStream zos, File file,
			String basePath) {
		boolean success = true;
		FileInputStream input = null;
		if (file.isDirectory()) {
			// 得到当前文件目录里面的文件列表
			// Log.v("zipFile: " + "文件夹");
			basePath = basePath + (basePath.length() == 0 ? "" : "/")
					+ file.getName();
			// Log.v("basePath: " + basePath);
			// 循环递归压缩
			for (File f : file.listFiles()) {
				if (!zipFile(zos, f, basePath)) {
					success = false;
					break;
				}
			}
		} else {
			// 压缩文件
			basePath = (basePath.length() == 0 ? "" : basePath + "/")
					+ file.getName();
			// Log.v("basePath: " + basePath);
			try {
				zos.putNextEntry(new ZipEntry(basePath));
				input = new FileInputStream(file);
				int readLen = 0;
				byte[] buffer = new byte[1024];
				while ((readLen = input.read(buffer, 0, 1024)) != -1) {
					zos.write(buffer, 0, readLen);
				}
				input.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				success = false;
				e.printStackTrace();
			}
		}
		return success;
	}

	/**
	 * 压缩单个文件或者文件夹中的子文件和文件夹
	 * 
	 * @param srcFile
	 * @return
	 */
	public static boolean zipFiles(BuildTask buildTask, File filesDir,
			File srcFile) {
		// Log.i("zipFiles: " + srcFile.getAbsolutePath());
		boolean success = true;
		try {
			ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(
					new File(filesDir, "Project/res/raw/python_project.zip")));
			if (srcFile.isDirectory()) {
				for (File f : srcFile.listFiles()) {
					if (!buildTask.getRun()) {
						success = false;
						break;
					}
					// Log.i("zipFiles: " + f.getAbsolutePath());
					if (!zipFile(zos, f, "")) {
						success = false;
						break;
					}
				}
			} else {
				success = zipFile(zos, srcFile, "");
			}
			zos.flush();
			zos.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			success = false;
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			success = false;
			e.printStackTrace();
		}
		if (!buildTask.getRun()) {
			success = false;
		}
		return success;
	}

}
