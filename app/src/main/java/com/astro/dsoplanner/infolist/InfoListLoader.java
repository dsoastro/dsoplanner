package com.astro.dsoplanner.infolist;

import com.astro.dsoplanner.ErrorHandler;

import java.io.IOException;

public interface InfoListLoader {
    void open();

    String getName() throws IOException;

    Object next(ErrorHandler.ErrorRec e) throws IOException;

    void close() throws IOException;
}
