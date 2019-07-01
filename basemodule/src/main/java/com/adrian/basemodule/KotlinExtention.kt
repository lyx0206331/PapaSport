package com.adrian.basemodule

/**
 * author:RanQing
 * date:2019/6/29 0029 3:24
 * description:Kotlin扩展类
 **/

fun Boolean?.orFalse(): Boolean = this ?: false

fun String?.orEmpty(): String = this ?: ""

fun Int?.orZero(): Int = this ?: 0

fun Long?.orZero(): Long = this ?: 0

fun Float?.orZero(): Float = this ?: .0f

fun Double?.orZero(): Double = this ?: .0