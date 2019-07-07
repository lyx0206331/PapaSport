
## app
#### 主工程，内置AgentWeb库，方便使用Web页面，同时集合后面几个Module功能，处理主要业务

## BaseModule
### 基础工程，内置一些基本工具类

## NFCModule
#### NFC模块工程，处理NFC数据读取相关业务。*可修改config.gradle中的isModule值，进行Application及Module两种模式切换*

## RFIDModule
#### 射频模块工程，处理射频识别相关业务，如身份证识别。*可修改config.gradle中的isModule值，进行Application及Module两种模式切换*

## PrintModule
#### Pos机打印相关业务。*可修改config.gradle中的isModule值，进行Application及Module两种模式切换*

> 备注：这里有两个坑，一个是RFID与NFC功能冲突，开启RFID将导致NFC不可用;另一个坑是，RFID与扫码识别同时打开时，如果单独关闭RFID，可能会导致扫码功能无效