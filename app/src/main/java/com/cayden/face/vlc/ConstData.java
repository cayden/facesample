package com.cayden.face.vlc;


import momo.cn.edu.fjnu.androidutils.data.CommonValues;

public class ConstData {
	public static final int DB_VERSION = 1;
	public static final String DB_DIRECTORY = CommonValues.application.getFilesDir().getPath();
	public static final String DB_NAME = "udp_player.db";

	public static final int DEFAULT_PREVIEW_WIDTH = 800;
	public static final int DEFAULT_PREVIEW_HEIGHT = 654;
	public static final int PIXEL_SIZE = 4;
	public static final int DEFAULT_PREVIEW_MODE = 0;
	public static final int DEFAULT_PREVIEW_MIN_FPS = 1;
	public static final int DEFAULT_PREVIEW_MAX_FPS = 30;
	public static final float DEFAULT_BANDWIDTH = 1.0f;

	public static final int FRAME_FORMAT_YUYV = 0;
	public static final int FRAME_FORMAT_MJPEG = 1;

	public static final int PIXEL_FORMAT_RAW = 0;
	public static final int PIXEL_FORMAT_YUV = 1;
	public static final int PIXEL_FORMAT_RGB565 = 2;
	public static final int PIXEL_FORMAT_RGBX = 3;
	public static final int PIXEL_FORMAT_YUV420SP = 4;
	public static final int PIXEL_FORMAT_NV21 = 5;		// = YVU420SemiPlanar

	public interface IntentKey{
		String VIDEO_URL = "video_url";
		String INSTALL_TIME = "install_time";
	}
}
