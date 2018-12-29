# facesample

本项目主要基于vlc来播放流媒体视频

主要包含以下内容

- 1、使用已经编译的libvlc来播放流媒体视频
- 2、使用MTCNN进行人脸识别并标记人脸
- 3、保存标记的人脸图片
- 4、使用FACENET进行人脸比对
- 未完待续...


### v1.0.3
   - 1, 通过FACENET获取脸部特征数据
   - 2, 人脸比对，找出相似度最高的人


### v1.0.2
   - 1, 通过MTCNN检测人脸
   - 2, 对人脸进行标记


### v1.0.1
   - 1, 获取返回的视频流数据
   - 2, 将数据nv12转换为nv21,并保存图片

### v1.0.0
   - 1, added libvlc
   - 2, support for playing rtsp video stream
   
