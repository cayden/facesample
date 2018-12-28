package com.cayden.face.facenet;

/**
 * Created by caydencui on 2018/9/6.
 * 人脸特征(512维特征值)
 * 相似度取特征向量之间的欧式距离.
 */
public class FaceFeature {
    public static final int DIMS=512;
    private float fea[];
    public FaceFeature(){
        fea=new float[DIMS];
    }
    public float[] getFeature(){
        return fea;
    }

    public void setFea(float[] fea) {
        this.fea = fea;
    }

    //比较当前特征和另一个特征之间的相似度
    public double compare(FaceFeature ff){
        double dist=0;
        for (int i=0;i<DIMS;i++)
            dist+=(fea[i]-ff.fea[i])*(fea[i]-ff.fea[i]);
        dist= Math.sqrt(dist);
        return dist;
    }
}
