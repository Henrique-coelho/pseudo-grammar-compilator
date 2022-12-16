package generator;

import generator.addresses.NameAddress;

import java.util.ArrayList;
import java.util.List;

public class CurrentSymbolTable {
    protected List<NameAddress> table;
    private static int offset = 0;
    public CurrentSymbolTable(){
        this.table = new ArrayList<NameAddress>();
    }

    public boolean put(NameAddress addr){
        if(this.contains(addr.toString()))
            return false;
        else
            this.table.add(addr);
        offset += addr.width();
        return true;
    }
    public NameAddress get(String id) {
        if(contains(id))
            for (NameAddress addr:table) {
                if(addr.value().equals(id))
                    return addr;
            }
        return null;
    }
    public boolean contains(String id){
        for (NameAddress addr:table) {
            if(addr.value().equals(id)){
                return true;
            }
        }
        return false;
    }
}
