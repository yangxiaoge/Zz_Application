# 阿里 Sophix 热修复

### 文档中心 [地址](https://help.aliyun.com/document_detail/69874.html?spm=a2c4g.11186623.6.552.IYLNUd#1.2%20%E9%9B%86%E6%88%90%E5%87%86%E5%A4%87)

### 控制台 [地址](https://emas.console.aliyun.com/#/product/3560919/overview)

`doc` 目录下 `app-release1.apk` 是原版，`app-release2.apk` 是修复版，`sophix-patch.jar` 是补丁包

### 注意：

1. 修复版版本号记得要大于旧版本(最好是1->1.1)
    1. 如果当前是1.0版本，然后发不了一个补丁之后发现还有bug，继续在该应用版本中发布一个补丁（如果你新建一个比如2.0的版本发布这个补丁是无效的），
    此时前一个发布的补丁默认会被停止（其实就是你主动撤销并发布一个新的补丁）

2. 继续打包之前请仔细确认以下几点：
    1. 确保没有新增四大组件，没有修改AndroidManifest.xml和入口Application中的代码。
    2. 如果使用了混淆，确保打包使用的新旧包的混淆保持已经确保一致，如新包apply了旧包的mapping文件。
    3. 如果使用了加固，确保打包使用的新旧包都是加固前的正常包。
    4. 如果有资源修复，确保没有修改通知栏图标、启动图标资源以及RemoteViews等系统负责展示的资源。
    5. 如果有SO库的修复，确保所需要修复的SO都是以System.loadLibrary的方式，而不是以具体路径的方式进行加载。

### 补丁包添加：
![](doc/上传补丁包.png)

### 修复成功：
![](doc/成功修复.png)

### 可参考：https://www.jianshu.com/p/8ea4d653a53e
