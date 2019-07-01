package com.adrian.rfidmodule;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * author:RanQing
 * date:2019/6/29 0029 14:24
 * description:身份证信息
 **/
public class IDCardInfo {
    /**
     * 姓名
     */
    private String name;
    /**
     * 性别
     */
    private String sex;
    /**
     * 民族
     */
    private String nationality;
    /**
     * 出生日期
     */
    private String birthDate;
    /**
     * 地址
     */
    private String address;
    /**
     * 证件号码
     */
    private String idNumber;
    /**
     * 签发机关
     */
    private String office;
    /**
     * 有效期
     */
    private String validityPeriod;
    /**
     * 其它。如头像
     */
    private String extra;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getIdNumber() {
        return idNumber;
    }

    public void setIdNumber(String idNumber) {
        this.idNumber = idNumber;
    }

    public String getOffice() {
        return office;
    }

    public void setOffice(String office) {
        this.office = office;
    }

    public String getValidityPeriod() {
        return validityPeriod;
    }

    public void setValidityPeriod(String validityPeriod) {
        this.validityPeriod = validityPeriod;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }

    public String toJsonString() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("name", name);
            jsonObject.put("sex", sex);
            jsonObject.put("nationality", nationality);
            jsonObject.put("birthDate", birthDate);
            jsonObject.put("address", address);
            jsonObject.put("idNumber", idNumber);
            jsonObject.put("office", office);
            jsonObject.put("validityPeriod", validityPeriod);
            jsonObject.put("extra", extra);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }
}
