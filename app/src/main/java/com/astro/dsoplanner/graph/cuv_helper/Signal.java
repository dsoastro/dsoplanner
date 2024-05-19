package com.astro.dsoplanner.graph.cuv_helper;

public class Signal{
    private UploadRec u=null;
    public synchronized UploadRec getRec(){
        return u;
    }
    public synchronized void setRec(UploadRec u){
        this.u=u;
    }
}