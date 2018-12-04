package com.ont.media.player.sound;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.util.Log;

import com.ont.media.player.IVideoView;
import com.ont.media.player.util.L;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import tv.danmaku.ijk.media.player.IMediaPlayer;

/**
 * Created by betali on 2018/5/25.
 */

public class AudioEncoder {

    private static final String TAG = AudioEncoder.class.getSimpleName();
    private static final String AUDIO_MIME_TYPE = "audio/mp4a-latm";

    // config
    private MediaCodec mEncoder;
    private int mSampleRate = IEncodeDef.AUDIO_SAMPLE_RATE;
    private int mChannelConfig = IEncodeDef.AUDIO_CHANNEL_CONFIG;
    private int mFormat = IEncodeDef.AUDIO_FORMAT;
    private int mBitrate = IEncodeDef.AUDIO_BITRATE;
    private int mKeyProfile = IEncodeDef.AUDIO_KEY_PROFILE;

    private long mStartTimeUs;
    private long mLastTimestamp;
    private IVideoView mVideoView;
    private MediaCodec.BufferInfo mBufferInfo;
    private Map<Long, Object> mReserveBuffers;

    public AudioEncoder(IVideoView videoView) {

        mVideoView = videoView;
        mReserveBuffers = new HashMap<Long, Object>();
        mLastTimestamp = -1;
    }

    public void setSampleRate(int sampleRate) {

        this.mSampleRate = sampleRate;
    }

    public void setChannelConfig(int channelConfig) {

        this.mChannelConfig = channelConfig;
    }

    public void setFormat(int format) {

        this.mFormat = format;
    }

    public void setBitrate(int bitrate) {

        this.mBitrate = bitrate;
    }

    public void setmKeyProfile(int keyProfile) {

        this.mKeyProfile = keyProfile;
    }

    public boolean start() {

        boolean ret = init();
        if (!ret) {

            return false;
        }

        mStartTimeUs = System.nanoTime() / 1000L;
        mBufferInfo = new MediaCodec.BufferInfo();
        mEncoder.start();
        return true;
    }

