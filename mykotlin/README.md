# Kotlin 仿开眼

集成 Kotlin 步骤：
1. 工程 build.gradle 中添加 `classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:1.1.3-2"`
2. module build.gradle 中添加 `apply plugin: 'kotlin-android'`

**注意点**
- 如果想要直接 通过 id 取得对象（例如： `txt_title.setText("")`），必须在 module build.gradle 中添加 `apply plugin: 'kotlin-android-extensions'`