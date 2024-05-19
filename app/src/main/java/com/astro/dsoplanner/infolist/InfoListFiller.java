package com.astro.dsoplanner.infolist;

import java.util.Iterator;

public interface InfoListFiller { //allows InfoLists to be filled with data, encapsulates access to data providers such as AstroCatalog databases
    Iterator getIterator();//get the objects in filler via Iterator
}
