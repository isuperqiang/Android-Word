package com.silence.utils;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class WavWriter {
    /**
     * constant define
     */
    private static final int SIZE_OF_WAVE_HEADER = 44;
    private static final String CHUNK_ID = "RIFF";
    private static final String FORMAT = "WAVE";
    private static final String SUB_CHUNK1_ID = "fmt ";
    private static final int SUB_CHUNK1_SIZE = 16;
    private static final String SUB_CHUNK2_ID = "data";
    private static final short FORMAT_PCM = 1; // Indicates PCM format.
    private static final short DEFAULT_NUM_CHANNELS = 1;
    private static final short DEFAULT_BITS_PER_SAMPLE = 16;

    /**
     * member properties
     */
    private RandomAccessFile mInternalWriter;
    private short mNumChannels;
    private int mSampleRate;
    private short mBitsPerSample;

    public WavWriter(File file, int sample) throws IOException {
        init(file, DEFAULT_NUM_CHANNELS, sample, DEFAULT_BITS_PER_SAMPLE);
    }

    private boolean init(File file, short numChannels, int sampleRate, short bitsPerSample) throws IOException {
        if (null == file) {
            return false;
        }
        mInternalWriter = new RandomAccessFile(file, "rw");
        mNumChannels = numChannels;
        mSampleRate = sampleRate;
        mBitsPerSample = bitsPerSample;
        byte[] buffer = new byte[SIZE_OF_WAVE_HEADER];
        mInternalWriter.write(buffer);
        return true;
    }

    public void write(byte[] buffer) throws IOException {
        mInternalWriter.write(buffer);
    }

    public void writeChars(String val) throws IOException {
        for (int i = 0; i < val.length(); i++) {
            mInternalWriter.write(val.charAt(i));
        }
    }

    public void writeInt(int val) throws IOException {
        mInternalWriter.write(val >> 0);
        mInternalWriter.write(val >> 8);
        mInternalWriter.write(val >> 16);
        mInternalWriter.write(val >> 24);
    }

    public void writeShort(short val) throws IOException {
        mInternalWriter.write(val >> 0);
        mInternalWriter.write(val >> 8);
    }

    public int getDataSize() throws IOException {
        return (int) (mInternalWriter.length() - SIZE_OF_WAVE_HEADER);
    }

    public void writeHeader() throws IOException {
        /* RIFF header */
        mInternalWriter.seek(0);
        writeChars(CHUNK_ID);

        writeInt(36 + getDataSize());
        writeChars(FORMAT);

        /** format chunk */
        writeChars(SUB_CHUNK1_ID);
        writeInt(SUB_CHUNK1_SIZE);
        writeShort(FORMAT_PCM);
        writeShort(mNumChannels);
        writeInt(mSampleRate);

        writeInt(mNumChannels * mSampleRate * mBitsPerSample / 8);
        writeShort((short) (mNumChannels * mBitsPerSample / 8));
        writeShort(mBitsPerSample);

        /** data chunk */
        writeChars(SUB_CHUNK2_ID);
        writeInt(getDataSize());
    }

    public void close() throws IOException {
        if (mInternalWriter != null) {
            mInternalWriter.close();
            mInternalWriter = null;
        }
    }
}
