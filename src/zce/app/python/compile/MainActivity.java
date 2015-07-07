package zce.app.python.compile;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.commons.codec.binary.Base64Codec;

import python.filechooser.FileChooserActivity;
import zce.app.python.compile.util.CreateProject;
import zce.app.python.compile.util.FileUtils;
import zce.app.python.compile.util.Log;
import zce.app.python.compile.util.Settings;
import zce.app.python.compile.widget.EditText;
import zce.app.python.compile.widget.ErrorDialog;
import zce.app.sdpath.GetPath;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import de.keyboardsurfer.mobile.app.android.widget.crouton.Crouton;
import de.keyboardsurfer.mobile.app.android.widget.crouton.Style;

//import zce.app.python.compile.util.Log;

public class MainActivity extends ActionBarActivity {

	/**
	 * appName 程序名 packageName 包名 versionCode 版本号 versionName 版本名 iconPath 图标路径
	 * projectPath 项目路径
	 */
	private EditText appName, packageName, versionCode, versionName, iconPath,
			projectPath;

	private Button buildBtn;
	/**
	 * 结果显示
	 */
	private static TextView result;
	/**
	 * so 库解密
	 */
	private NativeMethods nativeMethods;
	/**
	 * base64解码
	 */
	private Base64Codec base;
	/**
	 * 编译apk
	 */
	private BuildTask buildTask;
	/**
	 * 编译命令
	 */
	private CompileTask compileTask;
	/**
	 * assets资源管理器
	 */
	private AssetManager assetManager;
	/**
	 * 命令字典
	 */
	private Map<String, String> map;
	/**
	 * 采用哪种打包方式
	 */
	private String whichMethod = "One";
	/**
	 * 选择的图标路径还是项目路径
	 */
	private String whichPath;
	/**
	 * 内部储存路径
	 */
	private static File path;
	/**
	 * 配置储存
	 */
	private SharedPreferences sharedPreferences;

	public boolean isRun = false;

	// private static final String[] files = { "cres.sh", "cjava.sh", "cdex.sh",
	// "capk.sh", "sapk.sh" };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.build_main);
		// 保持屏幕常亮
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		path = getFilesDir();
		assetManager = getAssets();

		sharedPreferences = getSharedPreferences(getPackageName(), MODE_PRIVATE);
		Settings.getSettings(sharedPreferences);

		appName = (EditText) findViewById(R.id.appName);
		appName.setTag(Settings.APPNAME);
		appName.setText(Settings.appName);
		appName.setText("test");

		packageName = (EditText) findViewById(R.id.packageName);
		packageName.setTag(Settings.PACKAGENAME);
		packageName.setText(Settings.packageName);
		packageName.setText("com.test");

		versionCode = (EditText) findViewById(R.id.versionCode);
		versionCode.setTag(Settings.VERSIONCODE);
		versionCode.setText(Settings.versionCode);
		versionCode.setText("1");

		versionName = (EditText) findViewById(R.id.versionName);
		versionName.setTag(Settings.VERSIONNAME);
		versionName.setText(Settings.versionName);
		versionName.setText("1.0");

		iconPath = (EditText) findViewById(R.id.iconPath);
		iconPath.setTag(Settings.ICONPATH);
		iconPath.setText(Settings.iconPath);

		projectPath = (EditText) findViewById(R.id.projectPath);
		projectPath.setTag(Settings.PROJECTPATH);
		projectPath.setText(Settings.projectPath);
		projectPath.setText(new File(path, "script.py").getAbsolutePath());

		result = (TextView) findViewById(R.id.result);
		buildBtn = (Button) findViewById(R.id.buildBtn);

		String aname = appName.getText().toString().trim();
		String pname = packageName.getText().toString().trim();
		String vcode = versionCode.getText().toString().trim();
		String vname = versionName.getText().toString().trim();
		String ipath = iconPath.getText().toString().trim();

