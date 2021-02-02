package com.adrian.papasport.model

import org.json.JSONObject

/**
 * author:RanQing
 * date:2019/6/29 0029 13:29
 * description:
 **/
class NFCTagInfo(val decTagId: String, val reversedId: String) {

    fun toJsonString(): String {
        val jsonObj = JSONObject()
        jsonObj.put("card_num", decTagId)
        jsonObj.put("reversedId", reversedId)
        return jsonObj.toString()
    }
}