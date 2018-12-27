package com.cayden.face.vlc;

public abstract interface NxpRtspCallbackInterface {
    public abstract void decodeOutputBuffer(int frameLen, byte[] frameBuffer, long width, long height);
}
