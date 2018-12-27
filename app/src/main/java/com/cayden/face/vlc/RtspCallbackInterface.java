package com.cayden.face.vlc;

public abstract interface RtspCallbackInterface {
    public abstract void decodeOutputBuffer(int frameLen, byte[] frameBuffer, long width, long height);
}
