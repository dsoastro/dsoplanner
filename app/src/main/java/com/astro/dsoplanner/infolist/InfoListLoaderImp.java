package com.astro.dsoplanner.infolist;

import com.astro.dsoplanner.ErrorHandler;
import com.astro.dsoplanner.ObjectInflater;
import com.astro.dsoplanner.infolist.InfoListLoader;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public class InfoListLoaderImp implements InfoListLoader { //Object loader for any type of info lists
    private DataInputStream in;

    public InfoListLoaderImp(InputStream in) {
        this.in = new DataInputStream(in);
    }

    public void open() {
    }

    public String getName() throws IOException {
        String name = in.readUTF();
        return name;
    }

    public Object next(ErrorHandler.ErrorRec erec) throws IOException {
        int classType = in.readInt();
        int len = in.readInt();
        byte[] buff = new byte[len];
        in.read(buff);
        Object obj = null;
        try {
            obj = ObjectInflater.getInflater().inflate(classType,
                    new DataInputStream(new ByteArrayInputStream(buff)));
        } catch (Exception e) {
            erec.type = ErrorHandler.DATA_CORRUPTED;
            return null;
        }
        if (obj == null)
            erec.type = ErrorHandler.DATA_CORRUPTED;
        return obj;

    }

    public void close() throws IOException {
        in.close();
    }
}
