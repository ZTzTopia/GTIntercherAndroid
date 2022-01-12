package com.anzu.sdk;

import android.content.res.AssetFileDescriptor;
import android.graphics.SurfaceTexture;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.Build;
import android.util.Log;
import android.view.Surface;

import com.google.android.gms.gcm.Task;
import com.google.android.gms.search.SearchAuth;
import com.tapjoy.TJAdUnitConstants;
import com.tapjoy.TapjoyConstants;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class AnzuVideoDecoder {
    final int TIMEOUT_USEC = SearchAuth.StatusCodes.AUTH_DISABLED;
    private long accumulatedPauseTime = 0;
    private MediaCodec audioDecoder = null;
    private final Object audioDecoderLock = new Object();
    private MediaExtractor audioExtractor = null;
    int audioFrameSize = 2;
    ByteBuffer[] audioInputBuffers = null;
    ByteBuffer[] audioOutputBuffers = null;
    private MediaFormat audioTrackFormat;
    int audioTrackIndex;
    private double clipDuration = 0.0d;
    private CodecOutputSurface codecOutputSurface = null;
    private boolean decoderThreadShouldRun = false;
    private boolean decodesAudio = false;
    private boolean didError = false;
    ByteBuffer directAudioBuffer = null;
    MediaCodec.BufferInfo info;
    boolean inputDone = false;
    private boolean isPaused = false;
    private boolean isPlaying = false;
    private ByteBuffer mPixelBuf;
    private final Object mThreadDoneEvent = new Object();
    private long nativeInstance = 0;
    boolean outputDone = false;
    private long pauseStartTime;
    private final Object pauseSynch = new Object();
    private final Object timeSynch = new Object();
    long videoBufferPresentationTime = 0;
    private MediaCodec videoDecoder = null;
    private final Object videoDecoderLock = new Object();
    private MediaExtractor videoExtractor = null;
    private int videoHeight = 0;
    ByteBuffer[] videoInputBuffers = null;
    String videoMimeFormat;
    private MediaFormat videoTrackFormat;
    int videoTrackIndex;
    private int videoWidth = 0;

    private static class CodecOutputSurface implements SurfaceTexture.OnFrameAvailableListener {
        private EGLContext mEGLContext = EGL14.EGL_NO_CONTEXT;
        private EGLDisplay mEGLDisplay = EGL14.EGL_NO_DISPLAY;
        private EGLSurface mEGLSurface = EGL14.EGL_NO_SURFACE;
        private boolean mFrameAvailable;
        private final Object mFrameSyncObject = new Object();
        int mHeight;
        private Surface mSurface;
        private SurfaceTexture mSurfaceTexture;
        private STextureRender mTextureRender;
        int mWidth;

        public CodecOutputSurface(int width, int height) {
            if (width <= 0 || height <= 0) {
                throw new IllegalArgumentException();
            }

            mWidth = width;
            mHeight = height;
            eglSetup();
            makeCurrent();
            setup();
        }

        private void checkEglError(String str) {
            int eglGetError = EGL14.eglGetError();
            if (eglGetError != EGL14.EGL_SUCCESS) {
                throw new RuntimeException(str + ": EGL error: 0x" + Integer.toHexString(eglGetError));
            }
        }

        private void eglSetup() {
            mEGLDisplay= EGL14.eglGetDisplay(0);
            if (mEGLDisplay != EGL14.EGL_NO_DISPLAY) {
                int[] iArr = new int[2];
                if (EGL14.eglInitialize(mEGLDisplay, iArr, 0, iArr, 1)) {
                    EGLConfig[] eGLConfigArr = new EGLConfig[1];
                        if (EGL14.eglChooseConfig(mEGLDisplay, new int[]{EGL14.EGL_RED_SIZE, 8, EGL14.EGL_GREEN_SIZE, 8, EGL14.EGL_BLUE_SIZE, 8, EGL14.EGL_ALPHA_SIZE, 8, EGL14.EGL_RENDERABLE_TYPE, 4, EGL14.EGL_SURFACE_TYPE, 1, EGL14.EGL_NONE}, 0, eGLConfigArr, 0, 1, new int[1], 0)) {
                        mEGLContext = EGL14.eglCreateContext(mEGLDisplay, eGLConfigArr[0], EGL14.EGL_NO_CONTEXT, new int[]{EGL14.EGL_CONTEXT_CLIENT_VERSION, 2, EGL14.EGL_NONE}, 0);
                        checkEglError("eglCreateContext");
                        if (mEGLContext != null) {
                            EGLDisplay eGLDisplay = this.mEGLDisplay;
                            EGLConfig eGLConfig = eGLConfigArr[0];
                            int[] iArr2 = new int[5];
                            iArr2[0] = EGL14.EGL_WIDTH;
                            iArr2[1] = mWidth;
                            iArr2[2] = EGL14.EGL_HEIGHT;
                            iArr2[3] = mHeight;
                            iArr2[4] = EGL14.EGL_NONE;
                            mEGLSurface = EGL14.eglCreatePbufferSurface(eGLDisplay, eGLConfig, iArr2, 0);
                            checkEglError("eglCreatePbufferSurface");
                            if (mEGLSurface == null) {
                                throw new RuntimeException("surface was null");
                            }
                            return;
                        }
                        throw new RuntimeException("null context");
                    }
                    throw new RuntimeException("unable to find RGB888+recordable ES2 EGL config");
                }
                mEGLDisplay = null;
                throw new RuntimeException("unable to initialize EGL14");
            }
            throw new RuntimeException("unable to get EGL14 display");
        }

        private void setup() {
            mTextureRender = new STextureRender();
            mTextureRender.surfaceCreated();
            mSurfaceTexture = new SurfaceTexture(mTextureRender.getTextureId());
            mSurfaceTexture.setOnFrameAvailableListener(this);
            mSurface = new Surface(mSurfaceTexture);
        }

        public void GetRGBA8888(ByteBuffer byteBuffer) {
            byteBuffer.rewind();
            GLES20.glReadPixels(0, 0, this.mWidth, this.mHeight, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, byteBuffer);
        }

        public boolean awaitNewImage() {
            synchronized (mFrameSyncObject) {
                do {
                    if (!mFrameAvailable) {
                        try {
                            mFrameSyncObject.wait(2500);
                        } catch (Exception e) {
                            return false;
                        }
                    } else {
                        mFrameAvailable = false;
                        try {
                            mTextureRender.checkGlError("before updateTexImage");
                            mSurfaceTexture.updateTexImage();
                            return true;
                        } catch (Exception e2) {
                            return false;
                        }
                    }
                }
                while (mFrameAvailable);
                return false;
            }
        }

        public void drawImage(boolean z) {
            this.mTextureRender.drawFrame(mSurfaceTexture, z);
        }

        public Surface getSurface() {
            return mSurface;
        }

        public void makeCurrent() {
            if (!EGL14.eglMakeCurrent(mEGLDisplay, mEGLSurface, mEGLSurface, mEGLContext)) {
                throw new RuntimeException("eglMakeCurrent failed");
            }
        }

        public void onFrameAvailable(SurfaceTexture surfaceTexture) {
            synchronized (mFrameSyncObject) {
                if (!mFrameAvailable) {
                    mFrameAvailable = true;
                    mFrameSyncObject.notifyAll();
                } else {
                    throw new RuntimeException("mFrameAvailable already set, frame could be dropped");
                }
            }
        }

        public void release() {
            if (mEGLDisplay != EGL14.EGL_NO_DISPLAY) {
                EGL14.eglDestroySurface(mEGLDisplay, mEGLSurface);
                EGL14.eglDestroyContext(mEGLDisplay, mEGLContext);
                EGL14.eglTerminate(mEGLDisplay);
            }

            mEGLDisplay = EGL14.EGL_NO_DISPLAY;
            mEGLContext = EGL14.EGL_NO_CONTEXT;
            mEGLSurface = EGL14.EGL_NO_SURFACE;
            mSurface.release();
            mTextureRender = null;
            mSurface = null;
            mSurfaceTexture = null;
        }
    }

    /* access modifiers changed from: private */
    public static class STextureRender {
        private static final int FLOAT_SIZE_BYTES = 4;
        private static final String FRAGMENT_SHADER = "#extension GL_OES_EGL_image_external : require\nprecision mediump float;\nvarying vec2 vTextureCoord;\nuniform samplerExternalOES sTexture;\nvoid main() {\n    gl_FragColor = texture2D(sTexture, vTextureCoord);\n}\n";
        private static final int TRIANGLE_VERTICES_DATA_POS_OFFSET = 0;
        private static final int TRIANGLE_VERTICES_DATA_STRIDE_BYTES = 20;
        private static final int TRIANGLE_VERTICES_DATA_UV_OFFSET = 3;
        private static final String VERTEX_SHADER = "uniform mat4 uMVPMatrix;\nuniform mat4 uSTMatrix;\nattribute vec4 aPosition;\nattribute vec4 aTextureCoord;\nvarying vec2 vTextureCoord;\nvoid main() {\n    gl_Position = uMVPMatrix * aPosition;\n    vTextureCoord = (uSTMatrix * aTextureCoord).xy;\n}\n";
        private final float[] mMVPMatrix = new float[16];
        private int mProgram;
        private final float[] mSTMatrix = new float[16];
        private int mTextureID = -12345;
        private final FloatBuffer mTriangleVertices;
        private final float[] mTriangleVerticesData;
        private int maPositionHandle;
        private int maTextureHandle;
        private int muMVPMatrixHandle;
        private int muSTMatrixHandle;

        public STextureRender() {
            float[] fArr = {-1.0f, -1.0f, 0.0f, 0.0f, 0.0f, 1.0f, -1.0f, 0.0f, 1.0f, 0.0f, -1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f};
            mTriangleVerticesData = fArr;
            FloatBuffer asFloatBuffer = ByteBuffer.allocateDirect(fArr.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
            mTriangleVertices = asFloatBuffer;
            asFloatBuffer.put(fArr).position(0);
            Matrix.setIdentityM(mSTMatrix, 0);
        }

        public static void checkLocation(int i, String str) {
            if (i < 0) {
                throw new RuntimeException("Unable to locate '" + str + "' in program");
            }
        }

        private int createProgram(String str, String str2) {
            int loadShader = loadShader(GLES20.GL_VERTEX_SHADER, str);
            int i = 0;
            if (loadShader == 0) {
                return 0;
            }

            int loadShader2 = loadShader(GLES20.GL_FRAGMENT_SHADER, str2);
            if (loadShader2 == 0) {
                return 0;
            }

            int glCreateProgram = GLES20.glCreateProgram();
            if (glCreateProgram == 0) {
                Log.println(Log.INFO, "ANZU", "Could not create program");
            }

            GLES20.glAttachShader(glCreateProgram, loadShader);
            checkGlError("glAttachShader");
            GLES20.glAttachShader(glCreateProgram, loadShader2);
            checkGlError("glAttachShader");
            GLES20.glLinkProgram(glCreateProgram);

            int[] iArr = new int[1];
            GLES20.glGetProgramiv(glCreateProgram, 35714, iArr, 0);
            if (iArr[0] != 1) {
                Log.println(Log.INFO, "ANZU", "Could not link program: ");
                Log.println(Log.INFO, "ANZU", GLES20.glGetProgramInfoLog(glCreateProgram));
                GLES20.glDeleteProgram(glCreateProgram);
            } else {
                i = glCreateProgram;
            }
            return i;
        }

        private int loadShader(int i, String str) {
            int glCreateShader = GLES20.glCreateShader(i);
            checkGlError("glCreateShader type=" + i);
            GLES20.glShaderSource(glCreateShader, str);
            GLES20.glCompileShader(glCreateShader);

            int[] iArr = new int[1];
            GLES20.glGetShaderiv(glCreateShader, 35713, iArr, 0);
            if (iArr[0] == 0) {
                Log.println(Log.INFO, "ANZU", "Could not compile shader " + i + ":");
                Log.println(Log.INFO, "ANZU", " " + GLES20.glGetShaderInfoLog(glCreateShader));
                GLES20.glDeleteShader(glCreateShader);
                return 0;
            }
            return glCreateShader;
        }

        public void changeFragmentShader(String str) {
            String str2 = str;
            if (str == null) {
                str2 = FRAGMENT_SHADER;
            }

            GLES20.glDeleteProgram(mProgram);
            mProgram = createProgram(VERTEX_SHADER, str2);
            if (mProgram == 0) {
                throw new RuntimeException("failed creating program");
            }
        }

        public void checkGlError(String str) {
            int glGetError = GLES20.glGetError();
            if (glGetError != 0) {
                Log.println(Log.INFO, "ANZU", str + ": glError " + glGetError);
                throw new RuntimeException(str + ": glError " + glGetError);
            }
        }

        public void drawFrame(SurfaceTexture surfaceTexture, boolean z) {
            checkGlError("onDrawFrame start");
            surfaceTexture.getTransformMatrix(mSTMatrix);
            if (z) {
                float[] fArr = mSTMatrix;
                fArr[5] = -fArr[5];
                fArr[13] = 1.0f - fArr[13];
            }

            GLES20.glClearColor(0.0f, 1.0f, 0.0f, 1.0f);
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
            GLES20.glUseProgram(mProgram);
            checkGlError("glUseProgram");
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(36197 /* what? */, mTextureID);
            mTriangleVertices.position(0);
            GLES20.glVertexAttribPointer(maPositionHandle, 3, GLES20.GL_FLOAT, false, 20, (Buffer) mTriangleVertices);
            checkGlError("glVertexAttribPointer maPosition");
            GLES20.glEnableVertexAttribArray(maPositionHandle);
            checkGlError("glEnableVertexAttribArray maPositionHandle");
            mTriangleVertices.position(3);
            GLES20.glVertexAttribPointer(maTextureHandle, 2, GLES20.GL_FLOAT, false, 20, (Buffer) mTriangleVertices);
            checkGlError("glVertexAttribPointer maTextureHandle");
            GLES20.glEnableVertexAttribArray(maTextureHandle);
            checkGlError("glEnableVertexAttribArray maTextureHandle");
            Matrix.setIdentityM(mMVPMatrix, 0);
            GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, mMVPMatrix, 0);
            GLES20.glUniformMatrix4fv(muSTMatrixHandle, 1, false, mSTMatrix, 0);
            GLES20.glDrawArrays(5, 0, 4);
            checkGlError("glDrawArrays");
            GLES20.glBindTexture(36197 /* what? */, 0);
        }

        public int getTextureId() {
            return mTextureID;
        }

        public void surfaceCreated() {
            mProgram = createProgram(VERTEX_SHADER, FRAGMENT_SHADER);
            if (mProgram != 0) {
                int glGetAttribLocation = GLES20.glGetAttribLocation(mProgram, "aPosition");
                maPositionHandle = glGetAttribLocation;
                checkLocation(glGetAttribLocation, "aPosition");
                int glGetAttribLocation2 = GLES20.glGetAttribLocation(mProgram, "aTextureCoord");
                maTextureHandle = glGetAttribLocation2;
                checkLocation(glGetAttribLocation2, "aTextureCoord");
                int glGetUniformLocation = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
                muMVPMatrixHandle = glGetUniformLocation;
                checkLocation(glGetUniformLocation, "uMVPMatrix");
                int glGetUniformLocation2 = GLES20.glGetUniformLocation(mProgram, "uSTMatrix");
                muSTMatrixHandle = glGetUniformLocation2;
                checkLocation(glGetUniformLocation2, "uSTMatrix");
                int[] iArr = new int[1];
                GLES20.glGenTextures(1, iArr, 0);
                int i = iArr[0];
                mTextureID = i;
                GLES20.glBindTexture(36197, i);
                checkGlError("glBindTexture mTextureID");
                GLES20.glTexParameterf(36197, 10241, 9728.0f);
                GLES20.glTexParameterf(36197, Task.EXTRAS_LIMIT_BYTES, 9729.0f);
                GLES20.glTexParameteri(36197, 10242, 33071);
                GLES20.glTexParameteri(36197, 10243, 33071);
                checkGlError("glTexParameter");
                return;
            }
            throw new RuntimeException("failed creating program");
        }
    }

    private String AsAssetFile(String str) {
        int indexOf = str.indexOf("!/assets/");
        return indexOf != -1 ? str.substring(indexOf + 9) : "";
    }

    /* access modifiers changed from: private */
    public static native boolean BufferLockUnlock(long j, boolean z);

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void DoError() {
        isPlaying = false;
        OnPlaybackError(nativeInstance);
    }

    private static native float GetAudioBufferFullness(long j);

    private static native void OnPlaybackComplete(long j);

    private static native void OnPlaybackError(long j);

    private void Pause() {
        synchronized (pauseSynch) {
            if (!isPaused) {
                pauseStartTime = System.currentTimeMillis();
                isPaused = true;
            }
        }
    }

    private void Resume() {
        synchronized (pauseSynch) {
            if (isPaused) {
                accumulatedPauseTime += System.currentTimeMillis() - pauseStartTime;
                isPaused = false;
                pauseSynch.notifyAll();
            }
        }
    }

    private static native void SetAudioBufferFormat(long j, int i, int i2, int i3);

    private static native boolean ShouldLoop(long j);

    private void Stop() {
        try {
            if (videoDecoder != null) {
                Resume();
                synchronized (mThreadDoneEvent) {
                    if (decoderThreadShouldRun) {
                        synchronized (videoDecoderLock) {
                            synchronized (audioDecoderLock) {
                                decoderThreadShouldRun = false;
                            }
                        }
                        try {
                            mThreadDoneEvent.wait(TapjoyConstants.TIMER_INCREMENT);
                        } catch (Exception e) {
                            /* ~ */
                        }
                    }
                }
                synchronized (videoDecoderLock) {
                    MediaCodec mediaCodec = videoDecoder;
                    if (mediaCodec != null) {
                        if (isPlaying) {
                            mediaCodec.stop();
                        }

                        videoDecoder.release();
                        videoExtractor.release();
                        videoDecoder = null;
                    }
                }
                synchronized (audioDecoderLock) {
                    MediaCodec mediaCodec2 = audioDecoder;
                    if (mediaCodec2 != null) {
                        if (isPlaying) {
                            mediaCodec2.stop();
                        }

                        audioDecoder.release();
                        audioExtractor.release();
                        audioDecoder = null;
                    }
                }
                CodecOutputSurface codecOutputSurface2 = codecOutputSurface;
                if (codecOutputSurface2 != null) {
                    codecOutputSurface2.release();
                    codecOutputSurface = null;
                }

                isPlaying = false;
                nativeInstance = 0;
            }
        } catch (Exception e2) {
            /* ~ */
        }
    }

    private void SynchronousDecodeThread() {
        decoderThreadShouldRun = true;
        new AnzuVideoDecoder$1(this).start();
    }

    class AnzuVideoDecoder$1 extends Thread {
        private final AnzuVideoDecoder anzuVideoDecoder;

        AnzuVideoDecoder$1(AnzuVideoDecoder anzuVideoDecoder) {
            this.anzuVideoDecoder = anzuVideoDecoder;
        }

        public void run() {
            anzuVideoDecoder.inputDone = false;
            anzuVideoDecoder.outputDone = false;
            synchronized (anzuVideoDecoder.videoDecoderLock) {
                anzuVideoDecoder.codecOutputSurface = new AnzuVideoDecoder.CodecOutputSurface(anzuVideoDecoder.videoWidth, anzuVideoDecoder.videoHeight);
                try {
                    anzuVideoDecoder.videoDecoder.configure(anzuVideoDecoder.videoTrackFormat, anzuVideoDecoder.codecOutputSurface.getSurface(), null, 0);
                    anzuVideoDecoder.videoDecoder.start();
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                        anzuVideoDecoder.videoInputBuffers = anzuVideoDecoder.videoDecoder.getInputBuffers();
                    }
                } catch (Exception exception) {
                    Log.println(Log.ERROR, "ANZU", "videoDecoder.dequeueOutputBuffer threw an exception: " + exception.getLocalizedMessage());
                    anzuVideoDecoder.DoError();
                    anzuVideoDecoder.didError = true;
                }

                synchronized (anzuVideoDecoder.audioDecoderLock) {
                    if (!anzuVideoDecoder.didError && anzuVideoDecoder.audioDecoder != null) {
                        anzuVideoDecoder.audioDecoder.configure(anzuVideoDecoder.audioTrackFormat, null, null, 0);
                        anzuVideoDecoder.audioDecoder.start();
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                            anzuVideoDecoder.audioInputBuffers = anzuVideoDecoder.videoDecoder.getInputBuffers();
                            anzuVideoDecoder.audioOutputBuffers = anzuVideoDecoder.videoDecoder.getOutputBuffers();
                        }
                    }

                    // TODO: Complete the code.
                }
            }
        }
    }


    private static native void UpdateRGBA8888Buffer(long j);

    private static native int WriteAudioBuffer(long j, ByteBuffer byteBuffer, int i);

    private void deselectAllTracks(MediaExtractor mediaExtractor) {
        int trackCount = mediaExtractor.getTrackCount();
        for (int i = 0; i < trackCount; i++) {
            mediaExtractor.unselectTrack(i);
        }
    }

    private int selectAudioTrack() {
        return selectTrackOfType(audioExtractor, "audio");
    }

    private int selectTrackOfType(MediaExtractor mediaExtractor, String str) {
        int trackCount = mediaExtractor.getTrackCount();
        for (int i = 0; i < trackCount; i++) {
            if (mediaExtractor.getTrackFormat(i).getString("mime").startsWith(str + "/")) {
                return i;
            }
        }
        return -1;
    }

    private int selectVideoTrack() {
        return selectTrackOfType(videoExtractor, "video");
    }

    boolean FeedVideoBuffers() {
        return false;
    }

    boolean FillAudioBuffers() {
        boolean z;
        int i;
        synchronized (audioDecoderLock) {
            MediaCodec mediaCodec = audioDecoder;
            z = false;
            if (mediaCodec != null) {
                int dequeueInputBuffer = mediaCodec.dequeueInputBuffer(TapjoyConstants.TIMER_INCREMENT);
                z = false;
                if (dequeueInputBuffer >= 0) {
                    int readSampleData = audioExtractor.readSampleData(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP ? audioInputBuffers[dequeueInputBuffer] : audioDecoder.getInputBuffer(dequeueInputBuffer), 0);
                    z = false;
                    if (readSampleData > 0) {
                        audioDecoder.queueInputBuffer(dequeueInputBuffer, 0, readSampleData, audioExtractor.getSampleTime(), 0);
                        audioExtractor.advance();
                        int dequeueOutputBuffer = audioDecoder.dequeueOutputBuffer(info, TapjoyConstants.TIMER_INCREMENT);
                        if (dequeueOutputBuffer == -1) {
                            z = false;
                        } else if (dequeueOutputBuffer == -3) {
                            z = false;
                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                                audioOutputBuffers = audioDecoder.getOutputBuffers();
                                z = false;
                            }
                        } else if (dequeueOutputBuffer == -2) {
                            MediaFormat outputFormat = audioDecoder.getOutputFormat();
                            int integer = outputFormat.getInteger("channel-count");
                            int integer2 = outputFormat.getInteger("sample-rate");
                            audioFrameSize = integer * 2;
                            SetAudioBufferFormat(nativeInstance, 0, integer2, integer);
                            z = false;
                        } else if (dequeueOutputBuffer < 0) {
                            z = false;
                        } else {
                            z = false;
                            if ((info.flags & 4) == 0) {
                                ByteBuffer outputBuffer = Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP ? audioOutputBuffers[dequeueOutputBuffer] : audioDecoder.getOutputBuffer(dequeueOutputBuffer);
                                if (outputBuffer != null && (i = info.size / audioFrameSize) > 0) {
                                    ByteBuffer byteBuffer = directAudioBuffer;
                                    if (byteBuffer == null || byteBuffer.remaining() < outputBuffer.capacity()) {
                                        directAudioBuffer = ByteBuffer.allocateDirect(outputBuffer.capacity());
                                    }
                                    ByteBuffer byteBuffer2 = directAudioBuffer;
                                    if (byteBuffer2 != null) {
                                        try {
                                            byteBuffer2.put(outputBuffer);
                                            directAudioBuffer.rewind();
                                            WriteAudioBuffer(nativeInstance, directAudioBuffer, i);
                                            directAudioBuffer.clear();
                                            z = true;
                                        } catch (Exception e) {
                                            Log.println(Log.ERROR, "ANZU", "exception: insufficient buffer capacity");
                                        }
                                        audioDecoder.releaseOutputBuffer(dequeueOutputBuffer, false);
                                    }
                                }
                                z = false;
                                audioDecoder.releaseOutputBuffer(dequeueOutputBuffer, false);
                            }
                        }
                    }
                }
            }
        }
        return z;
    }

    public boolean HasAudio() {
        return decodesAudio;
    }

    public int GetWidth() {
        return videoWidth;
    }

    public int GetHeight() {
        return videoHeight;
    }

    public double GetDuration() {
        return clipDuration;
    }

    public double GetPlaybackPosition() {
        double d = (double) videoBufferPresentationTime;
        return d / 1000000.0d;
    }

    public ByteBuffer Play(long j, String str, boolean z, int i, int i2, int i3) {
        boolean z2;
        FileDescriptor fd;
        AssetFileDescriptor assetFileDescriptor;
        FileDescriptor fd2;
        synchronized (videoDecoderLock) {
            info = new MediaCodec.BufferInfo();
            nativeInstance = j;
            isPaused = false;
            accumulatedPauseTime = 0;
            String AsAssetFile = AsAssetFile(str);
            z2 = false;
            try {
                AssetFileDescriptor assetFileDescriptor2 = null;
                if (AsAssetFile.length() > 0) {
                    assetFileDescriptor = Anzu.GetContext().getAssets().openFd(AsAssetFile);
                    fd = assetFileDescriptor.getFileDescriptor();
                } else {
                    fd = new FileInputStream(new File(str)).getFD();
                    assetFileDescriptor = null;
                }
                boolean z3 = false;
                if (fd != null) {
                    MediaExtractor mediaExtractor = new MediaExtractor();
                    videoExtractor = mediaExtractor;
                    if (assetFileDescriptor != null) {
                        mediaExtractor.setDataSource(fd, assetFileDescriptor.getStartOffset(), assetFileDescriptor.getLength());
                    } else {
                        mediaExtractor.setDataSource(fd);
                    }
                    deselectAllTracks(videoExtractor);
                    int selectVideoTrack = selectVideoTrack();
                    videoTrackIndex = selectVideoTrack;
                    audioTrackIndex = -1;
                    if (selectVideoTrack >= 0) {
                        videoExtractor.selectTrack(selectVideoTrack);
                        MediaFormat trackFormat = videoExtractor.getTrackFormat(videoTrackIndex);
                        videoTrackFormat = trackFormat;
                        videoWidth = trackFormat.getInteger(TJAdUnitConstants.String.WIDTH);
                        videoHeight = videoTrackFormat.getInteger(TJAdUnitConstants.String.HEIGHT);
                        double d = (double) videoTrackFormat.getLong("durationUs");
                        Double.isNaN(d);
                        clipDuration = d / 1000000.0d;
                        String string = videoTrackFormat.getString("mime");
                        videoMimeFormat = string;
                        videoDecoder = MediaCodec.createDecoderByType(string);
                        ByteBuffer allocateDirect = ByteBuffer.allocateDirect(videoWidth * videoHeight * 4);
                        mPixelBuf = allocateDirect;
                        allocateDirect.order(ByteOrder.LITTLE_ENDIAN);
                    }
                    AssetFileDescriptor assetFileDescriptor3 = null;
                    if (i2 != 0) {
                        audioExtractor = new MediaExtractor();
                        if (AsAssetFile.length() > 0) {
                            assetFileDescriptor3 = Anzu.GetContext().getAssets().openFd(AsAssetFile);
                            fd2 = assetFileDescriptor3.getFileDescriptor();
                        } else {
                            fd2 = new FileInputStream(new File(str)).getFD();
                            assetFileDescriptor3 = null;
                        }
                        if (assetFileDescriptor3 != null) {
                            audioExtractor.setDataSource(fd2, assetFileDescriptor3.getStartOffset(), assetFileDescriptor3.getLength());
                        } else {
                            audioExtractor.setDataSource(fd2);
                        }
                        deselectAllTracks(audioExtractor);
                        audioTrackIndex = selectAudioTrack();
                        synchronized (audioDecoderLock) {
                            int i4 = audioTrackIndex;
                            if (i4 >= 0) {
                                audioExtractor.selectTrack(i4);
                                MediaFormat trackFormat2 = audioExtractor.getTrackFormat(audioTrackIndex);
                                audioTrackFormat = trackFormat2;
                                String string2 = trackFormat2.getString("mime");
                                audioFrameSize = i3 * 2;
                                MediaCodec createDecoderByType = MediaCodec.createDecoderByType(string2);
                                audioDecoder = createDecoderByType;
                                if (createDecoderByType != null) {
                                    decodesAudio = true;
                                }
                            }
                        }
                    }
                    assetFileDescriptor2 = assetFileDescriptor3;
                    z3 = false;
                    if (videoTrackIndex >= 0) {
                        SynchronousDecodeThread();
                        z3 = true;
                        assetFileDescriptor2 = assetFileDescriptor3;
                    }
                }
                if (assetFileDescriptor != null) {
                    assetFileDescriptor.close();
                }
                z2 = z3;
                if (assetFileDescriptor2 != null) {
                    assetFileDescriptor2.close();
                    z2 = z3;
                }
            } catch (Exception e) {
                Log.println(Log.ERROR, "ANZU", "exception opening " + str + ": " + e.getLocalizedMessage());
            }
        }
        if (!z2) {
            DoError();
        }
        return mPixelBuf;
    }
}
