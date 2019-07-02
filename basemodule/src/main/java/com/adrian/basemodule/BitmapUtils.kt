package com.adrian.basemodule

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.view.View

/**
 * date:2019/7/2 18:57
 * author:RanQing
 * description:
 */

/**
 * 可见View控件生成Bitmap
 */
fun View.visibleView2Bitmap(): Bitmap? = if (this == null) null else {
    val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bmp)
    layout(left, top, right, bottom)
    draw(canvas)
    bmp
}

/**
 * 可见View控件生成Bitmap
 */
fun View.visibleView2Bitmap2(): Bitmap? = if (this == null) null else {
    clearFocus()
    isPressed = false
    //能画缓存就返回false
    val willNotCache = willNotCacheDrawing()
    setWillNotCacheDrawing(false)
    val color = drawingCacheBackgroundColor
    drawingCacheBackgroundColor = 0
    if (color != 0) {
        destroyDrawingCache()
    }
    buildDrawingCache()
    val cacheBitmap = drawingCache
    if (cacheBitmap == null) {
        null
    } else {
        val bitmap = Bitmap.createBitmap(cacheBitmap)
        //restore the view
        destroyDrawingCache()
        setWillNotCacheDrawing(willNotCache)
        drawingCacheBackgroundColor = color
        bitmap
    }
}

/**
 * 不可见View文件生成图片
 */
fun View.invisibleView2Bitmap(): Bitmap? = if (this == null) null else {
    if (measuredWidth < 1 || measuredHeight < 1) {
        val measureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        measure(measureSpec, measureSpec)
    }

    val measureW = measuredWidth
    val measureH = measuredHeight
    layout(0, 0, measureW, measureH)

    val bitmap = Bitmap.createBitmap(measureW, measureH, Bitmap.Config.ARGB_8888)
    bitmap.eraseColor(Color.TRANSPARENT)
    val canvas = Canvas(bitmap)
    draw(canvas)
    bitmap
}
