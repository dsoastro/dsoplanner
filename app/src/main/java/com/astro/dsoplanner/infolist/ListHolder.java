package com.astro.dsoplanner.infolist;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class ListHolder implements InfoListHolder {

    private static final String LISTS_IN_LISTHOLDER = "Lists in listholder:";

    private static InfoListHolder lh = new ListHolder();
    private List<Item> listOfLists = new ArrayList<Item>();
    private Set<Integer> idSet = new HashSet<Integer>();

    class Item {
        int id;

        InfoList list;

        public Item(int id, InfoList list) {
            this.id = id;
            this.list = list;
        }
    }

    @Override
    public String toString() {
        String s = LISTS_IN_LISTHOLDER + idSet;
        return s;
    }

    private ListHolder() {
    }

    public static InfoListHolder getListHolder() {
        return lh;
    }

    public void addNewList(int id, InfoList list) {
        if (idSet.contains(id))
            return;
        idSet.add(id);
        listOfLists.add(new Item(id, list));
    }

    public Set<Integer> getIdSet() {
        return idSet;
    }

    public int getCount() {
        return listOfLists.size();
    }

    public InfoList get(int id) {
        for (Item i : listOfLists) {
            if (i.id == id)
                return i.list;
        }
        return null;
    }

    public void remove(int id) {

        Iterator<Item> it = listOfLists.iterator();
        while (it.hasNext()) {
            Item item = it.next();
            if (item.id == id) {
                it.remove();
                idSet.remove(id);
                return;
            }
        }
    }

    public void removeAll() {
        listOfLists = new ArrayList<Item>();
        idSet = new HashSet<Integer>();
    }

}
