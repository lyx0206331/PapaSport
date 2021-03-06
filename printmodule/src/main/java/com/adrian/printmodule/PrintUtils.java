package com.adrian.printmodule;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.*;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.posapi.PosApi;
import android.posapi.PrintQueue;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.jetbrains.annotations.NotNull;
import qs.util.BarcodeCreater;
import qs.util.BitmapTools;

import java.io.*;
import java.util.HashMap;
import java.util.Hashtable;

/**
 * author:RanQing
 * date:2019/6/27 0027 1:45
 * description:Pos机工具类.相关功能分离
 **/
public class PrintUtils {

    private Context context;

    private PosApi mPosSDK = null;

    private byte mGpioPower = 0x1E;// PB14
    private byte mGpioTrig = 0x29;// PC9

    private int mCurSerialNo = 3; // usart3
    private int mBaudrate = 4; // 9600

    private ScanBroadcastReceiver scanBroadcastReceiver;

    private PrintQueue mPrintQueue = null;

    private Bitmap btMap;

    private IPrintListener listener;

    /**
     * 物理SCAN按键监听
     */
    boolean isScan = false;

    class ScanBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            //监听到SCAN按键按下广播，执行扫描
            scanDomn();
        }
    }

    /**
     * 扫描信息获取
     */
    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            String action = intent.getAction();
            if (action.equalsIgnoreCase(PosApi.ACTION_POS_COMM_STATUS)) {

                // 串口标志判断
                int cmdFlag = intent.getIntExtra(PosApi.KEY_CMD_FLAG, -1);

                // 获取串口返回的字节数组
                byte[] buffer = intent
                        .getByteArrayExtra(PosApi.KEY_CMD_DATA_BUFFER);

                switch (cmdFlag) {
                    // 如果为扫描数据返回串口
                    case PosApi.POS_EXPAND_SERIAL3:
                        if (buffer == null)
                            return;
                        try {
                            // 将字节数组转成字符串
                            String str = new String(buffer, "GBK");

                            listener.onScanSuccess(str);

                        } catch (UnsupportedEncodingException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        break;

                }
                // 扫描完本次后清空，以便下次扫描
                buffer = null;

            }
        }

    };

    public interface IPrintListener extends PrintQueue.OnPrintListener {
        void onScanSuccess(String msg);
    }

    public PrintUtils(Context context, IPrintListener listener) {
        this.context = context;
        this.listener = listener;

        init();
    }

    public void init() {
        // 获取PosApi实例
        mPosSDK = PosApi.getInstance(context);

        // 根据型号进行初始化mPosApi类
        if (Build.MODEL.contains("LTE")
                || android.os.Build.DISPLAY.contains("3508")
                || android.os.Build.DISPLAY.contains("403")
                || android.os.Build.DISPLAY.contains("35S09")) {
            mPosSDK.initPosDev("ima35s09");
        } else if (Build.MODEL.contains("5501")) {
            mPosSDK.initPosDev("ima35s12");
        } else {
            mPosSDK.initPosDev(PosApi.PRODUCT_MODEL_IMA80M01);
        }

        //监听初始化回调结果
        mPosSDK.setOnComEventListener(mCommEventListener);
        // 打印队列初始化
        mPrintQueue = new PrintQueue(context, mPosSDK);
        // 打印队列初始化
        mPrintQueue.init();
        // 打印结果监听
        mPrintQueue.setOnPrintListener(listener);

        //注册获取扫描信息的广播接收器
        IntentFilter mFilter = new IntentFilter();
        mFilter.addAction(PosApi.ACTION_POS_COMM_STATUS);
        context.registerReceiver(receiver, mFilter);

        // 物理扫描键按下时候会有动作为ismart.intent.scandown的广播发出，可监听该广播实现触发扫描动作
        scanBroadcastReceiver = new ScanBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("ismart.intent.scandown");
        context.registerReceiver(scanBroadcastReceiver, intentFilter);
    }

    public void release() {
        // 注销获取扫描数据的广播
        context.unregisterReceiver(receiver);

        // 注销物理SCAN按键的接收广播
        context.unregisterReceiver(scanBroadcastReceiver);

        // 关闭下层串口以及打印机
        mPosSDK.closeDev();

        if (mPrintQueue != null) {
            // 打印队列关闭
            mPrintQueue.close();
        }
    }

    // 部分参考方法,例如字体设置，对齐方式等方法
    private void test() {

        int concentration = 60;// 打印浓度
        StringBuilder sb = new StringBuilder();
        sb.append("1234567890\n");
        PrintQueue.TextData tData = mPrintQueue.new TextData();// 构造TextData实例
        tData.addParam(PrintQueue.PARAM_TEXTSIZE_2X);// 设置两倍字体大小
        tData.addText(sb.toString());// 添加打印内容
        mPrintQueue.addText(concentration, tData);// 添加到打印队列

        tData = mPrintQueue.new TextData();// 构造TextData实例
        tData.addParam(PrintQueue.PARAM_TEXTSIZE_1X);// 设置一倍字体大小
        tData.addParam(PrintQueue.PARAM_ALIGN_MIDDLE);// 设置居中对齐
        tData.addText(sb.toString());
        mPrintQueue.addText(concentration, tData);

        tData = mPrintQueue.new TextData();
        tData.addParam(PrintQueue.PARAM_ALIGN_RIGHT);// 设置右对齐
        tData.addText(sb.toString());
        mPrintQueue.addText(concentration, tData);

        tData = mPrintQueue.new TextData();
        tData.addParam(PrintQueue.PARAM_TEXTSIZE_1X);// 设置一倍字体大小
        tData.addParam(PrintQueue.PARAM_UNDERLINE);// 下划线
        tData.addText(sb.toString());
        mPrintQueue.addText(concentration, tData);

        tData = mPrintQueue.new TextData();
        tData.addParam(PrintQueue.PARAM_ALIGN_MIDDLE);// 设置居中对齐
        tData.addParam(PrintQueue.PARAM_UNDERLINE);// 下划线
        tData.addParam(PrintQueue.PARAM_TEXTSIZE_2X);// 设置两倍字体大小
        tData.addText(sb.toString());
        mPrintQueue.addText(concentration, tData);

        tData = mPrintQueue.new TextData();
        tData.addText(sb.toString());// 直接添加打印内容 不设置参数
        mPrintQueue.addText(concentration, tData);

        mPrintQueue.printStart();// 开始队列打印

    }

    // 打开串口以及GPIO口
    public void openDevice() {
        // open power
        mPosSDK.gpioControl(mGpioPower, 0, 1);

        mPosSDK.extendSerialInit(mCurSerialNo, mBaudrate, 1, 1, 1, 1);

    }

    /**
     * 打印文字
     */
    public void printText(String text) {

        try {
            // 获取编辑框中的字符串
//            str2 = mTv.getText().toString().trim();

            // 直接把字符串转成byte数组，然后添加到打印队列，这里打印多个\n是为了打印的文字能够出到外面，方便客户看到
            addPrintTextWithSize(1, 50, (text + "\n\n\n\n\n\n\n").getBytes("GBK"));

            // 打印队列启动
            mPrintQueue.printStart();

        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    /**
     * 打印一维码
     */
    public void printBarCode(String msg) {

        // 获取编辑框中的字符串
//        str2 = mTv.getText().toString().trim();
        if (msg == null || msg.length() <= 0)
            return;

        // 判断当前字符能否生成条码
        if (msg.getBytes().length > msg.length()) {
            Toast.makeText(context, "当前数据不能生成一维码", Toast.LENGTH_SHORT).show();
            return;
        }
        try {

            // 生成条码图片
            btMap = BarcodeCreater.creatBarcode(context, msg.trim(), 300, 100,
                    true, 1);
            // 条码图片转成打印字节数组
            byte[] printData = BitmapTools.bitmap2PrinterBytes(btMap);
            // 将打印数组添加到打印队列
            mPrintQueue.addBmp(60, 0, btMap.getWidth(), btMap.getHeight(),
                    printData);
            // 打印6个空行，使一维码显示到打印头外面
            mPrintQueue.addText(50, "\n\n\n\n\n\n".getBytes("GBK"));

        } catch (UnsupportedEncodingException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        // 打印队列开始
        mPrintQueue.printStart();

    }

    public void print(PrintInfo printInfo) {
        if (printInfo == null || (printInfo.getPayInfo() == null && printInfo.getTicketInfo() == null)) {
            return;
        }
        if (printInfo.getPayInfo() != null) {
            PrintQueue.TextData payTitleData = mPrintQueue.new TextData();
            payTitleData.addParam(PrintQueue.PARAM_ALIGN_MIDDLE);
            payTitleData.addText(printInfo.getPayInfo().getFieldName());
            mPrintQueue.addText(50, payTitleData);

            PrintQueue.TextData bodyData = mPrintQueue.new TextData();
            bodyData.addParam(PrintQueue.PARAM_ALIGN_LEFT);
            bodyData.addText(printInfo.getPayInfo().getPrintContent());
            mPrintQueue.addText(50, bodyData);
        }

        if (printInfo.getTicketInfo() != null && printInfo.getTicketInfo().size() > 0) {
            for (QrCodeTicketInfo qrInfo :
                    printInfo.getTicketInfo()) {
                try {

//                    // 二维码生成
//                    Bitmap btMap = encode2dAsBitmap(qrInfo.getTicketNum(), 300, 300, 2);
//
//                    // 图片转成打印字节数组
//                    byte[] printData = BitmapTools.bitmap2PrinterBytes(btMap);

                    PrintQueue.TextData titleData = mPrintQueue.new TextData();
                    titleData.addParam(PrintQueue.PARAM_ALIGN_MIDDLE);
                    titleData.addText(qrInfo.getTicketName());
                    mPrintQueue.addText(50, titleData);

                    PrintQueue.TextData dividerData = mPrintQueue.new TextData();
                    dividerData.addParam(PrintQueue.PARAM_ALIGN_LEFT);
                    dividerData.addText("\n     ----------------------\n");
                    mPrintQueue.addText(50, dividerData);

                    // 二维码生成
                    Bitmap btMap = encode2dAsBitmap(qrInfo.getTicketNum(), 300, 300, 2);

                    // 图片转成打印字节数组
                    byte[] printData = BitmapTools.bitmap2PrinterBytes(btMap);
                    // 将打印数组添加到打印队列
                    mPrintQueue.addBmp(50, 50, btMap.getWidth(), btMap.getHeight(),
                            printData);

                    PrintQueue.TextData numData = mPrintQueue.new TextData();
                    numData.addParam(PrintQueue.PARAM_ALIGN_MIDDLE);
                    numData.addText("\n票号：" + qrInfo.getTicketNum());
                    mPrintQueue.addText(50, numData);
//
                    // 打印3个空行，使二维码显示到打印头外面
                    mPrintQueue.addText(50, "\n\n\n".getBytes("GBK"));

                } catch (UnsupportedEncodingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        // 打印队列开始
        mPrintQueue.printStart();
    }

    /**
     * 打印支付凭证
     *
     * @param paymentVoucherInfo
     */
    public void printPaymentVoucher(@NotNull PaymentVoucherInfo paymentVoucherInfo) {
        PrintQueue.TextData titleData = mPrintQueue.new TextData();
        titleData.addParam(PrintQueue.PARAM_ALIGN_MIDDLE);
        titleData.addText(paymentVoucherInfo.getFieldName());
        mPrintQueue.addText(30, titleData);

        PrintQueue.TextData bodyData = mPrintQueue.new TextData();
        bodyData.addParam(PrintQueue.PARAM_ALIGN_LEFT);
        bodyData.addText(paymentVoucherInfo.getPrintContent());
        mPrintQueue.addText(30, bodyData);

        mPrintQueue.printStart();
    }

    /**
     * 打印二维码门票
     *
     * @param qrCodeTicketInfo
     */
    public void printQrCodeTicket(@NotNull QrCodeTicketInfo qrCodeTicketInfo) {
        if (TextUtils.isEmpty(qrCodeTicketInfo.getTicketNum()) || TextUtils.isEmpty(qrCodeTicketInfo.getTicketName())) {
            return;
        }

        try {

            // 二维码生成
            btMap = encode2dAsBitmap(qrCodeTicketInfo.getTicketNum(), 300, 300, 2);

            // 图片转成打印字节数组
            byte[] printData = BitmapTools.bitmap2PrinterBytes(btMap);

            PrintQueue.TextData titleData = mPrintQueue.new TextData();
            titleData.addParam(PrintQueue.PARAM_ALIGN_MIDDLE);
            titleData.addText(qrCodeTicketInfo.getTicketName());
            mPrintQueue.addText(30, titleData);

            PrintQueue.TextData dividerData = mPrintQueue.new TextData();
            dividerData.addParam(PrintQueue.PARAM_ALIGN_LEFT);
            dividerData.addText("\n     ----------------------\n");
            mPrintQueue.addText(30, dividerData);

            // 将打印数组添加到打印队列
            mPrintQueue.addBmp(50, 50, btMap.getWidth(), btMap.getHeight(),
                    printData);

            PrintQueue.TextData numData = mPrintQueue.new TextData();
            numData.addParam(PrintQueue.PARAM_ALIGN_MIDDLE);
            numData.addText("\n票号：" + qrCodeTicketInfo.getTicketNum());
            mPrintQueue.addText(30, numData);
//
            // 打印6个空行，使二维码显示到打印头外面
            mPrintQueue.addText(50, "\n\n\n".getBytes("GBK"));

        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // 打印队列开始
        mPrintQueue.printStart();
    }

    /**
     * 打印二维码
     */
    public void printQRCode(String msg) {

        // 获取编辑框中的字符串
//        str2 = mTv.getText().toString().trim();

        if (msg == null || msg.length() <= 0)
            return;

        try {

            // 二维码生成
            btMap = encode2dAsBitmap(msg, 300, 300, 2);

            // 二维码图片转成打印字节数组
            byte[] printData = BitmapTools.bitmap2PrinterBytes(btMap);

            // 将打印数组添加到打印队列
            mPrintQueue.addBmp(50, 50, btMap.getWidth(), btMap.getHeight(),
                    printData);

            // 打印6个空行，使二维码显示到打印头外面
            mPrintQueue.addText(50, "\n\n\n\n\n\n".getBytes("GBK"));

        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // 打印队列开始
        mPrintQueue.printStart();

    }

    private void saveBmp2Sdcard(Bitmap bmp) {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/二维码_" + System.currentTimeMillis() + ".png";
        File f = new File(path);
        try {
            f.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "在保存图片时出错：" + e.toString(), Toast.LENGTH_SHORT).show();
        }
        FileOutputStream fOut = null;
        try {
            fOut = new FileOutputStream(f);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        bmp.compress(Bitmap.CompressFormat.PNG, 100, fOut);
        try {
            fOut.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            fOut.close();
            Toast.makeText(context, "二维码成功保存到：" + path, Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void releaseDevice() {
        // 注销获取扫描数据的广播
        context.unregisterReceiver(receiver);

        // 注销物理SCAN按键的接收广播
        context.unregisterReceiver(scanBroadcastReceiver);

        // 关闭下层串口以及打印机
        mPosSDK.closeDev();

        if (mPrintQueue != null) {
            // 打印队列关闭
            mPrintQueue.close();
        }
    }

    /**
     * 初始化
     */
    PosApi.OnCommEventListener mCommEventListener = new PosApi.OnCommEventListener() {
        @Override
        public void onCommState(int cmdFlag, int state, byte[] resp, int respLen) {
            // TODO Auto-generated method stub
            switch (cmdFlag) {
                case PosApi.POS_INIT:
                    if (state == PosApi.COMM_STATUS_SUCCESS) {
                        Toast.makeText(context, "设备初始化成功",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "设备初始化失败",
                                Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    };

    /**
     * 执行扫描，扫描后的结果会通过action为PosApi.ACTION_POS_COMM_STATUS的广播发回
     */
    public void scanDomn() {
        if (!isScan) {
            mPosSDK.gpioControl(mGpioTrig, 0, 0);
            isScan = true;
            handler.removeCallbacks(run);
            // 3秒后还没有扫描到信息则强制关闭扫描头
            handler.postDelayed(run, 3000);
        } else {
            mPosSDK.gpioControl(mGpioTrig, 0, 1);
            mPosSDK.gpioControl(mGpioTrig, 0, 0);
            isScan = true;
            handler.removeCallbacks(run);
            // 3秒后还没有扫描到信息则强制关闭扫描头
            handler.postDelayed(run, 3000);
        }
    }

    Handler handler = new Handler();
    Runnable run = new Runnable() {
        @Override
        public void run() {
            // TODO Auto-generated method stub
            // 强制关闭扫描头
            mPosSDK.gpioControl(mGpioTrig, 0, 1);
            isScan = false;
        }
    };

    /**
     * 字符串转成GBK
     *
     * @param str
     * @return
     * @throws UnsupportedEncodingException
     */
    public String toGBK(String str) throws UnsupportedEncodingException {
        return this.changeCharset(str, "GBK");
    }

    /**
     * 字符串编码转换的实现方法
     *
     * @param str        待转换编码的字符串
     * @param newCharset 目标编码
     * @return
     * @throws UnsupportedEncodingException
     */
    public String changeCharset(String str, String newCharset)
            throws UnsupportedEncodingException {
        if (str != null) {
            // 用默认字符编码解码字符串。
            byte[] bs = str.getBytes();
            // 用新的字符编码生成字符串
            return new String(bs, newCharset);
        }
        return null;
    }

    /**
     * 生成二维码 要转换的地址或字符串,可以是中文
     *
     * @param url
     * @param width
     * @param height
     * @return
     */
    public Bitmap createQRImage(String url, int width, int height) {
        try {
            // 判断URL合法性
            if (url == null || "".equals(url) || url.length() < 1) {
                return null;
            }
            Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>();
            hints.put(EncodeHintType.CHARACTER_SET, "GBK");
            // 图像数据转换，使用了矩阵转换
            BitMatrix bitMatrix = new QRCodeWriter().encode(url,
                    BarcodeFormat.QR_CODE, width, height, hints);
            // bitMatrix = deleteWhite(bitMatrix);// 删除白边
            bitMatrix = deleteWhite(bitMatrix);// 删除白边
            width = bitMatrix.getWidth();
            height = bitMatrix.getHeight();
            int[] pixels = new int[width * height];
            // 下面这里按照二维码的算法，逐个生成二维码的图片，
            // 两个for循环是图片横列扫描的结果
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (bitMatrix.get(x, y)) {
                        pixels[y * width + x] = 0xff000000;
                    } else {
                        pixels[y * width + x] = 0xffffffff;
                    }
                }
            }
            // 生成二维码图片的格式，使用ARGB_8888
            Bitmap bitmap = Bitmap
                    .createBitmap(width, height, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 生成去除白边的二维码
     *
     * @param str
     * @param width
     * @param height
     * @return
     */
    public static Bitmap create2DCode(String str, int width, int height) {
        try {
            Hashtable<EncodeHintType, Object> hints = new Hashtable<EncodeHintType, Object>();
            hints.put(EncodeHintType.CHARACTER_SET, "GBK");
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            BitMatrix matrix = new QRCodeWriter().encode(str,
                    BarcodeFormat.QR_CODE, width, height);
            matrix = deleteWhite(matrix);// 删除白边
            width = matrix.getWidth();
            height = matrix.getHeight();
            int[] pixels = new int[width * height];
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (matrix.get(x, y)) {
                        pixels[y * width + x] = Color.BLACK;
                    } else {
                        pixels[y * width + x] = Color.WHITE;
                    }
                }
            }
            Bitmap bitmap = Bitmap.createBitmap(width, height,
                    Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
            return bitmap;
        } catch (Exception e) {
            return null;
        }
    }

    private static BitMatrix deleteWhite(BitMatrix matrix) {
        int[] rec = matrix.getEnclosingRectangle();
        int resWidth = rec[2] + 1;
        int resHeight = rec[3] + 1;

        BitMatrix resMatrix = new BitMatrix(resWidth, resHeight);
        resMatrix.clear();
        for (int i = 0; i < resWidth; i++) {
            for (int j = 0; j < resHeight; j++) {
                if (matrix.get(i + rec[0], j + rec[1]))
                    resMatrix.set(i, j);
            }
        }
        return resMatrix;
    }

    /**
     * 文字转图片
     *
     * @param str
     * @return
     */
    public Bitmap word2bitmap(String str) {

        Bitmap bMap = Bitmap.createBitmap(300, 80, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bMap);
        canvas.drawColor(Color.WHITE);
        TextPaint textPaint = new TextPaint();
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(40.0F);
        StaticLayout layout = new StaticLayout(str, textPaint, bMap.getWidth(),
                Layout.Alignment.ALIGN_NORMAL, (float) 1.0, (float) 0.0, true);
        layout.draw(canvas);

        return bMap;

    }

    /**
     * 两张图片上下合并成一张
     *
     * @param bitmap1
     * @param bitmap2
     * @return
     */
    public Bitmap twoBtmap2One(Bitmap bitmap1, Bitmap bitmap2) {
        Bitmap bitmap3 = Bitmap.createBitmap(bitmap1.getWidth(),
                bitmap1.getHeight() + bitmap2.getHeight(), bitmap1.getConfig());
        Canvas canvas = new Canvas(bitmap3);
        canvas.drawBitmap(bitmap1, new Matrix(), null);
        canvas.drawBitmap(bitmap2, 0, bitmap1.getHeight(), null);
        return bitmap3;
    }

    /**
     * 文字转图片
     *
     * @param text     将要生成图片的内容
     * @param textSize 文字大小
     * @return
     */
    public static Bitmap textAsBitmap(String text, float textSize) {

        TextPaint textPaint = new TextPaint();

        textPaint.setColor(Color.BLACK);

        textPaint.setTextSize(textSize);

        StaticLayout layout = new StaticLayout(text, textPaint, 380,
                Layout.Alignment.ALIGN_NORMAL, 1.3f, 0.0f, true);
        Bitmap bitmap = Bitmap.createBitmap(layout.getWidth() + 20,
                layout.getHeight() + 20, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.translate(10, 10);
        canvas.drawColor(Color.WHITE);

        layout.draw(canvas);
        Log.d("textAsBitmap",
                String.format("1:%d %d", layout.getWidth(), layout.getHeight()));
        return bitmap;
    }

    /*
     * 打印文字 size 1 --倍大小 2--2倍大小
     */
    private void addPrintTextWithSize(int size, int concentration, byte[] data) {
        if (data == null)
            return;
        // 2倍字体大小
        byte[] _2x = new byte[]{0x1b, 0x57, 0x02};
        // 1倍字体大小
        byte[] _1x = new byte[]{0x1b, 0x57, 0x01};
        byte[] mData = null;
        if (size == 1) {
            mData = new byte[3 + data.length];
            // 1倍字体大小 默认
            System.arraycopy(_1x, 0, mData, 0, _1x.length);
            System.arraycopy(data, 0, mData, _1x.length, data.length);

            mPrintQueue.addText(concentration, mData);

        } else if (size == 2) {
            mData = new byte[3 + data.length];
            // 1倍字体大小 默认
            System.arraycopy(_2x, 0, mData, 0, _2x.length);
            System.arraycopy(data, 0, mData, _2x.length, data.length);

            mPrintQueue.addText(concentration, mData);

        }

    }

    /**
     * 图片旋转
     *
     * @param bm
     * @param orientationDegree
     * @return
     */
    Bitmap adjustPhotoRotation(Bitmap bm, final int orientationDegree) {

        Matrix m = new Matrix();
        m.setRotate(orientationDegree, (float) bm.getWidth() / 2,
                (float) bm.getHeight() / 2);
        float targetX, targetY;
        if (orientationDegree == 90) {
            targetX = bm.getHeight();
            targetY = 0;
        } else {
            targetX = bm.getHeight();
            targetY = bm.getWidth();
        }

        final float[] values = new float[9];
        m.getValues(values);

        float x1 = values[Matrix.MTRANS_X];
        float y1 = values[Matrix.MTRANS_Y];

        m.postTranslate(targetX - x1, targetY - y1);

        Bitmap bm1 = Bitmap.createBitmap(bm.getHeight(), bm.getWidth(),
                Bitmap.Config.ARGB_8888);
        Paint paint = new Paint();
        Canvas canvas = new Canvas(bm1);
        canvas.drawBitmap(bm, m, paint);

        return bm1;
    }

    /**
     * 生成二维码
     *
     * @param contents      二维码内容
     * @param desiredWidth  二维码宽度
     * @param desiredHeight 二维码高度
     * @param barType       条码类型
     * @return
     */
    public static Bitmap encode2dAsBitmap(String contents, int desiredWidth,
                                          int desiredHeight, int barType) {
        BarcodeFormat barcodeFormat = BarcodeFormat.CODE_128;
        if (barType == 1) {
            barcodeFormat = BarcodeFormat.CODE_128;
        } else if (barType == 2) {
            barcodeFormat = BarcodeFormat.QR_CODE;
        }

        Bitmap barcodeBitmap = null;
        try {
            barcodeBitmap = encodeAsBitmap(contents, barcodeFormat,
                    desiredWidth, desiredHeight);
        } catch (WriterException e) {
            e.printStackTrace();
        }

        return barcodeBitmap;
    }

    public static Bitmap encodeAsBitmap(String contents, BarcodeFormat format,
                                        int desiredWidth, int desiredHeight) throws WriterException {
        final int WHITE = 0xFFFFFFFF;
        final int BLACK = 0xFF000000;

        HashMap<EncodeHintType, String> hints = null;
        String encoding = guessAppropriateEncoding(contents);
        if (encoding != null) {
            hints = new HashMap<EncodeHintType, String>(2);
            hints.put(EncodeHintType.CHARACTER_SET, encoding);
        }
        MultiFormatWriter writer = new MultiFormatWriter();
        BitMatrix result = writer.encode(contents, format, desiredWidth,
                desiredHeight, hints);
        result = deleteWhite(result);// 删除白边
        int width = result.getWidth();
        int height = result.getHeight();
        int[] pixels = new int[width * height];
        // All are 0, or black, by default
        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }

    public static String guessAppropriateEncoding(CharSequence contents) {
        // Very crude at the moment
        for (int i = 0; i < contents.length(); i++) {
            if (contents.charAt(i) > 0xFF) {
                return "UTF-8";
            }
        }
        return null;
    }

    /**
     * 读取SD卡中文本文件
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
        Toast.makeText(context, "保存成功，请到SD卡查看,文件名为pro.txt", Toast.LENGTH_SHORT)
                .show();
    }
}
