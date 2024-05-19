package com.astro.dsoplanner.infolist;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.astro.dsoplanner.base.Exportable;

public class InfoListSaverImp implements InfoListSaver {

    private static final String TAG = InfoListSaverImp.class.getSimpleName();
    private DataOutputStream out;

    public InfoListSaverImp(OutputStream out) {
        this.out = new DataOutputStream(out);
    }

    public void open() {
    }

    public void addName(String name) throws IOException {
        out.writeUTF(name);
    }

    public void addObject(Exportable obj) throws IOException {
        byte[] buff = obj.getByteRepresentation();
        if (buff == null)
            return;
        out.writeInt(obj.getClassTypeId());
        out.writeInt(buff.length);
        out.write(buff);

    }

    public void close() throws IOException {
        out.close();
    }
}






