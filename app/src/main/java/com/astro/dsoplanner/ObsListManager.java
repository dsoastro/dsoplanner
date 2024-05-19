package com.astro.dsoplanner;

import com.astro.dsoplanner.base.AstroObject;
import com.astro.dsoplanner.util.Holder2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ObsListManager {
    private class Item extends Holder2<AstroObject, Boolean> {
        public Item(AstroObject obj, Boolean b) {
            super(obj, b);
        }
    }

    private List<Item> list = new ArrayList<Item>();

    public int getCount() {
        return list.size();
    }

    public void add(AstroObject obj) {
        for (Item h : list) {
            if (h.x.equals(obj)) //do not add objects already in the List
                return;
        }
        list.add(new Item(obj, false));
    }

    public void add(Collection<AstroObject> collection) {

        for (Item h : list) {
            if (collection.contains(h.x))
                collection.remove(h.x);
        }
        for (AstroObject obj : collection) {
            list.add(new Item(obj, false));
        }

    }

    public void add(AstroObject obj, boolean onoff) {
        for (Item h : list) {
            if (h.x.equals(obj)) //do not add objects already in the List
                return;
        }
        list.add(new Item(obj, onoff));
    }

    public AstroObject get(int position) {
        return list.get(position).x;
    }

    public boolean isOn(int position) {
        return list.get(position).y;
    }

    public void setStatus(int position, boolean onoff) {
        Item h = list.get(position);
        h.y = onoff;
    }

    public List<AstroObject> getAll() {
        List<AstroObject> tmpList = new ArrayList<AstroObject>();
        for (Item h : list)
            tmpList.add(h.x);
        return tmpList;
    }

    public void remove(int position) {
        list.remove(position);
    }

    public void removeAll() {
        list = new ArrayList<Item>();
    }
}
