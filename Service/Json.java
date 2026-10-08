package Service;

import java.util.*;

/** Small JSON writer for controlled response DTOs; request bodies use URL-encoded forms. */
public final class Json {
    private Json() {}
    public static String write(Object value) {
        if(value==null)return "null";
        if(value instanceof String s) {
            StringBuilder text=new StringBuilder("\"");
            for(char c:s.toCharArray())switch(c){case '"'->text.append("\\\"");case '\\'->text.append("\\\\");case '\n'->text.append("\\n");case '\r'->text.append("\\r");case '\t'->text.append("\\t");default->{if(c<32)text.append(String.format("\\u%04x",(int)c));else text.append(c);}}
            return text.append('"').toString();
        }
        if(value instanceof Number||value instanceof Boolean)return value.toString();
        if(value instanceof Map<?,?> map){List<String> entries=new ArrayList<>();map.forEach((k,v)->entries.add(write(k.toString())+":"+write(v)));return "{"+String.join(",",entries)+"}";}
        if(value instanceof Iterable<?> list){List<String> items=new ArrayList<>();for(Object item:list)items.add(write(item));return "["+String.join(",",items)+"]";}
        throw new IllegalArgumentException("Unsupported JSON value: "+value.getClass());
    }
    public static Map<String,Object> object(Object... pairs) {
        Map<String,Object> result=new LinkedHashMap<>();for(int i=0;i<pairs.length;i+=2)result.put((String)pairs[i],pairs[i+1]);return result;
    }
}
