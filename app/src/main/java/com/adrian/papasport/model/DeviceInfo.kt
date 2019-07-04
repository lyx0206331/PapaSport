package com.adrian.papasport.model

import org.json.JSONObject

/**
 * date:2019/7/4 18:20
 * author:RanQing
 * description:
 */
class DeviceInfo(var imei: String? = null) {
    fun toJsonString(): String {
        val jsonObject = JSONObject()
        jsonObject.put("imei", imei)
        return jsonObject.toString()
    }
}