package com.ont.media.player.sound;

import android.media.AudioFormat;
import android.media.MediaCodecInfo;

/**
 * Created by betali on 2018/5/25.
 */

public interface IEncodeDef {

    int AUDIO_SAMPLE_RATE = 44100;
    int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    int AUDIO_BITRATE = 64 * 1024;
    int AUDIO_CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    int AUDIO_KEY_PROFILE = MediaCodecInfo.CodecProfileLevel.AACObjectLC;
}
