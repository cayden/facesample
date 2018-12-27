package com.cayden.face;

import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.cayden.face.vlc.NxpRtsp;
import com.cayden.face.vlc.NxpRtspCallbackInterface;
import com.cayden.face.vlc.UrlInfo;
import com.cayden.face.vlc.UrlInfoService;


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
    private NxpRtsp mNxpRtsp;
    private SurfaceView draw_view;


    private Button mBtnStop;
    private Button mBtnStartPlay;

    private EditText mEditNetAddress;
    Button mRun_verify,mPicSave;
    boolean isSave=false;
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


        mNxpRtsp=new NxpRtsp(this, new NxpRtspCallbackInterface() {
            @Override
            public void decodeOutputBuffer(int frameLen, byte[] frameBuffer,long width,long height) {
                Log.d(TAG,"Get frameLen :"+frameLen+" width :"+width+" height :"+height);
            }
        });

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
