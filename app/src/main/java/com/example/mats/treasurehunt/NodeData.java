package com.example.mats.treasurehunt;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * NodeData class contain all values and attributes for one node in the path
 * When the player reach correct GPS coordinates question will be shown.
 *
 * Information of each attribute:
 * answer: Contain the correct answer which will trigger next node
 * question: Contain the question which will be shown for the player
 * image: If the question has an additional information in form of a image. The value is
 * stored in form of a http address.
 * coordLong: Hold the longitude value of the GPS point. Float value
 * coordLati: Hold the latitude value of the GPS point. Float value
 * noTip: Number of tips for this question. All tips are stored in String tip1,tip2,tip3.
 * If noTip is less then 3. Stored values will contain zero value String ("").
 * tip[0]: First tip which player could trigger
 * tip[1]: Second tip which the player could trigger
 * tip[2]: Third tip which the player could trigger
 * type: Set the difficulty of the Hunt
 */
public class NodeData implements Parcelable{

    private String address;
    private String answer;
    private String question;
    private String image;
    private double coordLong;
    private double coordLati;
    private int noTip;
    private String[] tip;
    private int type;
    private int zip;

    public NodeData() {
        clear();
    }

    // Constructor from parcel object
    protected NodeData(Parcel _in) {
        address = _in.readString();
        answer = _in.readString();
        question = _in.readString();
        image = _in.readString();
        coordLong = _in.readDouble();
        coordLati = _in.readDouble();
        noTip = _in.readInt();
        tip = _in.createStringArray();
        type = _in.readInt();
        zip = _in.readInt();
    }

    // Copy constructor
    NodeData(NodeData _n) {
        address = _n.address;
        answer = _n.answer;
        question = _n.question;
        image = _n.image;
        coordLong = _n.coordLong;
        coordLati = _n.coordLati;
        noTip = _n.noTip;
        tip = _n.tip;
        type = _n.type;
        zip = _n.zip;
    }

    public static final Parcelable.Creator<NodeData> CREATOR = new Creator<NodeData>() {
        @Override
        public NodeData createFromParcel(Parcel source) {
            return new NodeData(source);
        }

        @Override
        public NodeData[] newArray(int size) {
            return new NodeData[size];
        }
    };

    // Getters
    public String getAddress() { return address; }
    public String getAnswer() { return answer; }
    public String getQuestion() { return question; }
    public String getImage() { return image; }
    public double getCoordLong() { return coordLong; }
    public double getCoordLati() { return coordLati; }
    public int getNoTip() { return noTip; }
    public String[] getTip() { return tip; }
    public String getTip1() { return tip[0]; }
    public String getTip2() { return tip[1]; }
    public String getTip3() { return tip[2]; }
    public int getType() { return type; }
    public int getZip() { return  zip; }

    // Setters
    public void setAddress(String _address) { this.address = _address; }
    public void setAnswer(String _answer) { this.answer = _answer; }
    public void setQuestion(String _question) { this.question = _question; }
    public void setImage(String _image) { this.image = _image; }
    public void setCoordLong(float _long) { this.coordLong = _long; }
    public void setCoordLati(float _lati) { this.coordLati = _lati; }
    public void setNoTip(int _no) { this.noTip = _no; }
    public void setTip(String[] _tip) { this.tip = _tip; }
    public void setTip1(String _tip1) { this.tip[0] = _tip1; }
    public void setTip2(String _tip2) { this.tip[1] = _tip2; }
    public void setTip3(String _tip3) { this.tip[2] = _tip3; }
    public void setType(int _type) { this.type = _type; }
    public void setZip(int _zip) { this.zip = _zip; }

    public void clear() {
        this.address = "";
        this.answer = "";
        this.question = "";
        this.image = "";
        this.coordLong = 0f;
        this.coordLati = 0f;
        this.noTip = 0;
        this.tip = new String[]{"", "", ""};
        this.type = 0;
        this.zip = 0;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(address);
        dest.writeString(answer);
        dest.writeString(question);
        dest.writeString(image);
        dest.writeDouble(coordLong);
        dest.writeDouble(coordLati);
        dest.writeInt(noTip);
        dest.writeStringArray(tip);
        dest.writeInt(type);
        dest.writeInt(zip);
    }
}
