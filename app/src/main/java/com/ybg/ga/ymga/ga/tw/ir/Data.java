package com.ybg.ga.ymga.ga.tw.ir;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by yangbagang on 15/6/17.
 */
public class Data implements Parcelable {

    public final static int YOK = 0xAA;
    public final static int YRESULT=0X00;
    public final static int YMU = 0X01;
    public final static int EONE = 0x81;
    public final static int ETWO = 0x82;
    public final static int ETHR = 0x83;
    public final static int EFOU = 0x84;
    public final static int EFIV = 0x85;
    public final static int ESIX = 0x86;
    public final static int ESEV = 0x87;
    public final static int EEIG = 0x88;
    private int head;
    private int dataone;
    private int datatwo,three,four,five,sex;

    public Data(){

    }

    public int getSex() {
        return sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

    public int getThree() {
        return three;
    }

    public void setThree(int three) {
        this.three = three;
    }

    public int getFour() {
        return four;
    }

    public void setFour(int four) {
        this.four = four;
    }

    public int getFive() {
        return five;
    }

    public void setFive(int five) {
        this.five = five;
    }


    public int getHead() {
        return head;
    }

    public void setHead(int head) {
        this.head = head;
    }

    public int getDataone() {
        return dataone;
    }

    public void setDataone(int dataone) {
        this.dataone = dataone;
    }

    public int getDatatwo() {
        return datatwo;
    }

    public void setDatatwo(int datatwo) {
        this.datatwo = datatwo;
    }


    public void analysis(int[] i) {

        head = i[0];
        dataone = i[1];
        datatwo = i[2];
        three = i[3];
        four = i[4];
        five = i[5];
        sex = i[6];
        //System.out.println(" head:" + head+"two:"+datatwo+","+i[2]+","+i[3]+","+i[4]+","+i[5]+","+i[6]+","+i[7]);
    }
    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        // TODO Auto-generated method stub
        dest.writeFloat(head);
        dest.writeFloat(dataone);
        dest.writeFloat(datatwo);
        dest.writeFloat(three);
        dest.writeFloat(four);
        dest.writeFloat(five);
        dest.writeFloat(sex);
    }
    public static final Parcelable.Creator<Data> CREATOR = new Parcelable.Creator<Data>() {
        public Data createFromParcel(Parcel in) {
            return new Data(in);
        }

        public Data[] newArray(int size) {
            return new Data[size];
        }
    };

    private Data(Parcel in) {
        head  = in.readInt();
        dataone = in.readInt();
        datatwo = in.readInt();
        three = in.readInt();
        four = in.readInt();
        five = in.readInt();
        sex = in.readInt();
    }

}
