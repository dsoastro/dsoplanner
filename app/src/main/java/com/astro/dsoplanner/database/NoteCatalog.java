package com.astro.dsoplanner.database;

import com.astro.dsoplanner.ErrorHandler;
import com.astro.dsoplanner.NoteRecord;
import com.astro.dsoplanner.base.AstroObject;

import java.util.List;

interface NoteCatalog{
    final int MAIN_CATALOG=1;
    public List<NoteRecord> search(AstroObject obj);
    public AstroObject getObject(NoteRecord n, ErrorHandler eh);
    public void open(ErrorHandler eh);
    public void close();
    public long add(AstroObject obj,String note,long date,String path,String name);
    public void edit (NoteRecord rec);
    public void remove(NoteRecord rec);
}
