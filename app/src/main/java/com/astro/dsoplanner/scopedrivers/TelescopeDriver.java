package com.astro.dsoplanner.scopedrivers;

import java.io.OutputStream;

import com.astro.dsoplanner.AstroTools;
import com.astro.dsoplanner.R;
import com.astro.dsoplanner.Global;

public interface TelescopeDriver {
    final static int EPOCH_CURRENT = 1;
    final static int EPOCH_2000 = 2;
    public final static int Celestron = 1;
    public final static int Meade = 2;
    public final static int IOptron = 3;
    public final static int DSC = 4;

    public void addMessage(byte[] b, int length);

    public void setPosition(double ra, double dec);

    public void setOutputStream(OutputStream out);

    public void slewToPosition();

    public void cancel();

    public int getEpoch();

    public int getDriver();

    public AstroTools.RaDecRec getPosition();
}
