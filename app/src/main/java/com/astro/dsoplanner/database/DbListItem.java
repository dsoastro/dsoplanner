package com.astro.dsoplanner.database;

import com.astro.dsoplanner.base.Exportable;


import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

public class DbListItem implements Exportable {

    private static final String DB_FILE_NAME = ", dbFileName=";
    private static final String DB_NAME = ", dbName=";
    private static final String CAT2 = ", cat=";
    private static final String DB_LIST_ITEM_MENU_ID = "DbListItem [menuId=";


    public int menuId;
    public int cat;//AstroCatalog number
    public String dbName;
    public String dbFileName;
    public FieldTypes ftypes;

    public DbListItem(int menuId, int cat, String dbName, String dbFileName, FieldTypes ftypes) {

        this.menuId = menuId;
        this.cat = cat;
        this.dbName = dbName;
        this.dbFileName = dbFileName;
        this.ftypes = ftypes;
    }

    public DbListItem(DataInputStream stream) throws IOException {
        menuId = stream.readShort();
        cat = stream.readShort();
        dbName = stream.readUTF();
        dbFileName = stream.readUTF();
        ftypes = new FieldTypes(stream);
    }

    public int getClassTypeId() {
        return Exportable.DB_LIST_ITEM;
    }

    public Map<String, String> getStringRepresentation() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return DB_LIST_ITEM_MENU_ID + menuId + CAT2 + cat + DB_NAME
                + dbName + DB_FILE_NAME + dbFileName + ftypes + "]";
    }

    public byte[] getByteRepresentation() {

        ByteArrayOutputStream buff = new ByteArrayOutputStream();
        DataOutputStream stream = new DataOutputStream(buff);
        try {
            stream.writeShort(menuId);
            stream.writeShort(cat);
            stream.writeUTF(dbName);
            stream.writeUTF(dbFileName);
            stream.write(ftypes.getByteRepresentation());
        } catch (IOException e) {
            return null;
        }

        return buff.toByteArray();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof DbListItem)
            if (((DbListItem) o).menuId == this.menuId)
                return true;
        return false;
    }

    @Override
    public int hashCode() {
        return menuId;
    }


    public static class FieldTypes {

        private static final String FIELD_TYPES = "FieldTypes[";
        private static final String WEB_LINK = "Web link";
        private static final String IMAGE_PATH = "Image path";
        private static final String INTEGER_NUMBER = "Integer number";
        private static final String DOUBLE_NUMBER = "Double number";
        private static final String TEXT_STRING = "Text string";

        public enum TYPE {
            NONE(0, ""),
            STRING(1, TEXT_STRING),
            DOUBLE(2, DOUBLE_NUMBER),
            INT(3, INTEGER_NUMBER),
            PHOTO(4, IMAGE_PATH),
            URL(5, WEB_LINK);

            public int id;
            public String name;

            TYPE(int i, String s) {
                id = i;
                name = s;
            }

            public static TYPE valueOf(int i) {
                for (TYPE t : TYPE.values()) {
                    if (t.id == i) {
                        return t;
                    }
                }
                return TYPE.NONE;
            }

            public static TYPE fromString(String s) {
                for (TYPE t : TYPE.values()) {
                    if (t.name.contentEquals(s)) {
                        return t;
                    }
                }
                return TYPE.NONE;
            }

        }

        private Map<String, TYPE> nameTypeMap = new TreeMap<>();//need an order for custom database

        public FieldTypes() {
        }

        public FieldTypes(DataInputStream stream) throws IOException {
            byte size = stream.readByte();
            for (int i = 0; i < size; i++) {
                String field = stream.readUTF();
                TYPE type = TYPE.valueOf(stream.readInt());
                nameTypeMap.put(field, type);
            }
        }

        public void put(String field, TYPE type) {
            nameTypeMap.put(field, type);
        }

        public boolean isEmpty() {
            return nameTypeMap.isEmpty();
        }

        public Map<String, TYPE> getNameTypeMap() {
            return nameTypeMap;
        }

        public Set<String> getFields() {
            return nameTypeMap.keySet();
        }

        /**
         * @return a set with numeric fields (Integer and Double)
         */
        public Set<String> getNumericFields() {
            Set<String> set = new HashSet<String>();
            for (Map.Entry<String, TYPE> e : nameTypeMap.entrySet()) {
                if (e.getValue() == TYPE.INT || e.getValue() == TYPE.DOUBLE)
                    set.add(e.getKey());
            }
            return set;
        }

        /**
         * @return a set with String fields
         */
        public Set<String> getStringFields() {
            Set<String> set = new HashSet<String>();
            for (Map.Entry<String, TYPE> e : nameTypeMap.entrySet()) {
                if (e.getValue() == TYPE.STRING || e.getValue() == TYPE.PHOTO || e.getValue() == TYPE.URL)
                    set.add(e.getKey());
            }
            return set;
        }

        public TYPE getType(String field) {
            TYPE type = nameTypeMap.get(field);
            if (type != null)
                return type;
            else
                return TYPE.NONE;
        }

        @Override
        public String toString() {
            String s = FIELD_TYPES;
            for (Map.Entry<String, TYPE> e : nameTypeMap.entrySet()) {
                s = s + e.getKey() + "=" + e.getValue() + ",";
            }
            s = s + "]";
            return s;
        }

        public byte[] getByteRepresentation() {

            ByteArrayOutputStream buff = new ByteArrayOutputStream();
            DataOutputStream stream = new DataOutputStream(buff);
            try {
                Set<Entry<String, TYPE>> setS = nameTypeMap.entrySet();
                stream.writeByte(setS.size());

                for (Entry<String, TYPE> e : setS) {
                    stream.writeUTF(e.getKey());
                    stream.writeInt(e.getValue().id);
                }
            } catch (Exception e) {
                return null;
            }
            return buff.toByteArray();
        }

    }
}
