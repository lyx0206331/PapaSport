package com.adrian.printmodule

/**
 * date:2019/7/3 9:56
 * author:RanQing
 * description:
 */
data class BasePrintModel(
    var type: Int = 0, //打印类型.1-门票;2-支付凭证
    var content: String = ""   //打印内容.门票或者支付凭证内容
) {
//    constructor() : this(0, "")

//    override fun toString(): String {
//        return "[type:$type, content:$content]"
//    }
}

/**
 * 二维码门票信息
 */
class QrCodeTicketInfo(
    var ticketName: String? = null,  //门票名称
    var ticketNum: String? = null    //门票编号
) {
//    constructor() : this(null, null)

//    override fun toString(): String {
//        return "[ticketName:$ticketName, ticketNum:$ticketNum]"
//    }
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
    var total: String? = null,          //合计价格
    var offer: String? = null,          //优惠减扣
    var payType: String? = null,       //支付方式
    var payTime: String? = null        //支付时间
) {
//    constructor() : this(null, null, null, null, null, null, null, null, null)

//    override fun toString(): String {
//        return "[fieldName:$fieldName, consumeType:$consumeType, consumeAddr:$consumeAddr, printTime:$printTime, " +
//                "ticketList:${ticketList.toString()}, total:$total, offer:$offer, payType:$payType, payTime:$payTime]"
//    }

    fun getPrintContent(): String {
        val sb = StringBuilder()
        sb.append("----------------------\n")
        sb.append("消费类型     $consumeType\n")
        sb.append("消费地点     $consumeAddr\n")
        sb.append("打印时间     $printTime\n")
        sb.append("----------------------\n")
        ticketList?.forEach {
            sb.append("${it.name}    ￥${it.price}    X${it.count}\n")
        }
        sb.append("合计       ￥$total\n")
        sb.append("优惠减扣     ￥$offer\n")
        sb.append("支付方式     $payType\n")
        sb.append("支付时间     $payTime\n\n\n")

        return sb.toString()
    }
}

/**
 * 票据简介
 */
class TicketBrief(
    var name: String? = null, //票据名称
    var price: String? = null, //单价
    var count: String? = null      //数量
) {
//    constructor() : this(null, null, null)
}