核心功能

一键开启屏幕录制 - 点击按钮即可开始/停止录制
自动保存视频 - 录制完成后自动保存到 Movies/ScreenRecords 目录
权限管理 - 自动请求必要的存储和音频权限
状态指示 - 实时显示录制状态

技术特点

MediaProjection API - 使用Android官方的屏幕录制API
高质量录制 - 支持1080p/30fps，8Mbps视频码率
音频录制 - 同时录制麦克风音频
自动文件管理 - 按时间戳自动命名和保存文件

使用说明
1. 项目配置

最低支持 Android 5.0 (API 21)
需要的权限：存储访问、音频录制
将所有文件放到对应的Android项目目录中

2. 文件结构
app/src/main/
├── java/com/example/screenrecorder/
│   ├── MainActivity.java          # 主界面
│   └── ScreenRecorderService.java # 录制服务
├── res/
│   ├── layout/activity_main.xml   # 界面布局
│   └── drawable/button_background.xml # 按钮样式
└── AndroidManifest.xml           # 应用配置
3. 关键实现点
权限处理：

自动检查和请求存储、音频权限
首次使用时会弹出系统权限对话框

录制流程：

点击开始录制按钮
系统弹出屏幕录制授权对话框
授权后开始录制，按钮变为"停止录制"
点击停止录制，视频自动保存

文件保存：

保存路径：/storage/emulated/0/Movies/ScreenRecords/
文件名格式：screen_record_yyyyMMdd_HHmmss.mp4
视频格式：MP4 (H.264视频 + AAC音频)

4. 运行要求

Android 5.0+ 设备
足够的存储空间
麦克风权限(如需录制声音)
