package com.byid.android;


import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android_serialport_api.sample.SerialPortActivity;
import android_serialport_api.sample.SerialPortPreferences;
import com.adrian.rfidmodule.R;
import com.by100.util.AppConfig;
import com.by100.util.CopyFileToSD;
import com.by100.util.NationDeal;
import com.ivsign.android.IDCReader.IDCReaderSDK;
import android.zyapi.CommonApi;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.UUID;


public class ByIdActivity extends SerialPortActivity {
	int Readflage = -99;
	int datalen;
	boolean isRun = true;
	boolean isOpen = false;
	boolean isPlay;
	byte[] cmd_SAM = {(byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0x96, 0x69, 0x00, 0x03, 0x12, (byte) 0xFF, (byte) 0xEE  };
	byte[] cmd_find  = {(byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0x96, 0x69, 0x00, 0x03, 0x20, 0x01, 0x22  };
	byte[] cmd_selt  = {(byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0x96, 0x69, 0x00, 0x03, 0x20, 0x02, 0x21  };
	byte[] cmd_read  = {(byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0x96, 0x69, 0x00, 0x03, 0x30, 0x01, 0x32 };
	byte[] cmd_sleep  = {(byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0x96, 0x69, 0x00, 0x02, 0x00, 0x02};
	byte[] cmd_weak  = {(byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0x96, 0x69, 0x00, 0x02, 0x01, 0x03 };
	byte[] recData = new byte[1500];

	String DEVICE_NAME1 = "BY-100A";
	String DEVICE_NAME2 = "IDCReader";
	String DEVICE_NAME3 = "COM2";
	String DEVICE_NAME4 = "BOLUTEK";
	byte[] tempData;
	UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	String[] decodeInfo = new String[10];
	TextView ett;
	ImageView image;
	Button btread;
	MediaPlayer player;
	SharedPreferences prefs;

//    private static final int REQUEST_EXTERNAL_STORAGE = 1;
//	private static String[] PERMISSIONS_STORAGE = {
//			"android.permission.READ_EXTERNAL_STORAGE",
//			"android.permission.WRITE_EXTERNAL_STORAGE" };

	//	private String serialPortPath = "/dev/ttyMT2";
	CommonApi mCommonApi;
	private int mComFd =-1;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		CopyFileToSD cFileToSD = new CopyFileToSD();
		cFileToSD.initDB(this);

		prefs = PreferenceManager.getDefaultSharedPreferences(this);

		player = MediaPlayer.create(this,R.raw.success);

		mCommonApi=new CommonApi();

//		mCommonApi.setGpioDir(83,0);
//		mCommonApi.getGpioIn(83);
////
//		mCommonApi.setGpioDir(68,0);
//		mCommonApi.getGpioIn(68);
//
//		mCommonApi.setGpioDir(53,0);
//		mCommonApi.getGpioIn(53);

		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				mCommonApi.setGpioOut(53,1);

				mCommonApi.setGpioOut(83,1);

				int  ret1 = mCommonApi.setGpioOut(68,1);

				if(ret1 == 0){
					Toast.makeText(ByIdActivity.this, "设置成功" , Toast.LENGTH_SHORT).show();
				}else{
					Toast.makeText(ByIdActivity.this, "设置失败" , Toast.LENGTH_SHORT).show();
				}
			}
		}, 1000);

		Button btconn = (Button)findViewById(R.id.btconn);
		btread = (Button)findViewById(R.id.btread);
		ett = (TextView)findViewById(R.id.textView1);

		image = (ImageView)findViewById(R.id.imageView1);

		btconn.setOnClickListener(new Button.OnClickListener(){
			public void onClick(View v){
				startActivity(new Intent(ByIdActivity.this, SerialPortPreferences.class));
			}
		});
		btread.setOnClickListener(new Button.OnClickListener(){
			public void onClick(View v){
				isOpen = !isOpen;
				if(isOpen){
					btread.setText("停止");
				}else{
					btread.setText("读卡");
				}
			}
		});
		new Thread(new ThreadRun()).start();
	}

	private class ThreadRun implements Runnable {
		@Override
		public void run() {
			while(isRun){
				try{
					Thread.sleep(1000);
					if(isOpen)
						ReadCard();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	private void ReadCard(){
		try {
			if ((mInputStream == null) || (mInputStream == null)) {
				Readflage = -2;// 连接异常
				return;
			}
			mOutputStream.write(cmd_find);
			Thread.sleep(200);
			int datalen = mInputStream.read(recData);
			if (recData[9] == -97) {
				mOutputStream.write(cmd_selt);
				Thread.sleep(200);
				datalen = mInputStream.read(recData);
				if (recData[9] == -112) {
					mOutputStream.write(cmd_read);
					Thread.sleep(1000);
					byte[] tempData = new byte[1500];
					if (mInputStream.available() > 0) {
						datalen = mInputStream.read(tempData);
					} else {
						Thread.sleep(500);
						if (mInputStream.available() > 0) {
							datalen = mInputStream.read(tempData);
						}
					}
					int flag = 0;
					if (datalen < 1294) {
						for (int i = 0; i < datalen; i++, flag++) {
							recData[flag] = tempData[i];
						}
						Thread.sleep(1000);
						if (mInputStream.available() > 0) {
							datalen = mInputStream.read(tempData);
						} else {
							Thread.sleep(500);
							if (mInputStream.available() > 0) {
								datalen = mInputStream.read(tempData);
							}
						}
						for (int i = 0; i < datalen; i++, flag++) {
							recData[flag] = tempData[i];
						}
					} else {
						for (int i = 0; i < datalen; i++, flag++) {
							recData[flag] = tempData[i];
						}
					}
					tempData = null;
					if(flag == 1295){
						if (recData[9] == -112) {

							byte[] dataBuf = new byte[256];
							for (int i = 0; i < 256; i++) {
								dataBuf[i] = recData[14 + i];
							}
							String TmpStr = new String(dataBuf, "UTF16-LE");
							TmpStr = new String(TmpStr.getBytes("UTF-8"));
							decodeInfo[0] = TmpStr.substring(0, 15);
							decodeInfo[1] = TmpStr.substring(15, 16);
							decodeInfo[2] = TmpStr.substring(16, 18);
							decodeInfo[3] = TmpStr.substring(18, 26);
							decodeInfo[4] = TmpStr.substring(26, 61);
							decodeInfo[5] = TmpStr.substring(61, 79);
							decodeInfo[6] = TmpStr.substring(79, 94);
							decodeInfo[7] = TmpStr.substring(94, 102);
							decodeInfo[8] = TmpStr.substring(102, 110);
							decodeInfo[9] = TmpStr.substring(110, 128);
							if (decodeInfo[1].equals("1"))
								decodeInfo[1] = "男";
							else
								decodeInfo[1] = "女";
							try {
								int code = Integer.parseInt(decodeInfo[2]
										.toString());
								decodeInfo[2] = NationDeal.decodeNation(code);
							} catch (Exception e) {
								decodeInfo[2] = "";
							}
							// 照片解码
							try {
								int ret = IDCReaderSDK.Init();
								if (ret == 0){
									byte[] datawlt = new byte[1384];
									byte[] byLicData = { (byte) 0x05,
											(byte) 0x00, (byte) 0x01,
											(byte) 0x00, (byte) 0x5B,
											(byte) 0x03, (byte) 0x33,
											(byte) 0x01, (byte) 0x5A,
											(byte) 0xB3, (byte) 0x1E,
											(byte) 0x00 };
									for (int i = 0; i < 1295; i++) {
										datawlt[i] = recData[i];
									}
									int t = IDCReaderSDK.unpack(datawlt,
											byLicData);
									if (t == 1) {
										Readflage = 1;// 读卡成功
									} else {
										Readflage = 6;// 照片解码异常
									}
								} else {
									Readflage = 6;// 照片解码异常
								}
							} catch (Exception e) {
								Readflage = 6;// 照片解码异常
							}
							handler.sendEmptyMessage(0);
						} else {
							Readflage = -5;// 读卡失败！
						}
					} else {
						Readflage = -5;// 读卡失败
					}
				} else {
					Readflage = -4;// 选卡失败
				}
			} else {
				Readflage = -3;// 寻卡失败
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Readflage = -99;// 读取数据异常
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			Readflage = -99;// 读取数据异常
		}
	}

	private void sendMessage(byte[] outS){
		try {
			if(mOutputStream!=null){
				mOutputStream.write(outS);
				mOutputStream.write('\n');
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onDataReceived(byte[] buffer, int size) {
		// TODO Auto-generated method stub
		datalen = size;
		if(tempFlag==-1)
			recData = buffer;
	}

	private Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			if(msg.what!=0){
				return;
			}
			try {
				if(Readflage > 0) {
					ett.setText("姓名：" + decodeInfo[0] + "\n" + "性别："
							+ decodeInfo[1] + "\n" + "民族：" + decodeInfo[2]
							+ "\n" + "出生日期：" + decodeInfo[3] + "\n" + "地址："
							+ decodeInfo[4] + "\n" + "身份号码：" + decodeInfo[5]
							+ "\n" + "签发机关：" + decodeInfo[6] + "\n" + "有效期限："
							+ decodeInfo[7] + "-" + decodeInfo[8] + "\n"
							+ decodeInfo[9] + "\n");
					if (Readflage == 1) {
						FileInputStream fis = new FileInputStream(
								Environment.getExternalStorageDirectory()
										+ "/wltlib/zp.bmp");
						Bitmap bmp = BitmapFactory.decodeStream(fis);
						fis.close();
						image.setImageBitmap(bmp);
					} else {
						ett.append("照片解码失败，请检查路径"
								+ AppConfig.RootFile);
						image.setImageBitmap(BitmapFactory.decodeResource(
								getResources(), R.mipmap.face));
					}
					if(isPlay)
						player.start();



//					Bitmap btMap_save=loadBitmapFromView(linearLayout_x);
//
//					saveBitmapToSDCard(btMap_save, getRandomCharAndNumr(5));
				}else{
					image.setImageBitmap(BitmapFactory.decodeResource(
							getResources(), R.mipmap.face));
					if (Readflage == -2) {
						ett.setText("连接异常");
					}
					if (Readflage == -3) {
						ett.setText("无卡或卡片已读过");
					}
					if (Readflage == -4) {
						ett.setText("无卡或卡片已读过");
					}
					if (Readflage == -5) {
						ett.setText("读卡失败");
					}
					if (Readflage == -99) {
						ett.setText("操作异常");
					}
				}
				Thread.sleep(100);
			}
			catch (IOException e) {
				// TODO Auto-generated catch block
				ett.setText("读取数据异常！");
				image.setImageBitmap(BitmapFactory.decodeResource(
						getResources(), R.mipmap.face));
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				ett.setText("读取数据异常！");
				image.setImageBitmap(BitmapFactory.decodeResource(
						getResources(), R.mipmap.face));
			}
		}
	};
	/**
	 * 获取当前view的图像
	 * @param v
	 * @return
	 */
	public static Bitmap loadBitmapFromView(View v) {
		if (v == null) {
			return null;
		}
		Bitmap screenshot;
		screenshot = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_4444);
		Canvas canvas = new Canvas(screenshot);
		canvas.translate(-v.getScrollX(), -v.getScrollY());//我们在用滑动View获得它的Bitmap时候，获得的是整个View的区域（包括隐藏的），如果想得到当前区域，需要重新定位到当前可显示的区域
		v.draw(canvas);// 将 view 画到画布上
		return screenshot;
	}

//    /**
//     * 保存bitmap到SD卡
//     * @param bitmap
//     * @param imagename
//     */
//    public static String saveBitmapToSDCard(Bitmap bitmap, String imagename) {
//        String path = "/sdcard/" + "img-" + imagename + ".jpg";
//        FileOutputStream fos = null;
//        try {
//            fos = new FileOutputStream(path);
//            if (fos != null) {
//                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
//                fos.close();
//            }
//
//            return path;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }

//    /**
//     * 检查读写权限
//     * @param activity
//     */
//	public static void verifyStoragePermissions(Activity activity) {
//
//		try {
//			// 检测是否有写的权限
//			int permission = ActivityCompat.checkSelfPermission(activity,
//					"android.permission.WRITE_EXTERNAL_STORAGE");
//			if (permission != PackageManager.PERMISSION_GRANTED) {
//				// 没有写的权限，去申请写的权限，会弹出对话框
//				ActivityCompat.requestPermissions(activity,
//						PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}

//	/**
//	 * 获取随机字母数字组合
//	 *
//	 * @param length
//	 *            字符串长度
//	 * @return
//	 */
//	public static String getRandomCharAndNumr(Integer length) {
//	    String str = "";
//	    Random random = new Random();
//	    for (int i = 0; i < length; i++) {
//	        boolean b = random.nextBoolean();
//	        if (b) { // 字符串
//	            // int choice = random.nextBoolean() ? 65 : 97; 取得65大写字母还是97小写字母
//	            str += (char) (65 + random.nextInt(26));// 取得大写字母
//	        } else { // 数字
//	            str += String.valueOf(random.nextInt(10));
//	        }
//	    }
//	    return str;
//	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		isPlay = prefs.getBoolean("checkbox",true);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		isRun = false;
//		if(mComFd>0){
		mCommonApi.setGpioDir(83,1);
		mCommonApi.setGpioOut(83,0);
		//设置启用GPIO口为53，也就是使GPIO 53口生效
		mCommonApi.setGpioDir(53,1);
		//拉低53口电压，断开对身份证模块的供电
		mCommonApi.setGpioOut(53,0);
//			Toast.makeText(getApplicationContext(), "退出", 0).show();
		mCommonApi.setGpioDir(68,1);
		mCommonApi.setGpioOut(68,0);
		//关闭mCommonApi类
		mCommonApi.closeCom(mComFd);
//		}
		super.onDestroy();
	}


}