		if (aname == null || aname.length() == 0) {
			Crouton.showText(this, "请先输入应用名", Style.ALERT);
			return;
		}
		if (pname == null || pname.length() == 0) {
			Crouton.showText(this, "请先输入包名", Style.ALERT);
			return;
		}
		if (vcode == null || vcode.length() == 0) {
			Crouton.showText(this, "请先输入版本号", Style.ALERT);
			return;
		}
		if (vname == null || vname.length() == 0) {
			Crouton.showText(this, "请先输入版本名", Style.ALERT);
			return;
		}
		if (!pname.matches("([a-z]+[.][a-z]+)*")) {
			Crouton.showText(this, "输入的包名不规范", Style.ALERT);
			return;
		}
		// 检测图标选择
		File ifile = new File(ipath);
		if (ipath == null || ipath.length() == 0 || !ifile.isFile()
				|| !ifile.getName().matches("\\w+.jpg|\\w+.png")) {
			ifile = null;
			Crouton.showText(this, "图标文件不可用将采用默认图标", Style.INFO);
		} else if (ifile.length() > 102400) {// 500kb
			Crouton.showText(this, "选择的图标文件实在是太大了!", Style.ALERT);
			return;
		}

		// 加载命令
		if (map == null || map.isEmpty()) {
			try {
				map = FileUtils.readCommand(getAssets().open("command.dat"));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Crouton.showText(this, "加载命令代码失败", Style.ALERT);
				return;
			}
		}
		if (nativeMethods == null) {// 第一次初始化
			nativeMethods = new NativeMethods();
		}
		if (base == null) {
			base = new Base64Codec();// 初始化base64解码
		}

		buildTask = new BuildTask(pname, aname, vcode, vname, ifile, new File(
				""));
		compileTask = new CompileTask(buildTask, path,
				getPackageResourcePath(), aname, pname,
				new CompileTaskHandler());
		buildTask.execute();
		isRun = true;
		buildBtn.setText("停止任务");

		// doIntent(getIntent());

