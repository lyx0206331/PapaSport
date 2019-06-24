package com.by100.util;//package com.by100.util;
//import java.io.File;
//import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.PrintWriter;
//import java.io.StringWriter;
//import java.io.Writer;
//import java.lang.Thread.UncaughtExceptionHandler;
//import java.lang.reflect.Field;
//import java.text.SimpleDateFormat;
//import java.util.Calendar;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.Map;
//
//import android.content.Context;
//import android.content.pm.PackageInfo;
//import android.content.pm.PackageManager;
//import android.content.pm.PackageManager.NameNotFoundException;
//import android.os.Build;
//import android.os.Environment;
//import android.os.Looper;
//import android.util.Log;
//import android.widget.Toast;
//
///**
// * UncaughtException处理类,当程序发生Uncaught异常的时候,由该类来接管程序,并记录发送错误报告.
// *
// * @author way
// *
// */
//public class RWCrashHandler implements UncaughtExceptionHandler {
//	private Thread.UncaughtExceptionHandler mDefaultHandler;// 系统默认的UncaughtException处理类
//	private static RWCrashHandler INSTANCE = new RWCrashHandler();// CrashHandler实例
//	private Context mContext;// 程序的Context对象
//	private Map<String, String> info = new HashMap<String, String>();// 用来存储设备信息和异常信息
//	private SimpleDateFormat format = new SimpleDateFormat(
//			"yyyy-MM-dd HH-mm-ss");// 用于格式化日期,作为日志文件名的一部分
//
//	/** 保证只有一个CrashHandler实例 */
//	private RWCrashHandler() {
//
//	}
//
//	/** 获取CrashHandler实例 ,单例模式 */
//	public static RWCrashHandler getInstance() {
//		return INSTANCE;
//	}
//
//	/**
//	 * 初始化
//	 *
//	 * @param context
//	 */
//	public void init(Context context) {
//		mContext = context;
//		mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();// 获取系统默认的UncaughtException处理器
//		Thread.setDefaultUncaughtExceptionHandler(this);// 设置该CrashHandler为程序的默认处理器
//	}
//
//	/**
//	 * 当UncaughtException发生时会转入该重写的方法来处理
//	 */
//	public void uncaughtException(Thread thread, Throwable ex) {
//		if (!handleException(ex) && mDefaultHandler != null) {
//			// 如果自定义的没有处理则让系统默认的异常处理器来处理
//			mDefaultHandler.uncaughtException(thread, ex);
//		} else {
//
////			int len = AppCommon.activityList.size();
////			for (int i = 0; i < len; i++) {
////				if(AppCommon.activityList.get(i) != null){
////					AppCommon.activityList.get(i).finish();
////				}
////			}
//			try {
//				Thread.sleep(3000);// 如果处理了，让程序继续运行3秒再退出，保证文件保存并上传到服务器
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			} finally {
//				// 退出程序
//				android.os.Process.killProcess(android.os.Process.myPid());
//				System.exit(0);
//			}
//		}
//	}
//
//	/**
//	 * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成.
//	 *
//	 * @param ex
//	 *            异常信息
//	 * @return true 如果处理了该异常信息;否则返回false.
//	 */
//	public boolean handleException(Throwable ex) {
//		if (ex == null)
//			return false;
//		new Thread() {
//			public void run() {
//				Looper.prepare();
//				Toast.makeText(mContext, "很抱歉,APP运行发生错误,即将退出", Toast.LENGTH_LONG).show();
//				Looper.loop();
//			}
//		}.start();
//		// 收集设备参数信息
//		collectDeviceInfo(mContext);
//		// 保存日志文件
//		saveCrashInfo2File(ex);
//		return true;
//	}
//
//	/**
//	 * 收集设备参数信息
//	 *
//	 * @param context
//	 */
//	public void collectDeviceInfo(Context context) {
//		try {
//			PackageManager pm = context.getPackageManager();// 获得包管理器
//			PackageInfo pi = pm.getPackageInfo(context.getPackageName(),
//					PackageManager.GET_ACTIVITIES);// 得到该应用的信息，即主Activity
//			if (pi != null) {
//				String versionName = pi.versionName == null ? "null"
//						: pi.versionName;
//				String versionCode = pi.versionCode + "";
//				info.put("versionName", versionName);
//				info.put("versionCode", versionCode);
//			}
//		} catch (NameNotFoundException e) {
//			e.printStackTrace();
//		}
//
//		Field[] fields = Build.class.getDeclaredFields();// 反射机制
//		for (Field field : fields) {
//			try {
//				field.setAccessible(true);
//				info.put(field.getName(), field.get("").toString());
//				Log.d("180", field.getName() + ":" + field.get(""));
//			} catch (IllegalArgumentException e) {
//				e.printStackTrace();
//			} catch (IllegalAccessException e) {
//				e.printStackTrace();
//			}
//		}
//	}
//
//	private String saveCrashInfo2File(Throwable ex) {
//		StringBuffer sb = new StringBuffer();
//		for (Map.Entry<String, String> entry : info.entrySet()) {
//			String key = entry.getKey();
//			String value = entry.getValue();
//			sb.append(key + "=" + value + "\r\n");
//		}
//		Writer writer = new StringWriter();
//		PrintWriter pw = new PrintWriter(writer);
//		ex.printStackTrace(pw);
//		Throwable cause = ex.getCause();
//		// 循环着把所有的异常信息写入writer中
//		while (cause != null) {
//			cause.printStackTrace(pw);
//			cause = cause.getCause();
//		}
//		pw.close();// 记得关闭
//		String result = writer.toString();
//		sb.append(result);
//		// 保存文件
//		// long timetamp = System.currentTimeMillis();
//		String time = format.format(new Date());
//		String fileName = time + "_YY.log";
//		Calendar calendar = Calendar.getInstance();
//		calendar.add(Calendar.DAY_OF_MONTH, -30);
//		if (Environment.getExternalStorageState().equals(
//				Environment.MEDIA_MOUNTED)) {
//			try {
//				File dir = new File(AppConfig.RootFileL);
//				if (!dir.exists())
//					dir.mkdir();
//
//				FileOutputStream fos = new FileOutputStream(dir + "/"
//						+ fileName);
//				fos.write(sb.toString().getBytes());
//				//发送给开发人员
////				sendCrashLog2PM(dir + fileName);
//				fos.close();
//				return fileName;
//			} catch (FileNotFoundException e) {
//				e.printStackTrace();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
//		return null;
//	}
//	/**
//	 * 将捕获的导致崩溃的错误信息发送给开发人员
//	 * 目前只将log日志保存在sdcard 和输出到LogCat中，并未发送给后台。
//	 */
////	private void sendCrashLog2PM(String fileName) {
////		if (!new File(fileName).exists()) {
////			Toast.makeText(mContext, "日志文件不存在！", Toast.LENGTH_SHORT).show();
////			return;
////		}
////		postFile(fileName,AppCommonData.ERRORLOG);
////	}
////	public void postFile(final String fileName,final String requestURL){
////		File file = new File(fileName);
////		if(file.exists() && file.length()>0){
////			AsyncHttpClient client = new AsyncHttpClient();
////			RequestParams params = new RequestParams();
////			try {
////				params.put("logfile", file);
////				client.post(requestURL, params,new AsyncHttpResponseHandler() {
////					@Override
////					public void onSuccess(int arg0, String arg1) {
////						Log.e("sss", "成功");
////					}
////					@Override
////					public void onFailure(Throwable arg0, String arg1) {
////						try {
////							Thread.sleep(5000);
////						} catch (InterruptedException e) {
////							e.printStackTrace();
////						}
////						postFile(fileName,requestURL);
////					}
////					@Override
////					public void onFinish() {
////						super.onFinish();
//////						Toast.makeText(MainActivity.this, "成功...........", Toast.LENGTH_LONG).show();
////					}
////				});
////			} catch (FileNotFoundException e) {
////				e.printStackTrace();
////			}
////		}
////	}
//}
