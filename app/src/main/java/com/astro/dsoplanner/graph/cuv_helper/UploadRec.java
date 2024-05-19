package com.astro.dsoplanner.graph.cuv_helper;

public class UploadRec{

    public double azCenter;
    public double altCenter;
    public double raCenter;
    public double decCenter;
    public double FOV;
    public double height;
    public double width;
    public boolean raiseNewPointFlag;
    public boolean newFOV;
    public boolean clearcache;
    @Override
    public String toString() {
        return "UploadRec [azCenter=" + azCenter + ", altCenter="
                + altCenter + ", raCenter=" + raCenter + ", decCenter="
                + decCenter + ", FOV=" + FOV + ", height=" + height
                + ", width=" + width + ", raiseNewPointFlag="
                + raiseNewPointFlag + ", newFOV=" + newFOV
                + ", clearcache=" + clearcache + "]";
    }
}
