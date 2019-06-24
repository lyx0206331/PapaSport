package com.adrian.nfcmodule;

import NFC.NdefMessageParser;
import NFC.ParsedNdefRecord;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.Settings;
import android.widget.TextView;
import android.widget.Toast;

import java.io.*;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * author:RanQing
 * date:2019/6/25 0025 2:07
 * description:
 **/
public class NFCUtils {

    private Context context;

    private INFCListener listener;

    private static final DateFormat TIME_FORMAT = SimpleDateFormat
            .getDateTimeInstance();
    private NfcAdapter mAdapter;
    private PendingIntent mPendingIntent;
    private NdefMessage mNdefPushMessage;
    private AlertDialog mDialog;

    public NFCUtils(Context context, INFCListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setListener(INFCListener listener) {
        this.listener = listener;
    }

    private void setPrompt(String msg) {
        if (listener != null) {
            listener.prompt(msg);
        }
    }

    public void onCreate() {
        resolveIntent(((Activity)context).getIntent());

        mDialog = new AlertDialog.Builder(context).setNeutralButton("Ok", null)
                .create();

        // 获取默认的NFC控制器
        mAdapter = NfcAdapter.getDefaultAdapter(context);

        //拦截系统级的NFC扫描，例如扫描蓝牙
        mPendingIntent = PendingIntent.getActivity(context, 0, new Intent(context,
                getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        mNdefPushMessage = new NdefMessage(new NdefRecord[] { newTextRecord("",
                Locale.ENGLISH, true) });

//		verifyStoragePermissions(this);
    }

    public void onResume() {
        if (mAdapter == null) {
            if (!mAdapter.isEnabled()) {
                showWirelessSettingsDialog();
            }

            showMessage("error", " NO NFC");

            setPrompt("设备不支持NFC！");

            return;
        }
        if (!mAdapter.isEnabled()) {
            listener.prompt("请在系统设置中先启用NFC功能！");
            return;
        }

        if (mAdapter != null) {
            //隐式启动
            mAdapter.enableForegroundDispatch((Activity)context, mPendingIntent, null, null);
            mAdapter.enableForegroundNdefPush((Activity)context, mNdefPushMessage);
        }
    }

    public void onPause() {
        if (mAdapter != null) {
            //隐式启动
            mAdapter.disableForegroundDispatch((Activity)context);
            mAdapter.disableForegroundNdefPush((Activity)context);
        }
    }

    //获取系统隐式启动的
    public void onNewIntent(Intent intent) {
        ((Activity)context).setIntent(intent);
        resolveIntent(intent);
    }
//	private static final int REQUEST_EXTERNAL_STORAGE = 1;
//	private static String[] PERMISSIONS_STORAGE = {
//			"android.permission.READ_EXTERNAL_STORAGE",
//			"android.permission.WRITE_EXTERNAL_STORAGE" };
//	// 检查读写权限
//	public static void verifyStoragePermissions(Activity activity) {
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

    //16进制字符串转换为String
    private String hexString = "0123456789ABCDEF";
    public String decode(String bytes) {
        if (bytes.length() != 30) {
            return null;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream(
                bytes.length() / 2);
        // 将每2位16进制整数组装成一个字节
        for (int i = 0; i < bytes.length(); i += 2)
            baos.write((hexString.indexOf(bytes.charAt(i)) << 4 | hexString
                    .indexOf(bytes.charAt(i + 1))));
        return new String(baos.toByteArray());
    }

    // 字符序列转换为16进制字符串
    private static String bytesToHexString(byte[] src, boolean isPrefix) {
        StringBuilder stringBuilder = new StringBuilder();
        if (isPrefix == true) {
            stringBuilder.append("0x");
        }
        if (src == null || src.length <= 0) {
            return null;
        }
        char[] buffer = new char[2];
        for (int i = 0; i < src.length; i++) {
            buffer[0] = Character.toUpperCase(Character.forDigit(
                    (src[i] >>> 4) & 0x0F, 16));
            buffer[1] = Character.toUpperCase(Character.forDigit(src[i] & 0x0F,
                    16));
            System.out.println(buffer);
            stringBuilder.append(buffer);
        }
        return stringBuilder.toString();
    }

    private void showMessage(String title, String message) {
        mDialog.setTitle(title);
        mDialog.setMessage(message);
        mDialog.show();
    }

    private NdefRecord newTextRecord(String text, Locale locale,
                                     boolean encodeInUtf8) {
        byte[] langBytes = locale.getLanguage().getBytes(
                Charset.forName("US-ASCII"));

        Charset utfEncoding = encodeInUtf8 ? Charset.forName("UTF-8") : Charset
                .forName("UTF-16");
        byte[] textBytes = text.getBytes(utfEncoding);

        int utfBit = encodeInUtf8 ? 0 : (1 << 7);
        char status = (char) (utfBit + langBytes.length);

        byte[] data = new byte[1 + langBytes.length + textBytes.length];
        data[0] = (byte) status;
        System.arraycopy(langBytes, 0, data, 1, langBytes.length);
        System.arraycopy(textBytes, 0, data, 1 + langBytes.length,
                textBytes.length);

        return new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT,
                new byte[0], data);
    }

    private void showWirelessSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("nfc_disabled");
        builder.setPositiveButton(android.R.string.ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(
                                Settings.ACTION_WIRELESS_SETTINGS);
                        context.startActivity(intent);
                    }
                });
        builder.setNegativeButton(android.R.string.cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ((Activity)context).finish();
                    }
                });
        builder.create().show();
        return;
    }

    //初步判断是什么类型NFC卡
    private void resolveIntent(Intent intent) {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            Parcelable[] rawMsgs = intent
                    .getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            NdefMessage[] msgs;
            if (rawMsgs != null) {
                msgs = new NdefMessage[rawMsgs.length];
                for (int i = 0; i < rawMsgs.length; i++) {
                    msgs[i] = (NdefMessage) rawMsgs[i];
                }
            } else {
                // Unknown tag type
                byte[] empty = new byte[0];
                byte[] id = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);
                Parcelable tag = intent
                        .getParcelableExtra(NfcAdapter.EXTRA_TAG);
                byte[] payload = dumpTagData(tag).getBytes();
                NdefRecord record = new NdefRecord(NdefRecord.TNF_UNKNOWN,
                        empty, id, payload);
                NdefMessage msg = new NdefMessage(new NdefRecord[] { record });
                msgs = new NdefMessage[] { msg };
            }
            // Setup the views
            listener.prompt("");
            buildTagViews(msgs);
        }
    }

    //一般公家卡，扫描的信息
    private String dumpTagData(Parcelable p) {
        StringBuilder sb = new StringBuilder();
        Tag tag = (Tag) p;
        byte[] id = tag.getId();
        sb.append("Tag ID (hex): ").append(getHex(id)).append("\n");
        sb.append("Tag ID (dec): ").append(getDec(id)).append("\n");
        sb.append("ID (reversed): ").append(getReversed(id)).append("\n");

//		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// HH:mm:ss
        //获取当前时间
//		Date date = new Date(System.currentTimeMillis());

//		String NFCID="NFCID:"+getDec(id)+",DATE:"+simpleDateFormat.format(date);

        // 读取已经保存的数据
//		String str = readSDFile();

        // 数据保存
//		saveDada2SD(str + "\n" + NFCID);

        String prefix = "android.nfc.tech.";
        sb.append("Technologies: ");
        for (String tech : tag.getTechList()) {
            sb.append(tech.substring(prefix.length()));
            sb.append(", ");
        }
        sb.delete(sb.length() - 2, sb.length());
        for (String tech : tag.getTechList()) {
            if (tech.equals(MifareClassic.class.getName())) {
                sb.append('\n');
                MifareClassic mifareTag = MifareClassic.get(tag);
                String type = "Unknown";
                switch (mifareTag.getType()) {
                    case MifareClassic.TYPE_CLASSIC:
                        type = "Classic";
                        break;
                    case MifareClassic.TYPE_PLUS:
                        type = "Plus";
                        break;
                    case MifareClassic.TYPE_PRO:
                        type = "Pro";
                        break;
                }
                sb.append("Mifare Classic type: ");
                sb.append(type);
                sb.append('\n');

                sb.append("Mifare size: ");
                sb.append(mifareTag.getSize() + " bytes");
                sb.append('\n');

                sb.append("Mifare sectors: ");
                sb.append(mifareTag.getSectorCount());
                sb.append('\n');

                sb.append("Mifare blocks: ");
                sb.append(mifareTag.getBlockCount());
            }

            if (tech.equals(MifareUltralight.class.getName())) {
                sb.append('\n');
                MifareUltralight mifareUlTag = MifareUltralight.get(tag);
                String type = "Unknown";
                switch (mifareUlTag.getType()) {
                    case MifareUltralight.TYPE_ULTRALIGHT:
                        type = "Ultralight";
                        break;
                    case MifareUltralight.TYPE_ULTRALIGHT_C:
                        type = "Ultralight C";
                        break;
                }
                sb.append("Mifare Ultralight type: ");
                sb.append(type);
            }
        }

        return sb.toString();
    }

    private String getHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = bytes.length - 1; i >= 0; --i) {
            int b = bytes[i] & 0xff;
            if (b < 0x10)
                sb.append('0');
            sb.append(Integer.toHexString(b));
            if (i > 0) {
                sb.append(" ");
            }
        }
        return sb.toString();
    }

    private long getDec(byte[] bytes) {
        long result = 0;
        long factor = 1;
        for (int i = 0; i < bytes.length; ++i) {
            long value = bytes[i] & 0xffl;
            result += value * factor;
            factor *= 256l;
        }
        return result;
    }

    private long getReversed(byte[] bytes) {
        long result = 0;
        long factor = 1;
        for (int i = bytes.length - 1; i >= 0; --i) {
            long value = bytes[i] & 0xffl;
            result += value * factor;
            factor *= 256l;
        }
        return result;
    }

    //显示NFC扫描的数据
    private void buildTagViews(NdefMessage[] msgs) {
        if (msgs == null || msgs.length == 0) {
            return;
        }
        // Parse the first message in the list
        // Build views for all of the sub records
        Date now = new Date();
        List<ParsedNdefRecord> records = NdefMessageParser.parse(msgs[0]);
        StringBuilder sb = new StringBuilder();
        final int size = records.size();
        for (int i = 0; i < size; i++) {
            TextView timeView = new TextView(context);
            timeView.setText(TIME_FORMAT.format(now));
            ParsedNdefRecord record = records.get(i);
            sb.append(record.getViewText());
//            promt.append(record.getViewText());
        }
        setPrompt(sb.toString());
    }

    /**
     *
     * 读取SD卡中文本文件
     *
     * @param fileName
     *
     * @return
     */
    @SuppressWarnings("resource")
    public String readSDFile() {
        try {
            File file = new File("/mnt/sdcard/pro.txt");
            FileInputStream is = new FileInputStream(file);
            byte[] b = new byte[is.available()];
            is.read(b);
            String result = new String(b);
            System.out.println("读取成功：" + result);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    // 保存至SD卡
    public void saveDada2SD(String sb) {
        String filePath = null;
        boolean hasSDCard = Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
        if (hasSDCard) { // SD卡根目录的hello.text
            filePath = "/mnt/sdcard/pro.txt";
        }
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                File dir = new File(file.getParent());
                dir.mkdirs();
                file.createNewFile();
            }
            FileOutputStream fileOut = null;
            BufferedOutputStream writer = null;
            OutputStreamWriter outputStreamWriter = null;
            BufferedWriter bufferedWriter = null;
            try {
                fileOut = new FileOutputStream(file);
                writer = new BufferedOutputStream(fileOut);
                outputStreamWriter = new OutputStreamWriter(writer, "UTF-8");
                bufferedWriter = new BufferedWriter(outputStreamWriter);
                bufferedWriter.write(new String(sb.toString()));
                bufferedWriter.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        Toast.makeText(context, "save success,text name is pro.txt", Toast.LENGTH_SHORT)
                .show();
    }

    public interface INFCListener {
        void prompt(String msg);
    }
}
