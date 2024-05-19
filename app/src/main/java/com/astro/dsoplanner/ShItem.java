package com.astro.dsoplanner;

import com.astro.dsoplanner.base.Exportable;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;

/**
 * for my implementation of shared prefs
 *
 * @author leonid
 */
public class ShItem implements Exportable {
    public static final int STRING = 1;

    @Override
    public String toString() {
        String value = "";
        switch (type) {
            case STRING:
                value = svalue;
                break;
            case INT:
                value = "" + ivalue;
                break;
            case BOOLEAN:
                value = "" + bvalue;
                break;
            case FLOAT:
                value = "" + fvalue;
                break;
            case LONG:
                value = "" + lvalue;
                break;

        }

        return "ShItem [name=" + name + ", type=" + type + ", value=" + value + "]";
    }

    public static final int INT = 2;
    public static final int BOOLEAN = 3;
    public static final int FLOAT = 4;
    public static final int LONG = 5;
    public static final int BYTE = 6;
    public static final int INT_ARR = 7;

    String name;
    int type;
    int ivalue;
    boolean bvalue;
    String svalue;
    float fvalue;
    long lvalue;
    byte[] bytevalue;
    int[] int_arr_value;


    /**
     * now works with BYTE array and INT only!!!
     */
    public ShItem(String name, int type, int ivalue, boolean bvalue, String svalue,
                  float fvalue, long lvalue, byte[] bytevalue) {
        this.name = name;
        this.type = type;
        this.ivalue = ivalue;
        this.bvalue = bvalue;
        this.svalue = svalue;
        this.fvalue = fvalue;
        this.lvalue = lvalue;
        this.bytevalue = bytevalue;
    }

    public ShItem(String name, int ivalue) {
        this.name = name;
        this.type = INT;
        this.ivalue = ivalue;
    }

    public ShItem(String name, byte[] bytevalue) {
        this.name = name;
        this.type = BYTE;
        this.bytevalue = bytevalue;
    }

    public ShItem(String name, int[] intarr) {
        this.name = name;
        int_arr_value = intarr;
        this.type = INT_ARR;
    }

    public ShItem(String name, boolean bvalue) {
        this.name = name;
        this.bvalue = bvalue;
    }

    public ShItem(String name, float fvalue) {
        this.name = name;
        this.fvalue = fvalue;
    }

    public ShItem(String name, long lvalue) {
        this.name = name;
        this.lvalue = lvalue;
    }

    public ShItem(String name, String svalue) {
        this.name = name;
        this.svalue = svalue;
    }

    public ShItem(String name, Object o) throws UnsupportedOperationException {
        if (o == null)
            throw new UnsupportedOperationException();

        this.name = name;
        if (o instanceof Boolean) {
            type = BOOLEAN;
            bvalue = (Boolean) o;
        } else if (o instanceof Float) {
            type = FLOAT;
            fvalue = (Float) o;
        } else if (o instanceof Integer) {
            type = INT;
            ivalue = (Integer) o;
        } else if (o instanceof Long) {
            type = LONG;
            lvalue = (Long) o;
        } else if (o instanceof String) {
            type = STRING;
            svalue = (String) o;
        } else
            throw new UnsupportedOperationException();
    }

    public int getClassTypeId() {
        return Exportable.SH_ITEM;
    }

    public Map<String, String> getStringRepresentation() {
        throw new UnsupportedOperationException();
    }

    /**
     * now works with BYTE array and INT only!!!
     */
    public byte[] getByteRepresentation() {

        ByteArrayOutputStream buff = new ByteArrayOutputStream();
        DataOutputStream stream = new DataOutputStream(buff);
        try {
            stream.writeUTF(name);
            stream.writeInt(type);
            switch (type) {
                case INT:
                    stream.writeInt(ivalue);
                    break;
                case BYTE:
                    stream.writeInt(bytevalue.length);
                    stream.write(bytevalue);
                    break;
                case INT_ARR:
                    stream.writeInt(int_arr_value.length);
                    for (int i = 0; i < int_arr_value.length; i++) {
                        stream.writeInt(int_arr_value[i]);
                    }
                    break;
                case BOOLEAN:
                    stream.writeBoolean(bvalue);
                    break;
                case FLOAT:
                    stream.writeFloat(fvalue);
                    break;
                case LONG:
                    stream.writeLong(lvalue);
                    break;
                case STRING:
                    stream.writeUTF(svalue);
                    break;
            }

        } catch (IOException e) {
            return null;
        }

        return buff.toByteArray();
    }

    public ShItem(DataInputStream stream) throws IOException {
        name = stream.readUTF();
        type = stream.readInt();
        switch (type) {
            case INT:
                ivalue = stream.readInt();
                break;
            case BYTE:
                int length = stream.readInt();
                byte[] arr = new byte[length];
                stream.read(arr);
                bytevalue = arr;
                break;
            case INT_ARR:
                int length2 = stream.readInt();
                int[] arr2 = new int[length2];
                for (int i = 0; i < length2; i++) {
                    arr2[i] = stream.readInt();
                }
                int_arr_value = arr2;
                break;
            case BOOLEAN:
                bvalue = stream.readBoolean();
                break;
            case FLOAT:
                fvalue = stream.readFloat();
                break;
            case LONG:
                lvalue = stream.readLong();
                break;
            case STRING:
                svalue = stream.readUTF();
                break;
        }


    }
}
