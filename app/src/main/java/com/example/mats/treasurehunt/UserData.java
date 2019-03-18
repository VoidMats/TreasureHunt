package com.example.mats.treasurehunt;

import android.os.Parcel;
import android.os.Parcelable;

public class UserData implements Parcelable {

    private String account;
    private String email;
    private int hunts;
    private String language;
    private String location;
    private String name;
    private String photo;
    private int quiz;
    private int scoreHunts;
    private int scoreQuiz;
    private String username;


    // Constructor
    public UserData() {
        clear();
    }

    // Constructor from parcel object
    protected UserData(Parcel _in) {
        account = _in.readString();
        email = _in.readString();
        hunts = _in.readInt();
        language = _in.readString();
        location = _in.readString();
        name = _in.readString();
        photo = _in.readString();
        quiz = _in.readInt();
        scoreHunts = _in.readInt();
        scoreQuiz = _in.readInt();
        username = _in.readString();
    }

    // Copy constructor
    UserData( UserData ud ) {
        this.account = ud.account;
        this.email = ud.email;
        this.hunts = ud.hunts;
        this.language = ud.language;
        this.location = ud.location;
        this.name = ud.name;
        this.photo = ud.photo;
        this.quiz = ud.quiz;
        this.scoreHunts = ud.scoreHunts;
        this.scoreQuiz = ud.scoreQuiz;
        this.username = ud.username;
    }

    public UserData(String _account, String _email, int _hunts, String _language, String _location,
                    String _name, String _photo, int _quiz, int _scoreHunts, int _scoreQuiz,
                    String _username) {
        this.account = _account;
        this.email = _email;
        this.hunts = _hunts;
        this.language = _language;
        this.location = _location;
        this.name = _name;
        this.photo = _photo;
        this.quiz = _quiz;
        this.scoreHunts = _scoreHunts;
        this.scoreQuiz = _scoreQuiz;
        this.username = _username;
    }

    public static final Creator<UserData> CREATOR = new Creator<UserData>() {
        @Override
        public UserData createFromParcel(Parcel in) {
            return new UserData(in);
        }

        @Override
        public UserData[] newArray(int size) {
            return new UserData[size];
        }
    };

    // Getters
    public String getAccount() {
        return account;
    }
    public String getEmail() { return email; }
    public int getHunts() { return hunts; }
    public String getLanguage() {
        return language;
    }
    public String getLocation() {
        return location;
    }
    public String getName() { return name; }
    public String getPhoto() { return photo; }
    public int getQuiz() { return quiz; }
    public int getScoreHunt() { return scoreHunts; }
    public int getScoreQuiz() { return scoreQuiz; }
    public String getUsername() {
        return username;
    }

    //Setters
    public void setAccount(String _account) {
        this.account = _account;
    }
    public void setEmail(String _email) { this.email = _email; }
    public void setHunts(int _hunts) { this.hunts = _hunts; }
    public void setLanguage(String _language) { this.language = _language; }
    public void setLocation(String _location) {
        this.location = _location;
    }
    public void setName(String _name) { this.name = _name; }
    public void setPhoto(String _photo) { this.photo = _photo; }
    public void setQuiz(int _quiz) { this.quiz = _quiz; }
    public void setScoreHunts(int _scoreHunts) { this.scoreHunts = _scoreHunts; }
    public void setScoreQuiz(int _scoreQuiz) { this.scoreQuiz = _scoreQuiz; }
    public void setUsername(String _name) {
        this.username = _name;
    }

    public void clear() {
        this.account = "Free";
        this.email = "";
        this.hunts = 0;
        this.language = "";
        this.location = "";
        this.name = "";
        this.photo = "";
        this.quiz = 0;
        this.scoreHunts = 0;
        this.scoreQuiz = 0;
        this.username = "";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel _dest, int flags) {
        _dest.writeString(account);
        _dest.writeString(email);
        _dest.writeInt(hunts);
        _dest.writeString(language);
        _dest.writeString(location);
        _dest.writeString(name);
        _dest.writeString(photo);
        _dest.writeInt(quiz);
        _dest.writeInt(scoreHunts);
        _dest.writeInt(scoreQuiz);
        _dest.writeString(username);
    }
}