		// map = new HashMap<String, String>();
		// base = new Base64Codec();
		// nativeMethods = new NativeMethods();
		// for (String fileName : files) {
		// try {
		// map.put(fileName.replace(".sh", ""), nativeMethods
		// .decrypt(new String(base.encode(FileUtils
		// .toByteArray(getAssets().open(fileName))),
		// "UTF-8")));
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// }
		// try {
		// ObjectOutputStream o = new ObjectOutputStream(new FileOutputStream(
		// new File(getFilesDir(), "command.dat")));
		// o.writeObject(map);
		// o.close();
		// } catch (FileNotFoundException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		//
		// Toast.makeText(this, "ok", Toast.LENGTH_LONG).show();

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			return true;
			// Toast.makeText(this, "后台运行中", Toast.LENGTH_SHORT).show();
			// moveTaskToBack(true);
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.exit) {
			return true;
			// if (buildTask != null) {
			// buildTask.cancel();
			// }
			// finish();
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onDestroy() {
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		super.onDestroy();
		android.os.Process.killProcess(android.os.Process.myPid());
		try {
			Thread.sleep(1000);
			System.exit(1);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		// Log.v(data.toString());
		if (resultCode == RESULT_OK) {
			if (requestCode == 5555 && data != null) {// 获取选择的文件路径
				String path = data.getData().getPath();
				if (whichPath != null && path != null) {
					if (whichPath.equals(Settings.ICONPATH)) {
						iconPath.setText(path);
						Settings.saveSettings(sharedPreferences,
								Settings.ICONPATH, path);
					} else if (whichPath.equals(Settings.PROJECTPATH)) {
						projectPath.setText(path);
						Settings.saveSettings(sharedPreferences,
								Settings.PROJECTPATH, path);
					}
				}
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		doIntent(intent);
	}

	/**
	 * 处理Intent
	 * 
	 * @param intent
	 */
	private void doIntent(Intent intent) {
		if (intent.getAction().equals("zce.app.python.compile.OPENFILE")) {
			try {
				Intent inten = new Intent(this, FileChooserActivity.class);
				whichPath = intent.getStringExtra("which");
				startActivityForResult(inten, 5555);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 依赖平台
	 * 
	 * @param v
	 */
	public void radioRelyOn(View v) {
		whichMethod = "One";
	}

	/**
	 * 不依赖平台
	 * 
	 * @param v
	 */
	public void radioInclude(View v) {
		whichMethod = "Two";
	}

	/**
	 * 获取储存路径
	 * 
	 * @return
	 */
	public File getSdpath() {
		return new GetPath().path(this);
	}

	/**
	 * 一键打包按钮
	 * 
	 * @param v
	 */
	public void onBuild(View v) {
		if (isRun) {
			if (buildTask != null) {
				buildTask.cancel();
			}// 任务正在运行中
			Crouton.showText(this, "当前任务还在运行中!请稍等片刻!", Style.INFO);
			return;
		} else {
			buildBtn.setText("一键打包");
		}
		String aname = appName.getText().toString().trim();
		String pname = packageName.getText().toString().trim();
		String vcode = versionCode.getText().toString().trim();
		String vname = versionName.getText().toString().trim();
		String ipath = iconPath.getText().toString().trim();
		String ppath = projectPath.getText().toString().trim();
		if (aname == null || aname.length() == 0) {
			Crouton.showText(this, "请先输入应用名", Style.ALERT);
			return;
		}
		if (pname == null || pname.length() == 0) {
			Crouton.showText(this, "请先输入包名", Style.ALERT);
			return;
		}
		if (vcode == null || vcode.length() == 0) {
			Crouton.showText(this, "请先输入版本号", Style.ALERT);
			return;
		}
		if (vname == null || vname.length() == 0) {
			Crouton.showText(this, "请先输入版本名", Style.ALERT);
			return;
		}
		if (!pname.matches("([a-z]+[.][a-z]+)*")) {
			Crouton.showText(this, "输入的包名不规范", Style.ALERT);
			return;
		}
		// 检测图标选择
		File ifile = new File(ipath);
		if (ipath == null || ipath.length() == 0 || !ifile.isFile()
				|| !ifile.getName().matches("\\w+.jpg|\\w+.png")) {
			ifile = null;
			Crouton.showText(this, "图标文件不可用将采用默认图标", Style.INFO);
		} else if (ifile.length() > 102400) {// 500kb
			Crouton.showText(this, "选择的图标文件实在是太大了!", Style.ALERT);
			return;
		}
		// 检测脚本选择
		if (ppath == null || ppath.length() == 0) {
			Crouton.showText(this, "请选择需要打包的py文件或者项目目录", Style.ALERT);
			return;
		}
		File pfile = new File(ppath);
		boolean pflag = false;
		if (pfile.isDirectory()) {
			// 如果是文件夹
			for (File f : pfile.listFiles()) {
				if (f.getName().equals("script.py")) {
					pflag = true;// 通过
				}
			}
		} else if (pfile.isFile()) {
			// 如果是文件
			if (pfile.getName().equals("script.py")) {
				pflag = true;// 通过
			}
		}
		if (!pflag) {
			pfile = null;
			Crouton.showText(this, "请选择需要打包的py文件或者项目目录", Style.ALERT);
			return;
		}
		// 加载命令
		if (map == null || map.isEmpty()) {
			try {
				map = FileUtils.readCommand(getAssets().open("command.dat"));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Crouton.showText(this, "加载命令代码失败", Style.ALERT);
				return;
			}
		}
		if (nativeMethods == null) {// 第一次初始化
			nativeMethods = new NativeMethods();
		}
		if (base == null) {
			base = new Base64Codec();// 初始化base64解码
		}

		buildTask = new BuildTask(pname, aname, vcode, vname, ifile, pfile);
		compileTask = new CompileTask(buildTask, path,
				getPackageResourcePath(), aname, pname,
				new CompileTaskHandler());
		buildTask.execute();
		isRun = true;
		buildBtn.setText("停止任务");
	}

	public class BuildTask extends AsyncTask<Boolean, String, Boolean> {

		/**
		 * pname 包名,aname 应用名,vcode 版本号, vname 版本名
		 */
		private String pname, aname, vcode, vname;
		/**
		 * ifile 图标路径,pfile 项目路径
		 */
		private File ifile, pfile;
		/**
		 * 运行状态标志
		 */
		private boolean isRun = true;

		public BuildTask(String pname, String aname, String vcode,
				String vname, File ifile, File pfile) {
			this.pname = pname;
			this.aname = aname;
			this.vcode = vcode;
			this.vname = vname;
			this.ifile = ifile;
			this.pfile = pfile;
		}

		public void cancel() {
			// Log.i("cancel");
			this.isRun = false;
		}

		public boolean getRun() {
			// Log.i("getRun: " + Boolean.toString(this.isRun));
			return this.isRun;
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			// 主线程中运行
			result.setText("请稍后!正在构建项目中...\n");
			Crouton.showText(MainActivity.this, "请稍后!正在构建项目中...", Style.CONFIRM);
			super.onPreExecute();
		}

		@Override
		protected Boolean doInBackground(Boolean... arg) {
			// TODO Auto-generated method stub
			// 耗时任务
			// 检测签名
			// boolean flag = true;
			// try {
			// Iterator<PackageInfo> iter = getPackageManager()
			// .getInstalledPackages(PackageManager.GET_SIGNATURES)
			// .iterator();
			// PackageInfo info;
			// BufferedReader br = new BufferedReader(new InputStreamReader(
			// assetManager.open("data.dat")));
			// StringBuffer sb = new StringBuffer();
			// String line;
			// while ((line = br.readLine()) != null) {
			// sb.append(line.replace("\r\n", "").replace("\n", "").trim());
			// }
			// br.close();
			// String packName = getPackageName();
			// while (iter.hasNext()) {
			// info = iter.next();
			// if (info.packageName.equals(packName)) {
			// String str = "";
			// String data = sb.toString();
			// String key = "zce.log.cat";
			// int ch;
			// for (int i = 0, j = 0; i < data.length(); i++, j++) {
			// if (j > key.length() - 1) {
			// j = j % key.length();
			// }
			// ch = (data.codePointAt(i) + 65535 - key
			// .codePointAt(j));
			// if (ch > 65535) {
			// ch = ch % 65535;
			// }
			// str += (char) ch;
			// }
			// if (info.signatures[0].toCharsString().equals(str)) {
			// flag = false;
			// break;
			// }
			// }
			// }
			// if (flag) {
			// publishProgress("0", "签名验证失败!");
			// publishProgress("1", "签名验证失败!");
			// return false;
			// }
			// } catch (IOException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// publishProgress("0", "签名验证失败!");
			// publishProgress("1", "签名验证失败!");
			// return false;
			// }

			// 1.先删除原有项目所有文件
			publishProgress("1", "正在删除旧项目...\n");
			FileUtils.deleteDir(this, new File(path, "Project"));
			// 2.解压数据
			try {
				publishProgress("1", "正在解压项目文件...\n");
				if (!CreateProject.unzip(this, assetManager.open("assets.zip"),
						path.getAbsolutePath())) {
					// 解压所需文件失败!
					publishProgress("0", "解压所需文件失败!");
					return false;
				}

				if (!CreateProject.unzip(this,
						assetManager.open("Python" + whichMethod + ".zip"),
						path.getAbsolutePath())) {
					// 解压所需文件失败!
					publishProgress("0", "解压所需文件失败!");
					return false;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
			publishProgress("1", "解压完成...\n");
			// 3.复制模版文件

			if (CreateProject.copyFromTemplate(this, path, whichMethod, pname,
					aname, vcode, vname, ifile)) {
				publishProgress("1", "创建项目完成!\n");
			} else {
				publishProgress("0", "创建项目失败!");
				publishProgress("1", "创建项目失败!\n");
				return false;
			}
			// 4.打包脚本文件或者脚本项目

			// publishProgress("1", "压缩复制需要打包的项目\n");
			// if (!CreateProject.zipFiles(this, path, pfile)) {
			// publishProgress("0", "压缩文件失败!");
			// return false;
			// }

			publishProgress("1", "压缩文件成功\n");
			// 5.开始构建apk
			Log.v("cres start");
			if (!compileTask.Cres(base, nativeMethods, map.get("cres"))) {
				Log.w(compileTask.getErrorData());
				publishProgress("3", compileTask.getErrorData());
				return false;
			}

			Log.v("cjava start");
			if (!compileTask.Cjava(base, nativeMethods, map.get("cjava"))) {
				Log.w(compileTask.getErrorData());
				publishProgress("3", compileTask.getErrorData());
				return false;
			}
			Log.v("cdex start");
			if (!compileTask.Cdex(base, nativeMethods, map.get("cdex"))) {
				Log.w(compileTask.getErrorData());
				publishProgress("3", compileTask.getErrorData());
				return false;
			}

			Log.v("capk start");
			if (!compileTask.Capk(base, nativeMethods, map.get("capk"))) {
				Log.w(compileTask.getErrorData());
				publishProgress("3", compileTask.getErrorData());
				return false;
			}

			Log.v("sapk start");
			if (!compileTask.Sapk(base, nativeMethods, map.get("sapk"))) {
				Log.w(compileTask.getErrorData());
				publishProgress("3", compileTask.getErrorData());
				return false;
			}
			Log.v("copy apk file");
			// 复制apk到外部
			File apkFile = new File(path, "Project/bin/" + aname + ".sign.apk");
			File newPath = new File(getSdpath(), aname + ".sign.apk");
			if (apkFile.exists()) {
				publishProgress("1", "复制已编译的apk到: " + newPath.getAbsolutePath()
						+ "\n");
				if (FileUtils.copyFile(apkFile, newPath)) {
					// 复制文件成功
					publishProgress("2", "APK文件在: " + newPath.getAbsolutePath());
				} else {
					newPath = new File(MainActivity.this.getExternalCacheDir(),
							aname + ".sign.apk");
					publishProgress("1",
							"复制已编译的apk到: " + newPath.getAbsolutePath() + "\n");
					if (FileUtils.copyFile(apkFile, newPath)) {
						// 复制成功
						publishProgress("2",
								"APK文件在: " + newPath.getAbsolutePath());
					} else {
						publishProgress("1",
								"复制apk文件失败,请到 " + path.getAbsolutePath()
										+ "/Project/bin/ 下查看\n");
						return false;
					}
				}
			} else {
				return false;
			}
			return true;
		}

		@Override
		protected void onProgressUpdate(String... values) {
			// TODO Auto-generated method stub
			// 更新进度
			if (values.length == 2) {
				String which = values[0];
				String msg = values[1];
				if (which == "0") {
					Crouton.showText(MainActivity.this, msg, Style.ALERT);
				} else if (which == "1") {
					result.setText(msg);
					// result.append(msg);
				} else if (which == "2") {
					Crouton.showText(MainActivity.this, msg, Style.INFO);
				} else if (which == "3") {
					ErrorDialog errorDialog = new ErrorDialog(MainActivity.this);
					errorDialog.setText(msg);
					errorDialog.show();
				}
			}
			super.onProgressUpdate(values);
		}

		@Override
		protected void onPostExecute(Boolean result) {
			// TODO Auto-generated method stub
			// 完成
			if (result) {
				MainActivity.result.setText("编译apk完成");
				Crouton.showText(MainActivity.this, "编译apk完成", Style.CONFIRM);
			} else {// 返回false表示失败了
				if (isRun) {
					MainActivity.result.setText("编译apk失败");
					Crouton.showText(MainActivity.this, "编译apk失败", Style.ALERT);
				} else {
					MainActivity.result.setText("编译任务已取消");
					Crouton.showText(MainActivity.this, "编译任务已取消", Style.INFO);
				}
			}
			MainActivity.this.isRun = false;
			// MainActivity.result.setText(R.string.aboutText);
			buildBtn.setText("一键打包");
			super.onPostExecute(result);
		}
	}

	static class CompileTaskHandler extends Handler {

		/**
		 * 总共记录的条数
		 */
		// private int total = 0;

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			result.setText((String) msg.obj);
			// if (total > 15) {
			// result.setText("");
			// total = 0;
			// }
			// result.append((String) msg.obj);
			// result.scrollTo(0, result.getHeight());
			// total++;
			super.handleMessage(msg);
		}
	}

}
