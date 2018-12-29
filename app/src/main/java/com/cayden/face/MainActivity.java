package com.cayden.face;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.cayden.face.facenet.Box;
import com.cayden.face.facenet.FaceFeature;
import com.cayden.face.facenet.Facenet;
import com.cayden.face.facenet.MTCNN;
import com.cayden.face.facenet.Utils;
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
import java.util.List;
import java.util.Vector;

import dou.utils.DLog;
import dou.utils.DisplayUtil;

/**
 * 启动类
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener{

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

    private TextView tv_result;
    private Button mBtnStop;
    private Button mBtnStartPlay,mBtnRegister;

    private EditText mEditNetAddress;
    Button mRun_verify, mPicSave;
    boolean isSave = false;
    private boolean isStart=false;
    protected int iw = 0, ih;
    private float scale_bit = 0;
    private final Object lock = new Object();
    private Facenet facenet;
    private MTCNN mtcnn;

    private String foundName;
    private double cmp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vlc_player);

        mVideoTexture = (SurfaceView) findViewById(R.id.camera_preview);
        draw_view = (SurfaceView) findViewById(R.id.pointView);
        draw_view.setZOrderOnTop(true);
        draw_view.getHolder().setFormat(PixelFormat.TRANSLUCENT);


        mBtnStop = (Button) findViewById(R.id.btn_stop);
        mBtnStartPlay = (Button) findViewById(R.id.btn_start_play);

        mRun_verify = (Button) findViewById(R.id.run_verify);
        mRun_verify.setOnClickListener(onClickListener_run);
        mPicSave = (Button) findViewById(R.id.pic_save);
        mPicSave.setOnClickListener(onClickListener_PicSave);
        mEditNetAddress = (EditText) findViewById(R.id.edit_net_address);
        tv_result=(TextView)findViewById(R.id.tv_result);
        mBtnRegister=(Button)findViewById(R.id.pic_register);
        mBtnRegister.setOnClickListener(this);

        init();
        facenet = Facenet.getInstance();
        mtcnn = MTCNN.getInstance();
        FaceDB.getInstance().loadFaces();

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.pic_register:
                startActivityForResult(new Intent(this, FaceManagerActivity.class), 10);
                break;
        }
    }

    public void init() {

        mUrlInfoService = new UrlInfoService();

        mBtnStartPlay.setOnClickListener(onClickListener_StartPlay);
        //mBtnPause.setOnClickListener(onClickListener_Pause);
        mBtnStop.setOnClickListener(onClickListener_Stop);


        mNxpRtsp = new Rtsp(this, new RtspCallbackInterface() {
            @Override
            public void decodeOutputBuffer(int frameLen, byte[] bytes, long width, long height) {
                Log.d(TAG, "Get frameLen :" + frameLen + " width :" + width + " height :" + height);
                if (frameLen == 0) return;
                if (iw == 0) {
                    iw = (int) width;
                    ih = (int) height;

                    int surface_w = mVideoTexture.getWidth();
                    int surface_h = mVideoTexture.getHeight();
                    scale_bit = (float) surface_h / ih;
                    ViewGroup.LayoutParams params = draw_view.getLayoutParams();
                    params.width = surface_w;
                    params.height = surface_w;
                    DLog.d("scale_bit:" + scale_bit + ",surface_w:" + surface_w + ",surface_h:" + surface_h + ",iw:" + iw + ",ih:" + ih);
                    draw_view.requestLayout();
                }

//                if (isSave) {
//                    mPictureSave(bytes);
//                    isSave = false;
//                }
                /**
                 * 将bytes转为bitmap
                 */
                synchronized (lock) {
                    Bitmap bitmap = null;
                    byte[] NV21 = new byte[bytes.length];
                    NV12ToNV21(bytes, NV21, iw, ih);
                    NV21ToBitmap nv21ToBitmap = new NV21ToBitmap(MainActivity.this);
                    bitmap = nv21ToBitmap.nv21ToBitmap(NV21, iw, ih);
                    if (isSave) {//保存图片
                        ImageUtils.saveImg(bitmap);
                        isSave = false;
                    }
                    Vector<Box> boxes = mtcnn.detectFaces(bitmap, 20);

                    drawAnim(boxes, draw_view, scale_bit, 1, "");

                    if (boxes.size()==0) return ;
                    for (int i=0;i<boxes.size();i++) Utils.drawBox(bitmap,boxes.get(i),1+bitmap.getWidth()/500 );
                    Log.i("Main","[*]boxNum："+boxes.size());
                    Box box=boxes.get(0);
                    Rect rect1=box.transform2Rect();

                    //MTCNN检测到的人脸框，再上下左右扩展margin个像素点，再放入facenet中。
                    int margin=20; //20这个值是facenet中设置的。自己应该可以调整。
                    Utils.rectExtend(bitmap,rect1,margin);
                    //要比较的两个人脸，加厚Rect
                    Utils.drawRect(bitmap,rect1,1+bitmap.getWidth()/100 );
                    //(2)裁剪出人脸(只取第一张)
                    final Bitmap face1=Utils.crop(bitmap,rect1);

                    FaceFeature ff1=facenet.recognizeImage(face1);
                    if(null==ff1||ff1.getFeature().length<0)return;
                    List<FaceDB.FaceRegist> mResgist =FaceDB.getInstance().mRegister;
                    double tempScore=-1;
                    for(int i=0;i<mResgist.size();i++){
                        FaceDB.FaceRegist faceRegist= mResgist.get(i);
                        double temp= ff1.compare(faceRegist.faceFeature);
                        /**
                         * 找出值最小的
                          */
                        if(tempScore==-1){
                            tempScore=temp;//第一次直接赋值
                            foundName=faceRegist.mName;
                        }else{
                            if(tempScore>temp){
                                foundName=faceRegist.mName;
                                tempScore=temp;
                            }
                        }
                        Log.d(TAG,">>>>>>>>>>temp="+temp+",tempScore="+tempScore+",foundName："+foundName);
                    }
                    cmp=tempScore;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            tv_result.setText(String.format("名字:%s  相似度 :  %.4f", foundName,cmp) );
                        }
                    });
                }

            }
        });

    }


    protected void drawAnim(Vector<Box> faces, SurfaceView outputView, float scale_bit, int cameraId, String fps) {
        Paint paint = new Paint();
        Canvas canvas = ((SurfaceView) outputView).getHolder().lockCanvas();
        if (canvas != null) {
            try {
                int viewH = outputView.getHeight();
                int viewW = outputView.getWidth();
//                DLog.d("viewW:"+viewW+",viewH:"+viewH);
                canvas.drawColor(0, PorterDuff.Mode.CLEAR);
                if (faces == null || faces.size() == 0) return;
                for (int i = 0; i < faces.size(); i++) {
                    paint.setColor(Color.BLUE);
                    int size = DisplayUtil.dip2px(this, 3);
                    paint.setStrokeWidth(size);
                    paint.setStyle(Paint.Style.STROKE);
                    Box box = faces.get(i);
                    float[] rect = box.transform2float();
                    float x1 = rect[0] * scale_bit;
                    float y1 = rect[1] * scale_bit;
                    float rect_width = rect[2] * 0.5F;
                    RectF rectf = new RectF(x1, y1, x1 + rect_width, y1 + rect_width);
                    canvas.drawRect(rectf, paint);
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                ((SurfaceView) outputView).getHolder().unlockCanvasAndPost(canvas);
            }
        }
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

    private View.OnClickListener onClickListener_run = new View.OnClickListener() {
        @Override
        public void onClick(View v) {


            //  mRegisterStatus.setText("");
            //  mEditText.setText("");
            mRun_verify.setClickable(false);
            //mBackgroundHandler1.post(new ImageProcess());

        }
    };
    private View.OnClickListener onClickListener_PicSave = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            isSave = true;
        }
    };

    private View.OnClickListener onClickListener_StartPlay = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String netAddress = mEditNetAddress.getText().toString().trim();
            if (!TextUtils.isEmpty(netAddress)) {
                UrlInfo urlInfo = new UrlInfo();
                urlInfo.setUrl(netAddress);
                mUrlInfoService.save(urlInfo);
                Rtsp.mRun = true;
                isStart=true;
                mNxpRtsp.SetUrl(netAddress);
                mNxpRtsp.init(null, mVideoTexture);
                mNxpRtsp.play();
            }

        }
    };

    private View.OnClickListener onClickListener_Stop = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mNxpRtsp.stop();

        }
    };


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(null!=mNxpRtsp&&isStart){
            Rtsp.mRun = false;
            mNxpRtsp.stop();
        }

    }
}
