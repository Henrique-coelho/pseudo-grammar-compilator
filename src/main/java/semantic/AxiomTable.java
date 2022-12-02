package semantic;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AxiomTable {
    protected Map<String, Axiom> table;
    protected AxiomTable previousTable;

    public AxiomTable(){
        this.table = new HashMap<String, Axiom>();
        this.previousTable = null;
    }

    public AxiomTable(AxiomTable previousTable){
        this.table = new HashMap<String, Axiom>();
        this.previousTable = previousTable;
    }

    public void insert(String name,Axiom axiom){
        this.table.put(name, axiom);
    }

    public Axiom get(String name){
        if(this.table.containsKey(name))
            return table.get(name);
        if(this.previousTable != null)
            return this.previousTable.get(name);
        return null;
    }
    public boolean contains(String axiom){
        if(this.table.containsKey(axiom))
            return true;
        if(this.previousTable != null)
            this.previousTable.contains(axiom);
        return false;
    }

    public String getType(String axiom){
        if(this.table.containsKey(axiom))
            return table.get(axiom).getType();
        if(this.previousTable != null)
            return this.previousTable.getType(axiom);
        return null;
    }

    public boolean closeTable(){
        if(this.previousTable != null){
            this.table = this.previousTable.table;
            this.previousTable = this.previousTable.previousTable;
            return true;
        }
        return false;
    }

    public void openTable(){
        this.previousTable = this;
        this.table = new HashMap<String, Axiom>();
    }
}
