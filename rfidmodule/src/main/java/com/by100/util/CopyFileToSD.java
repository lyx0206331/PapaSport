package com.by100.util;

import android.content.Context;
import android.util.Log;
import com.adrian.rfidmodule.R;

import java.io.*;

public class CopyFileToSD {
	/**
	 * 初始化设备数据库
	 *
	 * @param context
	 * @return
	 */
	public void initDB(Context context) {
		File file = new File(AppConfig.WITLIB);
		File file1 = new File(AppConfig.LIC);
		File file2 = new File(AppConfig.RootFile);//临时文件夹
		if(!file2.exists()){
			file2.mkdir();
		}
		//检测文件是否存在，否则拷贝
		if (!file.exists()) {
			InitDeviceB(context);
		}
		if(!file1.exists()){
			InitDeviceL(context);
		}
	}
	/**
	 * 拷贝数据库
	 *
	 * @param context
	 * @return
	 */
	private boolean InitDeviceB(Context context) {
		InputStream input = null;
		OutputStream output = null;
		// 输出路径

		// 从资源中读取数据库流
		input = context.getResources().openRawResource(R.raw.base);

		try {
			output = new FileOutputStream(AppConfig.WITLIB);

			// 拷贝到输出流
			byte[] buffer = new byte[2048];
			int length;
			while ((length = input.read(buffer)) > 0) {
				output.write(buffer, 0, length);
			}

			return true;
		} catch (FileNotFoundException e) {
			Log.e("123", e.getMessage());
		} catch (IOException e) {
			Log.e("123", e.getMessage());
		} finally {
			// 关闭输出流
			try {
				output.flush();
				output.close();
				input.close();
			} catch (IOException e) {
			}

		}

		return false;

	}
	/**
	 * 拷贝数据库
	 *
	 * @param context
	 * @return
	 */
	private boolean InitDeviceL(Context context) {
		InputStream input = null;
		OutputStream output = null;
		// 输出路径

		// 从资源中读取数据库流
		input = context.getResources().openRawResource(R.raw.license);

		try {
			output = new FileOutputStream(AppConfig.LIC);

			// 拷贝到输出流
			byte[] buffer = new byte[2048];
			int length;
			while ((length = input.read(buffer)) > 0) {
				output.write(buffer, 0, length);
			}

			return true;
		} catch (FileNotFoundException e) {
			Log.e("123", e.getMessage());
		} catch (IOException e) {
			Log.e("123", e.getMessage());
		} finally {
			// 关闭输出流
			try {
				output.flush();
				output.close();
				input.close();
			} catch (IOException e) {
			}

		}

		return false;

	}
}
