package com.ybg.ga.ymga.ga.tz;

import java.io.Serializable;

/**
 * Created by yangbagang on 15/10/10.
 */
public class TzBean implements Serializable {

    private static final long serialVersionUID = -8598198009968467076L;

    private float tzValue;
    private float tzZFValue;
    private float tzJRValue;
    private float tzSFValue;
    private float tzBMIValue;
    private float tzQZValue;
    private float tzGGValue;
    private int tzNZValue;
    private int tzJCValue;
    private int tzSTValue;

    public TzBean() {

    }

    public TzBean(float tzValue) {
        this.tzValue = tzValue;
    }

    public TzBean(float tzValue, float tzZFValue, float tzJRValue, float tzSFValue, float tzBMIValue, float tzQZValue, float tzGGValue, int tzNZValue, int tzJCValue, int tzSTValue) {
        this.tzValue = tzValue;
        this.tzZFValue = tzZFValue;
        this.tzJRValue = tzJRValue;
        this.tzSFValue = tzSFValue;
        this.tzBMIValue = tzBMIValue;
        this.tzQZValue = tzQZValue;
        this.tzGGValue = tzGGValue;
        this.tzNZValue = tzNZValue;
        this.tzJCValue = tzJCValue;
        this.tzSTValue = tzSTValue;
    }

    public float getTzValue() {
        return tzValue;
    }

    public void setTzValue(float tzValue) {
        this.tzValue = tzValue;
    }

    public float getTzZFValue() {
        return tzZFValue;
    }

    public void setTzZFValue(float tzZFValue) {
        this.tzZFValue = tzZFValue;
    }

    public float getTzJRValue() {
        return tzJRValue;
    }

    public void setTzJRValue(float tzJRValue) {
        this.tzJRValue = tzJRValue;
    }

    public float getTzSFValue() {
        return tzSFValue;
    }

    public void setTzSFValue(float tzSFValue) {
        this.tzSFValue = tzSFValue;
    }

    public float getTzBMIValue() {
        return tzBMIValue;
    }

    public void setTzBMIValue(float tzBMIValue) {
        this.tzBMIValue = tzBMIValue;
    }

    public float getTzQZValue() {
        return tzQZValue;
    }

    public void setTzQZValue(float tzQZValue) {
        this.tzQZValue = tzQZValue;
    }

    public float getTzGGValue() {
        return tzGGValue;
    }

    public void setTzGGValue(float tzGGValue) {
        this.tzGGValue = tzGGValue;
    }

    public int getTzNZValue() {
        return tzNZValue;
    }

    public void setTzNZValue(int tzNZValue) {
        this.tzNZValue = tzNZValue;
    }

    public int getTzJCValue() {
        return tzJCValue;
    }

    public void setTzJCValue(int tzJCValue) {
        this.tzJCValue = tzJCValue;
    }

    public int getTzSTValue() {
        return tzSTValue;
    }

    public void setTzSTValue(int tzSTValue) {
        this.tzSTValue = tzSTValue;
    }
}
