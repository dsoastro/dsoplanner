package com.astro.dsoplanner.scopedrivers;

public interface ComInterface {
    public void open();
    public void stop();
    public void read();
    public void send(double ra,double dec);
    public boolean isConnected();
    public void cancelContinous();
    public void readContinous(final int period);
}
