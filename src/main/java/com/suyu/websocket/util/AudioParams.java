package com.suyu.websocket.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

/**
 * Copyright (C), 2019 <br>
 * <br>
 * All rights reserved <br>
 * <br>
 *
 * @author dnwang
 * @since 2019/2/16,10:53
 */
public final class AudioParams implements Serializable {

    private static final int MIN_HEADER_LENGTH = 44;

    public static final int SAMPLE_RATE_16K = 16000; // 16K采样率
    public static final int SAMPLE_RATE_8K = 8000; // 8K采样率

    public static final int CHANNEL_MONO = 1; // 单声道
    public static final int CHANNEL_STEREO = 2; // 双声道

    public static final int ENCODING_PCM_16BIT = 16; // 编码格式
    public static final int ENCODING_PCM_8BIT = 8;

    public static final int LINEAR_ENCODING = 1;

    public int sampleRate;
    public int channel;
    public int format;
    public int encodeMode;
    public int frameSize;
    public int frameRate;
    public int pcmLen;

    private AudioParams() {
    }

    public AudioParams(int sampleRate,
                       int channel,
                       int format,
                       int pcmLen) {
        this(sampleRate, channel, format,
                LINEAR_ENCODING, // pcm encoding
                channel * (format / 8),
                channel * sampleRate / (format / 8),
                pcmLen); // pcm len
    }

    public AudioParams(int sampleRate,
                       int channel,
                       int format,
                       int encodeMode,
                       int frameSize,
                       int frameRate,
                       int pcmLen) {
        this.sampleRate = sampleRate;
        this.channel = channel;
        this.format = format;
        this.encodeMode = encodeMode;
        this.frameSize = frameSize;
        this.frameRate = frameRate;
        this.pcmLen = pcmLen;
    }

    public byte[] createWAVHeader() {
        final byte[] header = new byte[MIN_HEADER_LENGTH];
        // RIFF
        header[0] = 'R';
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        // RIFF chunk size: file len - 8byte('RIFF'+Size)
        final int riffChunkSize = pcmLen + header.length - 8;
        header[4] = (byte) (riffChunkSize & 0xff);
        header[5] = (byte) ((riffChunkSize >> 8) & 0xff);
        header[6] = (byte) ((riffChunkSize >> 16) & 0xff);
        header[7] = (byte) ((riffChunkSize >> 24) & 0xff);
        // WAVE
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        // fmt
        header[12] = 'f';
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        // fmt chunk size : fixed 16byte(16~35) + extra info
        final int minFmtChunkSize = 16; // no extra info
        header[16] = (byte) (minFmtChunkSize & 0xff);
        header[17] = (byte) ((minFmtChunkSize >> 8) & 0xff);
        header[18] = (byte) ((minFmtChunkSize >> 16) & 0xff);
        header[19] = (byte) ((minFmtChunkSize >> 24) & 0xff);
        // pcm encoding format 2byte, 1:linear, other: adpcm,...
        header[20] = (byte) (encodeMode & 0xff);
        header[21] = (byte) ((encodeMode >> 8) & 0xff);
        // channel size 2byte
        header[22] = (byte) (channel & 0xff);
        header[23] = (byte) ((channel >> 8) & 0xff);
        // sampleRate 4byte
        header[24] = (byte) (sampleRate & 0xff);
        header[25] = (byte) ((sampleRate >> 8) & 0xff);
        header[26] = (byte) ((sampleRate >> 16) & 0xff);
        header[27] = (byte) ((sampleRate >> 24) & 0xff);
        // frame rate 4byte
        header[28] = (byte) (frameRate & 0xff);
        header[29] = (byte) ((frameRate >> 8) & 0xff);
        header[30] = (byte) ((frameRate >> 16) & 0xff);
        header[31] = (byte) ((frameRate >> 24) & 0xff);
        // frame size 2byte
        header[32] = (byte) (frameSize & 0xff);
        header[33] = (byte) ((frameSize >> 8) & 0xff);
        // format 2byte
        header[34] = (byte) (format & 0xff);
        header[35] = (byte) ((format >> 8) & 0xff);
        // data
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        // fmt chunk size : pcm len
        header[40] = (byte) (pcmLen & 0xff);
        header[41] = (byte) ((pcmLen >> 8) & 0xff);
        header[42] = (byte) ((pcmLen >> 16) & 0xff);
        header[43] = (byte) ((pcmLen >> 24) & 0xff);
        return header;
    }

