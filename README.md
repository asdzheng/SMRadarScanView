# SMRadarScanView

###仿锤子系统的垃圾扫描的自定义View

##扫描效果图

![scan](https://github.com/asdzheng/SMRadarScanView/blob/master/gif/scan.gif)

##清除垃圾效果图

![clear](https://github.com/asdzheng/SMRadarScanView/blob/master/gif/clear.gif)

##各种属性设置

    <attr name="circleColor" format="color"/> // 外环颜色
    <attr name="innerCircleColor" format="color" /> // 内环圆的颜色
    <attr name="layerColor" format="color" />  //清除垃圾时白色透明蒙版颜色
    <attr name="innerTextColor" format="color" /> //中间显示文字的颜色
    <attr name="innerTextSize" format="integer" /> //中间显示文字的字体大小
    <attr name="radarShaderColor1" format="color" /> //渐变色1
    <attr name="radarShaderColor2" format="color" /> //渐变色2
    <attr name="radarLineColor" format="color" /> //雷达扫描针的颜色
    <attr name="borderWidth" format="integer" /> //最外白色环的宽度
    

## 开始和结束扫描

    radar.startScan();
    radar.stopScan();

## 开始和停止清理垃圾
    radar.startClear();
    radar.stopClear();


部分代码参考自 [RadarSacnView](https://github.com/gpfduoduo/RadarScanView)

## License Apache 2.0
