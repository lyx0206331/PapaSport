package qs.demo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.posapi.PosApi;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.adrian.printmodule.PrintUtils;
import com.adrian.printmodule.R;

/**
 * 群索PDA 扫描和打印 示范例子
 *
 * 注意：使用本例子之前，由于出厂自带的3505和5501是一直运行于后台的，会一直占用串口
 * 需要先卸掉3505和5501，避免打印和扫描串口冲突，从而导致打印失败甚至延迟
 *
 * 当安装APK时候出现Installation failed with message Invalid File:问题时，
 * 解决办法如下：
 * 1.点击工具栏上的Build中的Clean Project
 * 2.再点击工具栏上的Build中的Rebulid Project!
 * @author wsl
 *
 */
public class ScanPrintAcivity extends Activity {

	private Button mBtnOpen = null;
	private Button mBtnClose = null;
	private Button mBtnScan = null;
	private EditText mTv = null;

	MediaPlayer player;

	String str, str2, str3;

	private static final int REQUEST_EXTERNAL_STORAGE = 1;
	private static String[] PERMISSIONS_STORAGE = {
			"android.permission.READ_EXTERNAL_STORAGE",
			"android.permission.WRITE_EXTERNAL_STORAGE" };

    private PrintUtils printUtil;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.scan_layout1);

        // 初始化控件
        initViews();

        printUtil = new PrintUtils(this, new PrintUtils.IPrintListener() {
			@Override
            public void onScanSuccess(String msg) {
                // 开启提示音，提示客户条码或者二维码已经被扫到
                player.start();

                // 显示到编辑框中
                mTv.setText(msg);
			}

			@Override
			public void onFailed(int state) {
				switch (state) {
					case PosApi.ERR_POS_PRINT_NO_PAPER:
						// 打印缺纸
						showTip("打印缺纸");
						break;
					case PosApi.ERR_POS_PRINT_FAILED:
						// 打印失败
						showTip("打印失败");
						break;
					case PosApi.ERR_POS_PRINT_VOLTAGE_LOW:
						// 电压过低
						showTip("电压过低");
						break;
					case PosApi.ERR_POS_PRINT_VOLTAGE_HIGH:
						// 电压过高
						showTip("电压过高");
						break;
				}
			}

			@Override
            public void onFinish() {
                Toast.makeText(getApplicationContext(), "打印完成",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onGetState(int i) {

			}

			@Override
			public void onPrinterSetting(int state) {
				switch (state) {
					case 0:
						Toast.makeText(ScanPrintAcivity.this, "持续有纸",
								Toast.LENGTH_SHORT).show();
						break;
					case 1:
						Toast.makeText(ScanPrintAcivity.this, "缺纸",
								Toast.LENGTH_SHORT).show();
						break;
					case 2:
						Toast.makeText(ScanPrintAcivity.this, "检测到黑标",
								Toast.LENGTH_SHORT).show();
						break;
				}
			}
		});

		// 扫描提示音
		player = MediaPlayer.create(getApplicationContext(),
				R.raw.beep);

	}

	// 控件初始化
	private void initViews() {

		mBtnOpen = (Button) this.findViewById(R.id.btn_open);
		mBtnClose = (Button) this.findViewById(R.id.btn_close);
		mBtnScan = (Button) this.findViewById(R.id.btn_scan);
		mTv = (EditText) this.findViewById(R.id.tv);

		mBtnClose.setText("打印二维码");

		mBtnOpen.setText("打印文字");

		mBtnScan.setText("打印一维码");

		mBtnOpen.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

                // 获取编辑框中的字符串
                str2 = mTv.getText().toString().trim();
				// 打印文字
                printUtil.printText(str2);

			}
		});

		mBtnClose.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

                // 获取编辑框中的字符串
                str2 = mTv.getText().toString().trim();
				// 打印二维码
                printUtil.printQRCode(str2);

			}
		});

		mBtnScan.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

                // 获取编辑框中的字符串
                str2 = mTv.getText().toString().trim();
				// 打印一维码
                printUtil.printBarCode(str2);

			}
		});

		// 扫描按键监听
		findViewById(R.id.btn_scan1).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
                        printUtil.scanDomn();
					}
				});

		// 获取权限，在安卓6.0或者以上系统的机器上需要进行该操作，否则将会出现无法读写SD卡的情况
		verifyStoragePermissions(this);

	}

	// 检查读写权限
	public static void verifyStoragePermissions(Activity activity) {
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
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		// after 1000ms openDevice
		// 必须延迟一秒，否则将会出现第一次扫描和打印延迟的现象
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				// 打开GPIO，给扫描头上电
                printUtil.openDevice();

			}
		}, 1000);

		super.onResume();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();

        printUtil.release();
	}

	/**
	 * 弹出提示框
	 *
	 * @param msg
	 */
	private void showTip(String msg) {
		new AlertDialog.Builder(this).setTitle("提示").setMessage(msg)
				.setNegativeButton("关闭", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						dialog.dismiss();
						// isCanPrint=true;
					}
				}).show();
	}

}
