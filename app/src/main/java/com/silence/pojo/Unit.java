package com.silence.pojo;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Silence on 2016/2/8 0008.
 */
public class Unit implements Parcelable {
    private int mKey;
    private long mTime;
    private String mMetaKey;

    public Unit() {
    }

    public Unit(int key, long time, String metaKey) {
        mKey = key;
        mTime = time;
        mMetaKey = metaKey;
    }

    public int getKey() {
        return mKey;
    }

    public void setKey(int key) {
        mKey = key;
    }

    public long getTime() {
        return mTime;
    }

    public void setTime(long time) {
        mTime = time;
    }

    public String getMetaKey() {
        return mMetaKey;
    }

    public void setMetaKey(String metaKey) {
        mMetaKey = metaKey;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mKey);
        dest.writeLong(this.mTime);
        dest.writeString(this.mMetaKey);
    }

    protected Unit(Parcel in) {
        this.mKey = in.readInt();
        this.mTime = in.readLong();
        this.mMetaKey = in.readString();
    }

    public static final Parcelable.Creator<Unit> CREATOR = new Parcelable.Creator<Unit>() {
        public Unit createFromParcel(Parcel source) {
            return new Unit(source);
        }

        public Unit[] newArray(int size) {
            return new Unit[size];
        }
    };
}
