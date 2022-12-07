package semantic;

import java.util.HashMap;
import java.util.Map;

public class TypeTable {
    protected Map<String, Type> table;

    public TypeTable(){
        this.table = new HashMap<String, Type>();
    }
    public TypeTable(TypeTable previousTable){
        this.table = new HashMap<String, Type>();
    }
    public boolean insert(String id,Type type){
        if(this.contains(id))
            return false;
        else
            this.table.put(id, type);
        return true;
    }
    public Type get(String id) {
        if(this.table.containsKey(id))
            return table.get(id);
        else
            return Type.ERROR;
    }
    public boolean contains(String id){
        return this.table.containsKey(id);
    }
}
