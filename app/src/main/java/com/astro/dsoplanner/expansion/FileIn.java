package com.astro.dsoplanner.expansion;

import java.io.File;

import com.astro.dsoplanner.Global;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;

import android.util.Log;

import com.astro.dsoplanner.MarkTAG;


import com.astro.dsoplanner.AstroTools;

public class FileIn {

    private static final String UNEXPECTED_END_OF_FILE = "Unexpected end of file";

    private static final String TAG = FileIn.class.getSimpleName();
    RandomAccessFile in;
    String name;
    long size;
    String dir;

    public FileIn(RandomAccessFile in, String name, long size, String dir) {
        super();
        this.in = in;
        this.name = name;
        this.size = size;
        this.dir = dir;
    }

    public void read() throws IOException {
        OutputStream out = null;
        try {
            if (dir == null) {
                long pos = in.getFilePointer();
                in.seek(pos + size);
                return;
            }
            File f = null;
            f = new File(dir, name);
            out = new FileOutputStream(f);

            byte[] buf = new byte[2048];
            long fullblocks = size / buf.length;
            long rembytes = size - buf.length * fullblocks;
            for (long i = 0; i < fullblocks; i++) {
                int b = in.read(buf);
                if (b != buf.length)
                    throw new IOException(UNEXPECTED_END_OF_FILE);
                out.write(buf);
            }
            int rem = in.read(buf, 0, (int) rembytes);
            if (rem != (int) rembytes)
                throw new IOException(UNEXPECTED_END_OF_FILE);
            out.write(buf, 0, rem);
        } finally {
            if (out != null)
                out.close();
        }

    }
}
