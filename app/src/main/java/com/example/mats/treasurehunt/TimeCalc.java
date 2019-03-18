package com.example.mats.treasurehunt;

import android.os.Parcelable;

public class TimeCalc {
    private long ms;
    private int sec;
    private int min;
    private int hour;
    private boolean neg;

    public TimeCalc() {
        clear();
    }

    public TimeCalc(long _time) {
        this.ms = _time;
        calcTime();
    }

    // Setters
    public void setMs(long _ms) {
        this.ms = _ms;
        calcTime();
    }
    public void setNeg(boolean _neg) { this.neg = _neg;}

    // Getters
    public long getMs() {
        return ms;
    }
    public int getSec() {
        return sec;
    }
    public int getMin() {
        return min;
    }
    public int getHour() {
        return hour;
    }
    public boolean getNeg() { return neg; }

    public void removeTime(long _time) {
        if (neg) {
            ms += _time;
        }
        else {
            ms -= _time;
        }
        calcTime();
    }

    private void calcTime() {
        this.sec = (int) ( ms / 1000);
        this.min = sec / 60;
        this.sec = sec % 60;
        this.hour = min / 24;
        this.min = min % 60;
    }

    public String getTimeString() {
        StringBuilder sb = new StringBuilder(String.format("%02d:%02d:%02d", hour, min, sec ));
        if (neg) sb.insert(0,'-');
        return sb.toString();
    }

    public String getTimeString(long _time) {
        int sec = (int) ( _time / 1000);
        int min = sec / 60;
        sec = sec % 60;
        int hour = min / 24;
        min = min % 60;
        return String.format("%02d:%02d:%02d", hour, min, sec );
    }

    public void clear() {
        this.ms = 0;
        this.sec = 0;
        this.min = 0;
        this.hour = 0;
        this.neg = false;
    }
}
