package com.astro.dsoplanner.base;




import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 *
 * Additional fields for CustomObject
 *
 */
public class Fields{
    
    private static final String FIELDS = "Fields[";
    
    //format name:value
    Map<String,String> mapString=new TreeMap<String,String>();
    Map<String,Double> mapDouble=new TreeMap<String,Double>();
    Map<String,Integer> mapInt=new TreeMap<String,Integer>();
    Map<String,String> mapPhoto=new TreeMap<String,String>();
    Map<String,String> mapUrl=new TreeMap<String,String>();

    public static class Photo{
        String photo;
        public Photo(String s){
            photo=s;
        }
    }
    public static class Url{
        String url;
        public Url(String s){
            url=s;
        }
    }
    public String toString(){
        String s=FIELDS;
        for(Map.Entry<String, String> e:mapString.entrySet()){
            s=s+" "+e.getKey()+"="+e.getValue()+",";
        }
        for(Map.Entry<String, Double> e:mapDouble.entrySet()){
            s=s+" "+e.getKey()+"="+String.format(Locale.US,"%.6f",e.getValue())+",";
        }
        for(Map.Entry<String, Integer> e:mapInt.entrySet()){
            s=s+" "+e.getKey()+"="+e.getValue()+",";
        }
        for(Map.Entry<String, String> e:mapPhoto.entrySet()){
            s=s+" "+e.getKey()+"="+e.getValue()+",";
        }
        for(Map.Entry<String, String> e:mapUrl.entrySet()){
            s=s+" "+e.getKey()+"="+e.getValue()+",";
        }
        s=s+"]";
        return s;
    }
    public Fields(){}
    public Fields(DataInputStream stream) throws IOException{

        byte size=stream.readByte();
        for(int i=0;i<size;i++){
            String field=stream.readUTF();
            String value=stream.readUTF();
            mapString.put(field, value);
        }
        size=stream.readByte();
        for(int i=0;i<size;i++){
            String field=stream.readUTF();
            Double value=stream.readDouble();
            mapDouble.put(field, value);
        }
        size=stream.readByte();
        for(int i=0;i<size;i++){
            String field=stream.readUTF();
            Integer value=stream.readInt();
            mapInt.put(field, value);
        }
        size=stream.readByte();
        for(int i=0;i<size;i++){
            String field=stream.readUTF();
            String value=stream.readUTF();
            mapPhoto.put(field, value);
        }
        size=stream.readByte();
        for(int i=0;i<size;i++){
            String field=stream.readUTF();
            String value=stream.readUTF();
            mapUrl.put(field, value);
        }
    }
    public void put(String field, String value){
        mapString.put(field, value);

    }
    public void put(String field, Double value){
        mapDouble.put(field, value);
    }
    public void put(String field, Integer value){
        mapInt.put(field, value);

    }
    public void put(String field,Photo value){
        mapPhoto.put(field, value.photo);
    }
    public void put(String field,Url value){
        mapUrl.put(field, value.url);
    }
    public boolean isEmpty(){
        return(mapString.isEmpty()&&mapDouble.isEmpty()&&mapInt.isEmpty()&&mapPhoto.isEmpty()&&mapUrl.isEmpty());
    }
    /**
     *
     * @param field
     * @return null if there is no such field
     */
    public String get(String field){
        String s=mapString.get(field);
        if(s!=null)return s;

        s=mapPhoto.get(field);
        if(s!=null)return s;

        s=mapUrl.get(field);
        if(s!=null)return s;

        Double d=mapDouble.get(field);
        if(d!=null)return String.format(Locale.US,"%.6f",d);
        Integer i=mapInt.get(field);
        if(i!=null)return i.toString();

        return null;
    }
    /**
     *
     * @param field
     * @return the numeric value of the field or null if no such field
     */
    public Double getNum(String field){
        Double d=mapDouble.get(field);
        if(d!=null)return d;
        Integer i=mapInt.get(field);
        if(i!=null){
            return new Double(i);
        }
        return null;
    }
    public Map<String,String> getFieldMap(){
        Map<String,String> map=new TreeMap<String,String>();
        map.putAll(mapString);
        map.putAll(mapPhoto);
        map.putAll(mapUrl);
        for(Map.Entry<String,Double>e:mapDouble.entrySet()){
            map.put(e.getKey(), getFormattedDouble(e.getValue()));//String.format(Locale.US,"%.6f",e.getValue()));
        }
        for(Map.Entry<String,Integer>e:mapInt.entrySet()){
            map.put(e.getKey(), e.getValue().toString());
        }
        return map;

    }
    public Map<String,String> getStringMap(){
        return mapString;
    }
    public Map<String,Double> getDoubleMap(){
        return mapDouble;
    }
    public Map<String,Integer> getIntMap(){
        return mapInt;
    }
    public Map<String,String> getPhotoMap(){
        return mapPhoto;
    }
    public Map<String,String> getUrlMap(){
        return mapUrl;
    }
    public HashMap<String,String> getStringRepresentation(){
        HashMap<String,String> map=new HashMap<String,String>();
        //Set<Entry<String, String>> setS=mapString.entrySet();

        for(Map.Entry<String,String>e:mapString.entrySet()){
            map.put(e.getKey(), e.getValue());
        }
        for(Map.Entry<String,Double>e:mapDouble.entrySet()){
            map.put(e.getKey(), String.format(Locale.US,"%.6f",e.getValue()));
        }
        for(Map.Entry<String,Integer>e:mapInt.entrySet()){
            map.put(e.getKey(), e.getValue().toString());
        }
        for(Map.Entry<String,String>e:mapPhoto.entrySet()){
            map.put(e.getKey(), e.getValue());
        }
        for(Map.Entry<String,String>e:mapUrl.entrySet()){
            map.put(e.getKey(), e.getValue());
        }
        return map;
    }

