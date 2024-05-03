package me.awie1000.stackcalc;

public class Number {
    boolean isFloat;
    float fValue;
    int iValue;

    public static Number fromInt(int iValue) {
        return new Number(false, 0, iValue);
    }

    public static Number fromFloat(float fValue) {
        return new Number(true, fValue, 0);
    }

    public static Number copy(Number num) {
        return new Number(num.isFloat, num.fValue, num.iValue);
    }

    private Number(boolean isFloat, float fValue, int iValue) {
        this.isFloat = isFloat;
        this.fValue = fValue;
        this.iValue = iValue;
    }

    public float toFloat() {
        return isFloat ? fValue : (float)iValue;
    }

    public int toInt() {
        return isFloat ? (int)fValue : iValue;
    }

    public boolean isZero() {
        return isFloat ? (fValue == 0.0f) : (iValue == 0);
    }

    public Number floor() {
        return Number.fromInt(this.toInt());
    }

    public Number ceil() {
        return Number.fromInt(isFloat ? (int)Math.ceil(fValue) : iValue);
    }

    public Number add(Number other) {
        if(isFloat) return Number.fromFloat(fValue + other.toFloat());
        if(other.isFloat) return Number.fromFloat(this.toFloat() + other.fValue);
        return Number.fromInt(this.iValue + other.iValue);
    }

    public Number sub(Number other) {
        if(isFloat) return Number.fromFloat(fValue - other.toFloat());
        if(other.isFloat) return Number.fromFloat(this.toFloat() - other.fValue);
        return Number.fromInt(this.iValue - other.iValue);
    }

    public Number mul(Number other) {
        if(isFloat) return Number.fromFloat(fValue * other.toFloat());
        if(other.isFloat) return Number.fromFloat(this.toFloat() * other.fValue);
        return Number.fromInt(this.iValue * other.iValue);
    }

    public Number div(Number other) throws NumberError {
        if(other.isZero()) throw new NumberError("Cannot divide by zero!");
        if(!isFloat && !other.isFloat && iValue % other.iValue == 0) return Number.fromInt(iValue / other.iValue);
        return Number.fromFloat(this.toFloat() / other.toFloat());
    }

    public Number intDiv(Number other) throws NumberError {
        return this.div(other).floor();
    }

    public Number mod(Number other) throws NumberError {
        if(other.isZero()) throw new NumberError("Cannot modulus by zero!");
        if(isFloat) return Number.fromFloat(fValue % other.toFloat());
        if(other.isFloat) return Number.fromFloat(this.toFloat() % other.fValue);
        return Number.fromInt(this.iValue % other.iValue);
    }

    @Override
    public String toString() {
        return isFloat ? String.format("%.2f", fValue) : Integer.toString(iValue);
    }
}
