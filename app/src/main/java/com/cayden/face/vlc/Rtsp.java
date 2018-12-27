package com.cayden.face.vlc;

import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.net.Uri;
import android.os.Process;
import android.util.Log;
import android.view.SurfaceView;
import android.view.TextureView;

import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayCallback;
import org.videolan.libvlc.MediaPlayer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;


public class Rtsp {

    private String mUrl;
    private MediaPlayer mMediaPlayer = null;
    private LibVLC mLibVLC = null;
    private Context mContext;
    private SurfaceView msView = null;
    private TextureView mtView = null;
    private Thread mGetFrameBufferThread;
    private RtspCallbackInterface mNxpRtspCallbackInterface=null;
    private String TAG ="Rtsp";
    private MediaCodec mMediaCodec = null;
    private MediaFormat mOutFormat;
    private SurfaceView mSurfaceView = null;
    private int mRotationDegrees = 0;
    private int mLostFrameCount = 0;
    private int mRenderMode = 1;
    private int mcxVideo = 0;
    private int mcyVideo = 0;
    private int mNxpRtspState=-1;
    public int len=10;
    public byte[] frameData = new byte[4000000];
    public int[] frameInfo = new int[2];


    public Rtsp(Context context, RtspCallbackInterface callback){
        mContext=context;
        mNxpRtspCallbackInterface=callback;
    }
    public boolean SetUrl(String url){
        this.mUrl=url;
        return true;
    }

    public boolean init(SurfaceView sView, SurfaceView tView){

        final ArrayList<String> args = new ArrayList<>();
        args.add("-vvv");
        mLibVLC = new LibVLC(mContext, args);
        mMediaPlayer = new MediaPlayer(mLibVLC);

        //init mMediaPlayer
        final IVLCVout vlcVout = mMediaPlayer.getVLCVout();
        if(sView!=null) {
            msView = sView;
            vlcVout.setVideoView(mtView);
        }
        else if(tView!=null){
            mSurfaceView=tView;
            vlcVout.setVideoView(mSurfaceView);

        }

        vlcVout.attachViews();

        Media media = new Media(mLibVLC, Uri.parse(mUrl));
        media.setHWDecoderEnabled(true,true);
        media.addOption(":no-audio");
        media.addOption(":network-caching=150");
        media.addOption(":file-caching=150");
        media.addOption(":sout-mux-caching=150");
        media.addOption(":live-caching=150");
        mMediaPlayer.setMedia(media);

        mMediaPlayer.getVLCVout().setWindowSize(ConstData.DEFAULT_PREVIEW_WIDTH,ConstData.DEFAULT_PREVIEW_HEIGHT);
        mMediaPlayer.setAspectRatio(ConstData.DEFAULT_PREVIEW_WIDTH+":"+ConstData.DEFAULT_PREVIEW_HEIGHT);
        mMediaPlayer.setScale(0);

        media.release();
        float scale=704/576;
        vlcVout.setWindowSize(800,(int)(800/scale));
//        vlcVout.setWindowSize(1280,720);
        //creat a thread to get the  frame data
        this.mGetFrameBufferThread=new Thread(this.mGetFrameBufferRun);


        return true;
    }

    public boolean init(SurfaceView sView, TextureView tView){

        final ArrayList<String> args = new ArrayList<>();
        args.add("-vvv");
        mLibVLC = new LibVLC(mContext, args);
        mMediaPlayer = new MediaPlayer(mLibVLC);

        //init mMediaPlayer
        final IVLCVout vlcVout = mMediaPlayer.getVLCVout();
        if(sView!=null) {
            msView = sView;
            vlcVout.setVideoView(mtView);
        }
        else if(tView!=null){
            mtView=tView;
            vlcVout.setVideoView(mtView);
        }

       vlcVout.attachViews();

        Media media = new Media(mLibVLC, Uri.parse(mUrl));
        media.setHWDecoderEnabled(true,true);
        media.addOption(":no-audio");
        media.addOption(":network-caching=150");
        media.addOption(":file-caching=150");
        media.addOption(":sout-mux-caching=150");
        media.addOption(":live-caching=150");
        mMediaPlayer.setMedia(media);

        mMediaPlayer.getVLCVout().setWindowSize(ConstData.DEFAULT_PREVIEW_WIDTH,ConstData.DEFAULT_PREVIEW_HEIGHT);
        mMediaPlayer.setAspectRatio(ConstData.DEFAULT_PREVIEW_WIDTH+":"+ConstData.DEFAULT_PREVIEW_HEIGHT);
        mMediaPlayer.setScale(0);

        media.release();

        //creat a thread to get the  frame data
        this.mGetFrameBufferThread=new Thread(this.mGetFrameBufferRun);


        return true;
    }

    public void setVideoCallback(MediaPlayCallback mediaPlayCallback){
        if(mMediaPlayer!=null){
            ByteBuffer frameBuffer = ByteBuffer.allocateDirect(ConstData.DEFAULT_PREVIEW_WIDTH*ConstData.DEFAULT_PREVIEW_HEIGHT*ConstData.PIXEL_SIZE);
            mMediaPlayer.setVideoFormat("RGBA",ConstData.DEFAULT_PREVIEW_WIDTH,ConstData.DEFAULT_PREVIEW_HEIGHT,ConstData.PIXEL_SIZE);
            mMediaPlayer.setVideoCallback(frameBuffer,mediaPlayCallback);
        }
    }

    public boolean play(){
        Process.setThreadPriority(-20);
        mMediaPlayer.play();
        //test();
        this.mGetFrameBufferThread.start();


        return true;
    }
    void sleep(int millisecond)
    {
        try
        {
            Thread.sleep(millisecond);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    private void test() throws IOException {
       // byte[] array = {9,100,10,37,5,10};

        len=mMediaPlayer.getBuffer(frameData,frameInfo);
      ///  for(int temp=0;temp<10;temp++){
      ///      Log.d(TAG, "the Array len is  "+frameData[temp]);
       /// }
        //确定写出文件的位置
        File file = new File("/sdcard/DumpData.yuv");
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(frameData);
        fos.close();
       Log.d(TAG, "the Array len is  "+len);
    }

    public boolean stop(){
        mMediaPlayer.stop();
        this.mGetFrameBufferThread.interrupt();
        mMediaPlayer.getVLCVout().detachViews();
        return true;
    }
    public boolean pause(){
        mMediaPlayer.pause();
        mMediaPlayer.getVLCVout().detachViews();
        return true;
    }

    public static boolean mRun=true;


    private Runnable mGetFrameBufferRun=new Runnable() {
        @Override
        public void run()
        {
           // Process.setThreadPriority(-8);

            while(true) {

                try {
                if(mRun) {
//                    sleep(100);
                     len = mMediaPlayer.getBuffer(frameData, frameInfo);
                     mNxpRtspCallbackInterface.decodeOutputBuffer(len,frameData,frameInfo[0],frameInfo[1]);
//                    LocalFaceSDK.getInstance(null).cwPushBinaryFrame(frameData, frameInfo[0], frameInfo[1],
//                            FaceInterface.cw_img_form_t.CW_IMAGE_NV12, CaremaType.BACK_LANDSCAPE);
                }
                  //  test();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };
}
