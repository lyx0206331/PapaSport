package com.by100.util;


import android.os.Environment;

public class AppConfig {
	/**
	 * 数据文件的根路径 目前定在外部存储卡中
	 */
	public static final String BasePath= Environment.getExternalStorageDirectory().getAbsolutePath();
	/**
	 * 数据库文件夹名称
	 */
	private static final String DBDirectoryName = "wltlib";
	/**
	 * 临时证据文件夹
	 */
	public static final String DBDirectoryNameL = "clog";

	/**
	 * 总文件夹的路径
	 */
	public static final String RootFile = BasePath+"/"+DBDirectoryName+"/";
	/**
	 * 总文件夹的路径
	 */
	public static final String RootFileL = BasePath+"/"+DBDirectoryNameL+"/";

	public static final String WITLIB = RootFile+"base.dat";

	public static final String LIC = RootFile+ "license.lic";

}