    private String getFormattedDouble(double d){
        String s=String.format(Locale.US,"%.6f",d);
        int dotpos=s.indexOf('.', 0);
        if(dotpos==-1)
            return s;
        if(s.length()<=dotpos+2)
            return s;
        String s1=s.substring(0, dotpos+2);
        for(int i=dotpos+2;i<s.length();i++){
            char c=s.charAt(i);
            if(c=='0')
                return s1;
            else
                s1=s1+c;
        }
        return s1;

    }
    public byte[] getByteRepresentation(){

        ByteArrayOutputStream buff=new ByteArrayOutputStream();
        DataOutputStream stream=new DataOutputStream(buff);
        try{
            Set<Map.Entry<String, String>> setS=mapString.entrySet();
            stream.writeByte(setS.size());

            for(Map.Entry<String,String>e:setS){
                stream.writeUTF(e.getKey());
                stream.writeUTF(e.getValue());
            }

            Set<Map.Entry<String, Double>> setD=mapDouble.entrySet();
            stream.writeByte(setD.size());
            for(Map.Entry<String,Double>e:setD){
                stream.writeUTF(e.getKey());
                stream.writeDouble(e.getValue());
            }
            Set<Map.Entry<String, Integer>> setI=mapInt.entrySet();
            stream.writeByte(setI.size());
            for(Map.Entry<String,Integer>e:setI){
                stream.writeUTF(e.getKey());
                stream.writeInt(e.getValue());
            }

            setS=mapPhoto.entrySet();
            stream.writeByte(setS.size());
            for(Map.Entry<String,String>e:setS){
                stream.writeUTF(e.getKey());
                stream.writeUTF(e.getValue());
            }

            setS=mapUrl.entrySet();
            stream.writeByte(setS.size());
            for(Map.Entry<String,String>e:setS){
                stream.writeUTF(e.getKey());
                stream.writeUTF(e.getValue());
            }

        }
        catch(IOException e){
            return null;
        }


        return buff.toByteArray();
    }


}