    public static AudioParams parseParamsFrom(InputStream wav, OutputStream origin) {
        AudioParams p = null;
        try {
            final byte[] flag = new byte[4 + 4 + 4]; // 'RIFF' + size + 'WAVE'
            fixedRead(wav, flag, origin);
            final String riff = getString(flag, 0, 4).trim();
            final String wave = getString(flag, 8, 4).trim();
            if ("RIFF".equalsIgnoreCase(riff) && "WAVE".equalsIgnoreCase(wave)) {
                p = new AudioParams();
                parseChunk(wav, p, origin);
            } else {
                System.out.println("[AudioParams] >> can't parse audio header ! skip");
            }
        } catch (Exception e) {
            System.out.println("[AudioParams] >> can't parse audio header ! skip");
        }
        return p;
    }

    /**
     * https://sites.google.com/site/musicgapi/technical-documents/wav-file-format#list
     */
    private static void parseChunk(InputStream wav, AudioParams p, OutputStream origin) throws Exception {
        final byte[] chunk = new byte[4 + 4]; // name + size
        fixedRead(wav, chunk, origin);
        final String name = getString(chunk, 0, 4).trim();
        final int size = getInt(chunk, 4, 4);
        if ("fmt".equalsIgnoreCase(name)) {
            byte[] info = new byte[size];
            fixedRead(wav, info, origin);
            p.encodeMode = getInt(info, 0, 2);
            p.channel = getInt(info, 2, 2);
            p.sampleRate = getInt(info, 4, 4);
            p.frameRate = getInt(info, 8, 4);
            p.frameSize = getInt(info, 12, 2);
            p.format = getInt(info, 14, 2);
            parseChunk(wav, p, origin);
        } else if ("data".equalsIgnoreCase(name)) {
            p.pcmLen = size;
            // end parse !
        } else {
            // skip other chunk block, fact/list/...
            if (size > 0) {
                fixedRead(wav, new byte[size], origin);
            }
            parseChunk(wav, p, origin);
        }
    }

    private static int getInt(byte[] buf, int start, int len) {
        int target = 0;
        int index = 0;
        while (index < len) {
            target += (buf[start + index] & 0xff) << (8 * index);
            index++;
        }
        return target;
    }

    private static String getString(byte[] buf, int start, int len) {
        byte[] data = new byte[len];
        System.arraycopy(buf, start, data, 0, len);
        return new String(data);
    }

    private static void fixedRead(InputStream source, byte[] buf, OutputStream origin) throws IOException {
        final int len = source.read(buf);
        if (len > 0 && null != origin) {
            origin.write(buf, 0, len);
        }
//        System.out.println("[AudioParams] >> header read: " + len + "/" + buf.length);
    }

    public final AudioParams copy() {
        return new AudioParams(
                sampleRate,
                channel,
                format,
                encodeMode,
                frameSize,
                frameRate,
                pcmLen);
    }

    public final long getDuration() {
        return (long) ((pcmLen * 1.0f / (sampleRate * channel * (format / 8)) * 1000));
    }

    @Override
    public String toString() {
        return "AudioParams{" +
                "sampleRate=" + sampleRate +
                ", channel=" + channel +
                ", format=" + format +
                ", encodeMode=" + encodeMode +
                ", frameSize=" + frameSize +
                ", frameRate=" + frameRate +
                ", pcmLen=" + pcmLen +
                '}';
    }

}
