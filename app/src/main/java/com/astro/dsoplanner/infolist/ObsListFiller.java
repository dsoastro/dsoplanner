package com.astro.dsoplanner.infolist;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.astro.dsoplanner.base.AstroObject;
import com.astro.dsoplanner.infolist.ObsInfoListImpl.Item;


public class ObsListFiller implements InfoListFiller {
    private Collection<AstroObject> collection;

    public ObsListFiller(Collection<AstroObject> collection) {
        this.collection = collection;
    }

    public ObsListFiller(Collection<Item> collection, boolean dummy) {
        List<AstroObject> list = new ArrayList<AstroObject>();
        for (Item item : collection) {
            list.add(item.x);
        }
        this.collection = list;
    }

    public void update() {

    }

    public Iterator getIterator() {
        return collection.iterator();
    }
}
