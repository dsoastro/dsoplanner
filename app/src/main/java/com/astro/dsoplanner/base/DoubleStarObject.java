package com.astro.dsoplanner.base;

import com.astro.dsoplanner.AstroTools;


import java.io.DataInputStream;
import java.io.IOException;
import java.util.Locale;

public class DoubleStarObject extends CustomObjectLarge {

    private static final String HR_0_9 = "HR[0-9]+";
    private static final String SEPARATION = "separation";
    private static final String COMPONENTS = "components";


    public DoubleStarObject(int catalog, int id, double ra, double dec, int con, int type, String typeStr, double a,
                            double b, double mag, double pa, String name1, String name2, String comment, Fields fields) {
        super(catalog, id, ra, dec, con, type, typeStr, a, b, mag, pa, name1, name2, comment, fields);

    }

    public DoubleStarObject(DataInputStream stream) throws IOException {
        super(stream);

    }

    @Override
    protected String getLabelName() {
        String comp = fields.get(COMPONENTS);
        if (comp == null) comp = "";
        comp = comp.replace(" ", "");

        Double sep = fields.getNum(SEPARATION);
        String sepStr = "";
        double sepd = 0;
        if (sep != null) {
            sepStr = String.format(Locale.US, "%.1f", sep) + "\"";
            sepd = sep;
        }
        if (sepd != 0) {
            String add = comp + " " + sepStr;
            add = add.trim();
            return getShortName() + " " + add;
        } else
            return getShortName();


    }

    @Override
    public String getShortName() {
        if (getCatalog() == AstroCatalog.HAAS) {
            if (name2.matches(HR_0_9)) {
                HrStar star = AstroTools.getHrStar(name2);
                if (star != null) {
                    String name = star.getBayerNameWithoutNum();

                    if (!"".equals(name)) {
                        name = name + " " + getConString();
                        return name;
                    } else {
                        if (star.getFlamsteed() != 0) {
                            name = star.getFlamsteedName();
                            return name;
                        }
                    }
                }

            }
            return name1;
        } else
            return super.getShortName();
    }

    @Override
    public String getLongName() {
        if (getCatalog() == AstroCatalog.HAAS)
            return getShortName();
        else
            return super.getLongName();
    }

    @Override
    public String getDsoSelName() {
        if (getCatalog() == AstroCatalog.HAAS)
            return getShortName();
        else
            return super.getDsoSelName();
    }

    public String getDsoSelNameSecondLine() {
        Double sep = fields.getNum(SEPARATION);
        String sepStr = "--";
        if (sep != null) {
            sepStr = String.format(Locale.US, "%.1f", sep) + "\"";
        }


        Double mag2 = fields.getNum("mag2");
        String mag2Str = "--";
        if (mag2 != null) {
            mag2Str = String.format(Locale.US, "%.1f", mag2) + "m";
        }
        String comp = fields.get(COMPONENTS);
        if (comp == null) comp = "";
        comp = comp.replace(" ", "");

        double mag = getMag();
        String magStr = (Double.isNaN(mag) ? "--" : String.format(Locale.US, "%.1f", mag) + "m");
        return (comp + " " + magStr + " " + mag2Str + " " + sepStr).trim();

    }

    public String getCrossdbSearchName() {
        if (getCatalog() == AstroCatalog.HAAS)
            return name1;
        else if (getCatalog() == AstroCatalog.WDS)
            return "WDS" + name1;
        else
            return name1;
    }

    @Override
    public int getClassTypeId() {
        return Exportable.DS_OBJECT;
    }
}
