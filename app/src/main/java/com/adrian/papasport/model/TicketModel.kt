package com.adrian.papasport.model

import org.json.JSONArray
import org.json.JSONObject

/**
 * date:2019/7/3 9:56
 * author:RanQing
 * description:
 */
class BasePrintModel(
    var type: Int, //打印类型.1-门票;2-支付凭证
    var content: String    //打印内容.门票或者支付凭证内容
) {
    fun convert2JsonString(): String {
        val jsonObject = JSONObject()
        jsonObject.put("type", type)
        jsonObject.put("content", content)
        return jsonObject.toString()
    }

    fun parseJson(jsonStr: String) {
        val jsonObject = JSONObject(jsonStr)
        type = jsonObject.optInt("type")
        content = jsonObject.optString("content")
    }
}

/**
 * 二维码门票信息
 */
class QrCodeTicketInfo(
    var ticketName: String? = null,  //门票名称
    var ticketNum: String? = null    //门票号码
) {
    fun convert2JsonString(): String {
        val jsonObject = JSONObject()
        jsonObject.put("ticketName", ticketName)
        jsonObject.put("ticketNum", ticketNum)
        return jsonObject.toString()
    }

    fun parseJson(jsonStr: String) {
        val jsonObject = JSONObject(jsonStr)
        ticketName = jsonObject.optString("ticketName")
        ticketNum = jsonObject.optString("ticketNum")
    }
}

/**
 * 支付凭证信息
 */
class PaymentVoucherInfo(
    var fieldName: String? = null,     //场地名称
    var consumeType: String? = null,   //消费类型
    var consumeAddr: String? = null,   //消费地点
    var printTime: String? = null,     //打印时间
    var ticketList: ArrayList<TicketBrief>? = null,    //票据列表
    var total: Float? = null,          //合计价格
    var offer: Float? = null,          //优惠减扣
    var payType: String? = null,       //支付方式
    var payTime: String? = null        //支付时间
) {
    fun convert2JsonString(): String {
        val jsonObject = JSONObject()
        jsonObject.put("fieldName", fieldName)
        jsonObject.put("consumeType", consumeType)
        jsonObject.put("consumeAddr", consumeAddr)
        jsonObject.put("printTime", printTime)
        val ticketArray = JSONArray()
        ticketList?.forEach {
            val item = TicketBrief(it.name, it.price, it.count)
            ticketArray.put(JSONObject(item.convert2JsonString()))
        }
        jsonObject.put("ticketList", jsonObject)
        jsonObject.put("total", total)
        jsonObject.put("offer", offer)
        jsonObject.put("payType", payType)
        jsonObject.put("payTime", payTime)
        return jsonObject.toString()
    }

    fun parseJson(jsonStr: String) {
        val jsonObject = JSONObject(jsonStr)
        fieldName = jsonObject.optString("fieldName")
        consumeType = jsonObject.optString("consumeType")
        consumeAddr = jsonObject.optString("consumeAddr")
        printTime = jsonObject.optString("printTime")
        val ticketArray = jsonObject.optJSONArray("ticketList")
        val count = ticketArray.length()
        for (i in 0 until count) {
        }
    }
}

/**
 * 票据简介
 */
class TicketBrief(
    var name: String? = null, //票据名称
    var price: Double? = null, //单价
    var count: Int? = null      //数量
) {
    fun convert2JsonString(): String {
        val jsonObject = JSONObject()
        jsonObject.put("name", name)
        jsonObject.put("price", price)
        jsonObject.put("count", count)
        return jsonObject.toString()
    }

    fun parseJson(jsonStr: String) {
        val jsonObject = JSONObject(jsonStr)
        name = jsonObject.optString("name")
        price = jsonObject.optDouble("price")
        count = jsonObject.optInt("count")
    }
}