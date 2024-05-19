package com.astro.dsoplanner.infolist;

import com.astro.dsoplanner.base.Exportable;

import java.io.IOException;

public interface InfoListSaver {
    void open();

    void addName(String name) throws IOException;//add Name first!!!

    void addObject(Exportable obj) throws IOException;

    void close() throws IOException;
}
