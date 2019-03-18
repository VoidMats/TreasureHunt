package com.example.mats.treasurehunt;

import android.os.Parcel;
import android.os.Parcelable;

public class HuntData implements Parcelable {
    private Double coordLati;
    private Double coordLong;
    private String id;
    private String name;
    private String location;
    private String country;
    private long time;
    private long totaltime;
    private int type;
    private int noNodes;
    private boolean mIsEmpty;

    public HuntData() {
        this.coordLati = 0d;
        this.coordLong = 0d;
        this.id = "";
        this.name = "";
        this.location = "";
        this.country = "";
        this.time = 0;
        this.totaltime = 0;
        this.type = 0;
        this.noNodes = 0;
        this.mIsEmpty = true;
    }

    protected HuntData(Parcel _in) {
        coordLati = _in.readDouble();
        coordLong = _in.readDouble();
        id = _in.readString();
        name = _in.readString();
        location = _in.readString();
        country = _in.readString();
        time = _in.readLong();
        totaltime = _in.readLong();
        type = _in.readInt();
        noNodes = _in.readInt();
        mIsEmpty = _in.readByte() != 0;
    }

    public HuntData(Double _coordLati, Double _coordLong, String _path, String _name,
                    String _location, String _country, long _time, long _totaltime,
                    int _type, int _noNodes) {
        this.coordLati = _coordLati;
        this.coordLong = _coordLong;
        this.id = _path;
        this.name = _name;
        this.location = _location;
        this.country = _country;
        this.time = _time;
        this.totaltime = _totaltime;
        this.type = _type;
        this.noNodes = _noNodes;
        this.mIsEmpty = false;
    }

    public HuntData(HuntData _tmp) {
        coordLati = _tmp.coordLati;
        coordLong = _tmp.coordLong;
        id = _tmp.id;
        name = _tmp.name;
        location = _tmp.location;
        country = _tmp.country;
        time = _tmp.time;
        totaltime = _tmp.totaltime;
        type = _tmp.type;
        noNodes = _tmp.noNodes;
        mIsEmpty = false;
    }

    public static final Parcelable.Creator<HuntData> CREATOR = new Creator<HuntData>() {
        @Override
        public HuntData createFromParcel(Parcel source) {
            return new HuntData(source);
        }

        @Override
        public HuntData[] newArray(int size) {
            return new HuntData[size];
        }
    };

    // Getters
    public Double getCoordLati;
    public Double getCoordLong;
    public String getId() { return id; }
    public String getName() { return name; }
    public String getLocation() { return location; }
    public String getCountry() { return country; }
    public long getTime() { return time; }
    public long getTotaltime() { return totaltime; }
    public int getType() { return  type; }
    public int getNoNodes() { return noNodes; }

    // Setters
    public void setCoordLati(Double _lati) {
        checkEmpty();
        this.coordLati = _lati;
    }
    public void setCoordLong(Double _long) {
        checkEmpty();
        this.coordLong = _long;
    }
    public void setId(String _id) {
        checkEmpty();
        this.id = _id;
    }
    public void setName(String _name) {
        checkEmpty();
        this.name = _name;
    }
    public void setLocation(String _location) {
        checkEmpty();
        this.location = _location;
    }
    public void setCountry(String _country) {
        checkEmpty();
        this.country = _country;
    }
    public void setTime(long _time) {
        checkEmpty();
        this.time = _time;
    }
    public void setTotaltime(long _totaltime) {
        checkEmpty();
        this.totaltime = _totaltime;
    }
    public void setType(int _type) {
        checkEmpty();
        this.type = _type;
    }
    public void setNoNodes(int _noNodes) {
        checkEmpty();
        this.noNodes = _noNodes;
    }

    public void clear() {
        this.coordLati = 0d;
        this.coordLong = 0d;
        this.id = "";
        this.name = "";
        this.location = "";
        this.country = "";
        this.time = 0;
        this.totaltime = 0;
        this.type = 0;
        this.noNodes = 0;
        this.mIsEmpty = true;
    }

    /** Generate a pathId from the data it contains.
     *  Formula:
     *  [CountryCode][TypeOfgame][Location][NumberOfNodes]_[Name]
     */
    private String generatePathId() {
        StringBuilder tmpPathId = new StringBuilder();


        return tmpPathId.toString();
    }

    public boolean isEmpty() {
        if( mIsEmpty == true && id == "" )
            return true;
        else {
            return false;
        }
    }

    private void checkEmpty(){
        if( mIsEmpty == false )
            mIsEmpty = true;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel _dest, int _flags) {
        _dest.writeDouble(coordLati);
        _dest.writeDouble(coordLong);
        _dest.writeString(id);
        _dest.writeString(name);
        _dest.writeString(location);
        _dest.writeString(country);
        _dest.writeLong(time);
        _dest.writeLong(totaltime);
        _dest.writeInt(type);
        _dest.writeInt(noNodes);
        _dest.writeByte((byte) (mIsEmpty ? 1 : 0));
    }
}
