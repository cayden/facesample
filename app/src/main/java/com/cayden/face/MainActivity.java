package com.cayden.face;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.MediaPlayer;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.cayden.face.utils.ImageUtils;
import com.cayden.face.utils.NV21ToBitmap;
import com.cayden.face.vlc.ConstData;
import com.cayden.face.vlc.Rtsp;
import com.cayden.face.vlc.RtspCallbackInterface;
import com.cayden.face.vlc.UrlInfo;
import com.cayden.face.vlc.UrlInfoService;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 启动类
 */
public class MainActivity extends AppCompatActivity   {

    private static String TAG = "MainActivity";
    private static final String RTSP_URL_MAIN = "rtsp://admin:adminTUSI@192.168.1.64:554/h264/main/av_stream";
    private static final String RTSP_URL_SUB = "rtsp://admin:adminTUSI@192.168.1.64:554/h264/ch1/sub/av_stream";
//    private static final String  RTSP_URL_SUB="rtsp://192.168.1.65:554/profile2";

    private UrlInfoService mUrlInfoService;
    private String rtsp = RTSP_URL_SUB;
    private int orientation;
    private SurfaceView mVideoTexture = null;
    private MediaPlayer mediaPlayer;
    private Rtsp mNxpRtsp;
    private SurfaceView draw_view;


    private Button mBtnStop;
    private Button mBtnStartPlay;

    private EditText mEditNetAddress;
    Button mRun_verify,mPicSave;
    boolean isSave=false;
    protected int iw = 0, ih;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vlc_player);

        mVideoTexture = (SurfaceView) findViewById(R.id.camera_preview);
        draw_view = (SurfaceView) findViewById(R.id.pointView);
        draw_view.setZOrderOnTop(true);
        draw_view.getHolder().setFormat(PixelFormat.TRANSLUCENT);


        mBtnStop=(Button)findViewById(R.id.btn_stop);
        mBtnStartPlay=(Button)findViewById(R.id.btn_start_play);

        mRun_verify=(Button)findViewById(R.id.run_verify);
        mRun_verify.setOnClickListener(onClickListener_run);
        mPicSave=(Button)findViewById(R.id.pic_save);
        mPicSave.setOnClickListener(onClickListener_PicSave);
        mEditNetAddress=(EditText)findViewById(R.id.edit_net_address);

        init();

    }


    public void init() {

        mUrlInfoService = new UrlInfoService();

        mBtnStartPlay.setOnClickListener(onClickListener_StartPlay);
        //mBtnPause.setOnClickListener(onClickListener_Pause);
        mBtnStop.setOnClickListener(onClickListener_Stop);


        mNxpRtsp=new Rtsp(this, new RtspCallbackInterface() {
            @Override
            public void decodeOutputBuffer(int frameLen, byte[] bytes,long width,long height) {
                Log.d(TAG,"Get frameLen :"+frameLen+" width :"+width+" height :"+height);
                if(frameLen==0) return;
                if (iw == 0) {
                    iw=(int)width;
                    ih=(int)height;
                }

//                if (isSave) {
//                    mPictureSave(bytes);
//                    isSave = false;
//                }
                /**
                 * 将bytes转为bitmap
                 */
                Bitmap bitmap = null;
                byte[] NV21 = new byte[bytes.length];
                NV12ToNV21(bytes, NV21,iw, ih);
                NV21ToBitmap nv21ToBitmap = new NV21ToBitmap(MainActivity.this);
                bitmap = nv21ToBitmap.nv21ToBitmap(NV21, iw, ih);
                if(isSave){
                    ImageUtils.saveImg(bitmap);
                    isSave=false;
                }
                bitmap.recycle();
            }
        });

    }


    public static String tempTarget = Environment.getExternalStorageDirectory() + File.separator + "CWModels" + File.separator + "Register.jpg";

    /**
     * 保存图片
     *
     * @param data
     */
    public void mPictureSave(byte[] data) {
        byte[] NV21 = new byte[data.length];
        NV12ToNV21(data, NV21, ConstData.DEFAULT_PREVIEW_WIDTH, ConstData.DEFAULT_PREVIEW_HEIGHT);
        YuvImage yuvImage = new YuvImage(NV21, ImageFormat.NV21, ConstData.DEFAULT_PREVIEW_WIDTH, ConstData.DEFAULT_PREVIEW_HEIGHT, null);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(new Rect(0, 0, ConstData.DEFAULT_PREVIEW_WIDTH, ConstData.DEFAULT_PREVIEW_HEIGHT), 80, baos);
        byte[] jdata = baos.toByteArray();
        Bitmap bitmap = BitmapFactory.decodeByteArray(jdata, 0, jdata.length);
        File file = null;
        file = new File(tempTarget);
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(jdata);
            fileOutputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void NV12ToNV21(byte[] nv12, byte[] nv21, int width, int height) {
        if (nv21 == null || nv12 == null) return;
        int framesize = width * height;
        int i = 0, j = 0;
        //System.arraycopy(nv21, test, nv12, test, framesize);
        for (i = 0; i < framesize; i++) {
            nv21[i] = nv12[i];
        }
        for (j = 0; j < framesize / 2; j += 2) {
            nv21[framesize + j] = nv12[j + framesize + 1];
        }
        for (j = 0; j < framesize / 2; j += 2) {
            nv21[framesize + j + 1] = nv12[j + framesize];
        }
    }

    private View.OnClickListener onClickListener_run=new View.OnClickListener() {
        @Override
        public void onClick(View v){


                //  mRegisterStatus.setText("");
                //  mEditText.setText("");
                mRun_verify.setClickable(false);
                //mBackgroundHandler1.post(new ImageProcess());

        }
    };
    private View.OnClickListener onClickListener_PicSave=new View.OnClickListener() {
        @Override
        public void onClick(View v){
            isSave=true;
        }
    };

    private View.OnClickListener onClickListener_StartPlay=new View.OnClickListener() {
        @Override
        public void onClick(View v){
            String netAddress = mEditNetAddress.getText().toString().trim();
            if(!TextUtils.isEmpty(netAddress)){
                UrlInfo urlInfo = new UrlInfo();
                urlInfo.setUrl(netAddress);
                mUrlInfoService.save(urlInfo);

                mNxpRtsp.SetUrl(netAddress);
                mNxpRtsp.init(null,mVideoTexture);
                mNxpRtsp.play();
            }

        }
    };

    private View.OnClickListener onClickListener_Stop=new View.OnClickListener() {
        @Override
        public void onClick(View v){
            mNxpRtsp.stop();

        }
    };

}
