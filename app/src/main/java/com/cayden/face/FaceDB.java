package com.cayden.face;

import android.util.Log;


import com.cayden.face.facenet.FaceFeature;
import com.cayden.face.stream.ExtInputStream;
import com.cayden.face.stream.ExtOutputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by caydencui on 2018/9/6.
 */

public class FaceDB {
    private final String TAG = this.getClass().toString();

    String mDBPath;
    public List<FaceRegist> mRegister;
    private FaceDB() {

    }

    private static class SingletonInstance {
        private static final FaceDB INSTANCE = new FaceDB();
    }

    public static FaceDB getInstance() {
        return SingletonInstance.INSTANCE;
    }

    public void init(String path){
        mDBPath = path;
        mRegister = new ArrayList<>();
    }

    public class FaceRegist {
        public String mName;
        public FaceFeature faceFeature;

        public FaceRegist(String name) {
            mName = name;
            faceFeature = new FaceFeature();
        }
    }

    private boolean saveInfo() {
        try {
            Log.d(TAG, "mDBPath=" +mDBPath + "/face.txt");
            FileOutputStream fs = new FileOutputStream(mDBPath + "/face.txt");
            ExtOutputStream bos = new ExtOutputStream(fs);
            bos.writeString(  "tusi-face-v1.0.0," + 1);
            bos.close();
            fs.close();
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 加载数据
     * @return
     */
    private boolean loadInfo() {
        if (!mRegister.isEmpty()) {
            return false;
        }
        try {
            File file=new File(mDBPath );
            if(!file.exists()){
                file.mkdir();

            }
            FileInputStream fs = new FileInputStream(mDBPath + "/face.txt");
            ExtInputStream bos = new ExtInputStream(fs);
            //load version
            String version_saved = bos.readString();

            //load all regist name.
            if (version_saved != null) {
                for (String name = bos.readString(); name != null; name = bos.readString()){
                    if (new File(mDBPath + "/" + name + ".data").exists()) {
                        mRegister.add(new FaceRegist(new String(name)));
                    }
                }
            }
            bos.close();
            fs.close();
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 加载人脸数据
     * @return
     */
    public boolean loadFaces(){
        if (loadInfo()) {
            try {
                for (FaceRegist face : mRegister) {
                    Log.d(TAG, "load name:" + face.mName + "'s face feature data.");
                    FileInputStream fs = new FileInputStream(mDBPath + "/" + face.mName + ".data");

                    float [] fea= readFloatFromData(mDBPath + "/" + face.mName + ".data",512);
                    face.faceFeature.setFea(fea);

                }
                Log.d(TAG, "load mRegister: size = " + mRegister.size());
                return true;
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 添加人脸数据信息
     * @param name
     * @param face
     */
    public	void addFace(String name, FaceFeature face) {
        try {
            //check if already registered.
            boolean add = true;
            for (FaceRegist frface : mRegister) {
                if (frface.mName.equals(name)) {
                    frface.faceFeature=face;
                    add = false;
                    break;
                }
            }
            if (add) { // not registered.
                FaceRegist frface = new FaceRegist(name);
                frface.faceFeature=face;
                mRegister.add(frface);
            }

            if (saveInfo()) {
                //update all names
                FileOutputStream fs = new FileOutputStream(mDBPath + "/face.txt", true);
                ExtOutputStream bos = new ExtOutputStream(fs);
                for (FaceRegist frface : mRegister) {
                    bos.writeString(frface.mName);
                }
                bos.close();
                fs.close();

                //save new feature
                writeFloatToData(face.getFeature(),mDBPath + "/" + name + ".data",512);

            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除对应人脸信息
     * @param name
     * @return
     */
    public boolean delete(String name) {
        try {
            //check if already registered.
            boolean find = false;
            for (FaceRegist frface : mRegister) {
                if (frface.mName.equals(name)) {
                    File delfile = new File(mDBPath + "/" + name + ".data");
                    if (delfile.exists()) {
                        delfile.delete();
                    }
                    mRegister.remove(frface);
                    find = true;
                    break;
                }
            }

            if (find) {
                if (saveInfo()) {
                    //update all names
                    FileOutputStream fs = new FileOutputStream(mDBPath + "/face.txt", true);
                    ExtOutputStream bos = new ExtOutputStream(fs);
                    for (FaceRegist frface : mRegister) {
                        bos.writeString(frface.mName);
                    }
                    bos.close();
                    fs.close();
                }
            }
            return find;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }


    /**
     * 将float[]写入文件
     * @param verts
     * @param gcodeFile
     * @param count
     */
    private    void writeFloatToData(float[] verts, String gcodeFile, int count) {
        try {
            RandomAccessFile aFile = new RandomAccessFile(gcodeFile, "rw");
            FileChannel outChannel = aFile.getChannel();
            //one float 4 bytes
            ByteBuffer buf = ByteBuffer.allocate(4 * count * 3 * 3);
            buf.clear();
            buf.asFloatBuffer().put(verts);
            //while(buf.hasRemaining())
            {
                outChannel.write(buf);
            }
            //outChannel.close();
            buf.rewind();
            outChannel.close();
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
        }
    }

    /**
     * 读取文件返回float[]
     * @param gcodeFile
     * @param Count
     * @return
     */
    private  float[] readFloatFromData(String gcodeFile, int Count) {
        float[] verts = new float[Count * 3 * 3];
        try {
            RandomAccessFile rFile = new RandomAccessFile(gcodeFile, "rw");
            FileChannel inChannel = rFile.getChannel();
            ByteBuffer buf_in = ByteBuffer.allocate(3 * 3 * Count * 4);
            buf_in.clear();
            inChannel.read(buf_in);
            buf_in.rewind();
            buf_in.asFloatBuffer().get(verts);
            inChannel.close();
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
        }
        return verts;
    }


}