    public void stop() {

        try {
            if (mEncoder != null) {
                mEncoder.stop();
                mEncoder.release();
                mEncoder = null;
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        mBufferInfo = null;
        mReserveBuffers.clear();
        mLastTimestamp = -1;
    }

    public void onGetFrame(byte[] pcmFrame, int length) {

        ByteBuffer[] inputBuffers = mEncoder.getInputBuffers();
        ByteBuffer[] outputBuffers = mEncoder.getOutputBuffers();

        int inputBufferIndex = mEncoder.dequeueInputBuffer(-1);
        if (inputBufferIndex >= 0) {

            ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
            inputBuffer.clear();
            int bufferRemaining = inputBuffer.remaining();
            if (bufferRemaining < length) {
                inputBuffer.put(pcmFrame, 0, bufferRemaining);
            } else {
                inputBuffer.put(pcmFrame, 0, length);
            }

            long timestamp = System.nanoTime() / 1000L - mStartTimeUs;
            mEncoder.queueInputBuffer(inputBufferIndex, 0, inputBuffer.position(), timestamp, 0);
        } else {

            L.e(TAG, "audio encode get input buffer error!");
        }

        while (true) {

            int outputBufferIndex = mEncoder.dequeueOutputBuffer(mBufferInfo, 0);
            if (outputBufferIndex >= 0) {

                ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];
                if (outputBuffer == null) {

                    L.e(TAG, "audio encode get output buffer error!");
                    continue;
                }

                outputBuffer.position(mBufferInfo.offset);
                outputBuffer.limit(mBufferInfo.offset + mBufferInfo.size);
                long presentationTimeInMillis = mBufferInfo.presentationTimeUs / 1000L;

                if (presentationTimeInMillis < mLastTimestamp) {
                    continue;
                } else if (presentationTimeInMillis == mLastTimestamp) {
                    presentationTimeInMillis++;
                }

                int packetLen = mBufferInfo.size + 7;
                byte[] data = getBuffer(packetLen, mLastTimestamp, presentationTimeInMillis);
                fillADTSHeader(data, packetLen);
                outputBuffer.get(data, 7, mBufferInfo.size);
                outputBuffer.position(mBufferInfo.offset);

                mVideoView.writeDuplex(IMediaPlayer.DispatcherPacketType.DISPATCHER_PT_SOUND, data, packetLen, presentationTimeInMillis);
                mEncoder.releaseOutputBuffer(outputBufferIndex, false);
                mLastTimestamp = presentationTimeInMillis;

                Log.d(TAG, "encode duplex audio ts = " + presentationTimeInMillis + "  size = " + packetLen);

            } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {

                outputBuffers = mEncoder.getOutputBuffers();
            } else {

                break;
            }
        }
    }

    public byte[] getBuffer(int size, long lastSentFrameTimestamp, long currentTimeStamp)
    {
        /**
         * how does it work?
         * we put byte array with their timestamp value to a hash map
         * when there is a new output buffer array, we check the last frame timestamp of mediamuxer
         * if the byte buffer timestamp is less than the value of last frame timestamp of mediamuxer
         * it means that we can use that byte buffer again because it is already written to network
         */
        Iterator<Map.Entry<Long, Object>> iterator = mReserveBuffers.entrySet().iterator();
        while(iterator.hasNext()) {
            Map.Entry<Long, Object> next = iterator.next();
            if (next.getKey() <= lastSentFrameTimestamp)
            {
                // it means this frame is sent
                byte[] value = (byte[]) next.getValue();
                iterator.remove();
                if (value.length >= size)
                {
                    mReserveBuffers.put(currentTimeStamp, value);
                    return value;
                }
            }
        }

        byte[] data = new byte[size];
        mReserveBuffers.put(currentTimeStamp, data);
        return data;
    }

    private boolean init() {

        MediaFormat audioFormat = MediaFormat.createAudioFormat(AUDIO_MIME_TYPE, mSampleRate, mChannelConfig == AudioFormat.CHANNEL_IN_STEREO ? 2 : 1);
        audioFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
        audioFormat.setInteger(MediaFormat.KEY_BIT_RATE, mBitrate);
        audioFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, AudioRecord.getMinBufferSize(mSampleRate, mChannelConfig, mFormat));

        try {

            mEncoder = MediaCodec.createEncoderByType(AUDIO_MIME_TYPE);
            mEncoder.configure(audioFormat, null /* surface */, null /* crypto */, MediaCodec.CONFIGURE_FLAG_ENCODE);
        } catch (IOException | IllegalStateException e) {

            e.printStackTrace();
            mEncoder = null;
            return false;
        }

        return true;
    }

    private void fillADTSHeader(byte[] data, int packetLen) {

        int sampleRateIndex = 0;
        int mpeg4SampleRates[] = {
            96000, 88200, 64000, 48000, 44100, 32000,
                    24000, 22050, 16000, 12000, 11025, 8000, 7350
        };
        for (; sampleRateIndex < 13; sampleRateIndex++) {

            if (mpeg4SampleRates[sampleRateIndex] == mSampleRate) {

                break;
            }
        }

        /*
        channel_configuration: 表示声道数chanCfg
        0: Defined in AOT Specifc Config
        1: 1 channel: front-center
        2: 2 channels: front-left, front-right
        3: 3 channels: front-center, front-left, front-right
        4: 4 channels: front-center, front-left, front-right, back-center
        5: 5 channels: front-center, front-left, front-right, back-left, back-right
        6: 6 channels: front-center, front-left, front-right, back-left, back-right, LFE-channel
        7: 8 channels: front-center, front-left, front-right, side-left, side-right, back-left, back-right, LFE-channel
        8-15: Reserved
        */
        int channelConfig = mChannelConfig == AudioFormat.CHANNEL_IN_STEREO ? 2 : 1;

        // fill in ADTS data
        data[0] = (byte)0xFF;
        data[1] = (byte)0xF9;
        data[2] = (byte)(((mKeyProfile - 1) << 6) + (sampleRateIndex << 2) +(channelConfig >> 2));
        data[3] = (byte)(((channelConfig & 3) << 6) + (packetLen >> 11));
        data[4] = (byte)((packetLen & 0x7FF) >> 3);
        data[5] = (byte)(((packetLen & 7) << 5) + 0x1F);
        data[6] = (byte)0xFC;
    }
}
