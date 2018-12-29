package com.cayden.face;

import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.cayden.face.facenet.Box;
import com.cayden.face.facenet.FaceFeature;
import com.cayden.face.facenet.Facenet;
import com.cayden.face.facenet.MTCNN;
import com.cayden.face.facenet.Utils;

import java.io.File;
import java.io.IOException;
import java.util.Vector;

/**
 * Created by caydencui on 2018/12/29.
 */

public class FaceManagerActivity extends AppCompatActivity implements View.OnClickListener {

    public static final int CHOOSE_PHOTO=0;
    private Button btn_select,btn_register;
    private EditText edit_name;
    private ImageView iv_head;
    private Uri imageUri;
    private Facenet facenet;
    private MTCNN mtcnn;
    private  Bitmap bitmap=null;//选择的图片
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        facenet=Facenet.getInstance();
        mtcnn = MTCNN.getInstance();
        btn_select=(Button)findViewById(R.id.pic_select);
        btn_register=(Button)findViewById(R.id.pic_register);
        btn_select.setOnClickListener(this);
        btn_register.setOnClickListener(this);
        iv_head=(ImageView)findViewById(R.id.iv_head);
        edit_name=(EditText)findViewById(R.id.edit_name);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.pic_select:
                choosePhoto();
                break;
            case R.id.pic_register:
                registerFace();
                break;
        }
    }


    public Bitmap detectFaces(){
        if(null==bitmap)return null;

        Vector<Box> boxes=mtcnn.detectFaces(bitmap,40);
        if (boxes.size()==0) return null;
        for (int i=0;i<boxes.size();i++) Utils.drawBox(bitmap,boxes.get(i),1+bitmap.getWidth()/500 );
        Log.i("Main","[*]boxNum"+boxes.size());
        Rect rect1=boxes.get(0).transform2Rect();
        //MTCNN检测到的人脸框，再上下左右扩展margin个像素点，再放入facenet中。
        int margin=20; //20这个值是facenet中设置的。自己应该可以调整。
        Utils.rectExtend(bitmap,rect1,margin);
        //要比较的两个人脸，加厚Rect
        Utils.drawRect(bitmap,rect1,1+bitmap.getWidth()/100 );
        //(2)裁剪出人脸(只取第一张)
        return Utils.crop(bitmap,rect1);
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                iv_image.setImageBitmap(mBitmapFace1);
//            }
//        });
    }


    private void registerFace(){
        Bitmap bitmap=detectFaces();
        if(null==bitmap){
            Toast.makeText(this,"选择人脸图片",Toast.LENGTH_SHORT).show();
            return;
        }
        String username= edit_name.getText().toString();
        if(TextUtils.isEmpty(username)){
            Toast.makeText(this,"请输入名字",Toast.LENGTH_SHORT).show();
            return;
        }

        FaceFeature ff1=facenet.recognizeImage(bitmap);
        FaceDB.getInstance().addFace(username,ff1);
    }

    private void choosePhoto(){
        //同样new一个file用于存放照片
        File imageFile = new File(Environment
                .getExternalStorageDirectory(), "outputImage.jpg");
        if (imageFile.exists()) {
            imageFile.delete();
        }
        try {
            imageFile.createNewFile();
        } catch (IOException e) {
            // TODO Auto-generated catch block
        }
        //转换成Uri
        imageUri = Uri.fromFile(imageFile);
        //开启选择界面
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        //设置可以缩放
        intent.putExtra("scale", true);
        //设置可以裁剪
        intent.putExtra("crop", true);
        intent.setType("image/*");
        //设置输出位置
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        //开始选择
        startActivityForResult(intent, CHOOSE_PHOTO);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case CHOOSE_PHOTO:
                if(resultCode==RESULT_OK){
                    handleImageOnKitkat(data);
                }
                break;
        }

    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void handleImageOnKitkat(Intent data) {
        String imagePath = null;
        Uri uri = data.getData();
        if (DocumentsContract.isDocumentUri(this, uri)) {
            // 如果是document类型的Uri，则通过document id处理
            String docId = DocumentsContract.getDocumentId(uri);
            if ("com.android.providers.media.documents".equals(uri
                    .getAuthority())) {
                String id = docId.split(":")[1]; // 解析出数字格式的id
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            } else if ("com.android.providers.downloads.documents".equals(uri
                    .getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"),
                        Long.valueOf(docId));
                imagePath = getImagePath(contentUri, null);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            // 如果不是document类型的Uri，则使用普通方式处理
            imagePath = getImagePath(uri, null);
        }
        displayImage(imagePath); // 根据图片路径显示图片
        System.err.println(imagePath);
    }

    private String getImagePath(Uri uri, String selection) {
        String path = null;
        // 通过uri和selection来获取真实的图片路径
        Cursor cursor = getContentResolver().query(uri, null, selection, null,
                null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor
                        .getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    /**
     * 显示图片
     * @param imagePath
     */
    private void displayImage(String imagePath) {
        if (imagePath != null) {
            bitmap = BitmapFactory.decodeFile(imagePath);
            iv_head.setImageBitmap(bitmap);
        } else {
            Toast.makeText(this, "failed to get image", Toast.LENGTH_SHORT)
                    .show();
        }
    }

}
