package com.astro.dsoplanner.startools;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class TychoObject {

    private static final String MAG2 = ", mag=";
    private static final String DEC2 = ", dec=";
    private static final String RA2 = ", ra=";
    private static final String TYC32 = ", tyc3=";
    private static final String TYC22 = ", tyc2=";
    private static final String TYCHO_OBJECT_TYC1 = "TychoObject [tyc1=";


    public int tyc1;
    public int tyc2;
    public int tyc3;
    public double ra;
    public double dec;
    public double mag;
    public int tyc123index;


    @Override
    public String toString() {
        return TYCHO_OBJECT_TYC1 + tyc1 + TYC22 + tyc2 + TYC32
                + tyc3 + RA2 + ra + DEC2 + dec + MAG2 + mag
                + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + tyc1;
        result = prime * result + tyc2;
        result = prime * result + tyc3;
        return result;
    }

    @Override
    public boolean equals(Object obj) {

        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TychoObject other = (TychoObject) obj;
        if (tyc1 != other.tyc1)
            return false;
        if (tyc2 != other.tyc2)
            return false;
        if (tyc3 != other.tyc3)
            return false;
        return true;
    }

    public TychoObject(long index, byte[] data) throws IOException {
        BitTools.RaDec radec = BitTools.getRaDecFromTychoIndex(index);
        ra = radec.ra;
        dec = radec.dec;
        ByteArrayInputStream stream = new ByteArrayInputStream(data);
        DataInputStream in = new DataInputStream(stream);
        int tyc = in.readInt();
        tyc123index = tyc;
        BitTools.TycData tycdata = BitTools.convertIntToTyc(tyc);
        tyc1 = tycdata.tyc1;
        tyc2 = tycdata.tyc2;
        tyc3 = tycdata.tyc3;
        byte m = in.readByte();
        if (m > 0)
            mag = m / 10f;
        else
            mag = (m + 255) / 10f;

        in.close();
    }

    public TychoObject(double ra, double dec, byte[] data) throws IOException {

        this.ra = ra;
        this.dec = dec;
        ByteArrayInputStream stream = new ByteArrayInputStream(data);
        DataInputStream in = new DataInputStream(stream);
        int tyc = in.readInt();
        tyc123index = tyc;
        BitTools.TycData tycdata = BitTools.convertIntToTyc(tyc);
        tyc1 = tycdata.tyc1;
        tyc2 = tycdata.tyc2;
        tyc3 = tycdata.tyc3;
        byte m = in.readByte();
        if (m > 0)
            mag = m / 10f;
        else
            mag = (m + 255) / 10f;

        in.close();
    }

    public byte[] getData() throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(stream);

        out.writeInt(BitTools.convertTycToInt(tyc1, tyc2, tyc3));
        byte m = (byte) (mag * 10);
        out.writeByte(m);
        out.close();
        return stream.toByteArray();
    }

    public long getIndex() {
        return BitTools.getTychoIndex(ra, dec);
    }
}
