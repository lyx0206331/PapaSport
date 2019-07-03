package com.adrian.papasport.model

/**
 * date:2019/7/3 9:56
 * author:RanQing
 * description:
 */
class BasePrintModel(
    var type: Int, //打印类型.1-门票;2-支付凭证
    var content: String    //打印内容.门票或者支付凭证内容
)

/**
 * 二维码门票信息
 */
class QrCodeTicketInfo(
    var ticketName: String? = null,  //门票名称
    var ticketNum: String? = null    //门票编号
)

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
)

/**
 * 票据简介
 */
class TicketBrief(
    var name: String? = null, //票据名称
    var price: String? = null, //单价
    var count: String? = null      //数量
)