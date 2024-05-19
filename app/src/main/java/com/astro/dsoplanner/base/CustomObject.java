package com.astro.dsoplanner.base;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;

import android.graphics.Canvas;
import android.graphics.Paint;

import com.astro.dsoplanner.AstroTools;
import com.astro.dsoplanner.graph.NgcFactory;


import com.astro.dsoplanner.infolist.InfoList;
import com.astro.dsoplanner.infolist.ListHolder;

import static com.astro.dsoplanner.Constants.COMMENT;
import static com.astro.dsoplanner.Constants.NAME1;
import static com.astro.dsoplanner.Constants.NAME2;
import static com.astro.dsoplanner.Constants.TYPESTR;

public class CustomObject extends ExtendedObject {//the only one of type Exportable.CustomObject

    private static final String COMMENT2 = ", comment=";
    private static final String NAME22 = ", name2=";
    private static final String NAME12 = ", name1=";
    private static final String PA2 = ", pa=";
    private static final String CUSTOM_OBJECT_TYPE_STR = "CustomObject [typeStr=";


    public static int idNum = 0;//when we make CustomObject its id number is increased by one. This is
    //necessary as id and catalog numbers are used in equals calculation


    //typeStr is necessary if type is zero
    public String typeStr;

    public String comment;
    public String name1;
    public String name2;

    /**
     * @param catalog
     * @param id
     * @param ra
     * @param dec
     * @param con
     * @param type
     * @param typeStr, use for custom types, otherwise put ""
     * @param a
     * @param b
     * @param mag
     * @param pa
     * @param name1    short name
     * @param name2    long name
     * @param comment
     */
    public CustomObject(int catalog, int id, double ra, double dec, int con, int type, String typeStr, double a,
                        double b, double mag, double pa, String name1, String name2, String comment) {
        super(ra, dec, con, type, catalog, id, a, b, mag, pa);
        this.typeStr = typeStr;
        this.name1 = name1;
        this.name2 = name2;
        this.comment = comment;
        if (catalog == AstroCatalog.CUSTOM_CATALOG)
            this.id = idNum++;//increasing static var

    }

    public CustomObject(DataInputStream stream) throws IOException {
        super(stream);
        typeStr = stream.readUTF();
        name1 = stream.readUTF();
        name2 = stream.readUTF();
        comment = stream.readUTF();
        if (catalog == AstroCatalog.CUSTOM_CATALOG)
            id = idNum++;//increasing static var

    }

    public int getClassTypeId() {
        return Exportable.CUSTOM_OBJECT;
    }

    public int getCatalogNumber() {
        return catalog;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String getTypeString() {
        String s = super.getTypeString();
        if (type == AstroObject.Custom)
            return typeStr;
        else
            return s;
    }

    @Override
    public String getLongTypeString() {
        String s = super.getLongTypeString();
        if (type == AstroObject.Custom)
            return typeStr;
        else
            return s;
    }

    @Override
    public String toString() {
        return super.toString() + CUSTOM_OBJECT_TYPE_STR + typeStr + PA2 + pa + NAME12
                + name1 + NAME22 + name2 + COMMENT2 + comment + "]";
    }

    @Override
    public HashMap<String, String> getStringRepresentation() {
        HashMap<String, String> map = super.getStringRepresentation();
        map.put(NAME1, name1);
        map.put(NAME2, name2);
        map.put(COMMENT, comment);
        map.put(TYPESTR, typeStr);


        return map;

    }

    @Override
    public byte[] getByteRepresentation() {
        byte[] buf = super.getByteRepresentation();
        byte[] str = typeStr.getBytes();
        ByteArrayOutputStream buff = new ByteArrayOutputStream(str.length + name1.getBytes().length + name2.getBytes().length + comment.getBytes().length);
        DataOutputStream stream = new DataOutputStream(buff);
        try {
            stream.writeUTF(typeStr);
            stream.writeUTF(name1);
            stream.writeUTF(name2);
            stream.writeUTF(comment);

        } catch (IOException e) {
            return null;
        }
        byte[] newBuff = buff.toByteArray();
        byte[] combBuff = new byte[buf.length + newBuff.length];
        System.arraycopy(buf, 0, combBuff, 0, buf.length);
        System.arraycopy(newBuff, buf.length - buf.length, combBuff, buf.length, combBuff.length - buf.length);

        return combBuff;
    }

    public String getShortName() {
        return name1;
    }

    public String getLongName() {
        return name2;
    }

    public String getDsoSelName() {
        return name1;
    }

    public String getComment() {
        return comment;
    }

    @Override
    public boolean hasVisibility() {
        return true;
    }

    @Override
    protected void drawPath(Canvas canvas, Paint p) {
        int cat = getCatalog();
        if (!(cat == AstroCatalog.MESSIER || cat == AstroCatalog.CALDWELL || cat == AstroCatalog.NGCIC_CATALOG || cat == AstroCatalog.NGCIC_CATALOG + NgcFactory.LAYER_OFFSET)) {
            super.drawPath(canvas, p);
            return;
        }
        Integer value = AstroTools.getContourNumber(ref);
        if (value == null) {
            super.drawPath(canvas, p);
        } else {
            //super.drawPath(canvas, p);
            InfoList list = ListHolder.getListHolder().get(InfoList.NEBULA_CONTOUR_LIST);
            ContourObject obj = (ContourObject) list.get(value);
            obj.setXY();
            obj.setDisplayXY();
            obj.draw(canvas, p);
            obj.drawCross(canvas, p, getXd(), getYd());
        }
    }

}
