package generator.addresses;

import generator.Type;

import java.util.ArrayList;
import java.util.List;

public class Expression {
    private final Address addr;
    private final Type type;
    private final List<Integer> nextlist;
    private final List<Integer> truelist;
    private final List<Integer> falselist;

    public Expression(Address addr){
        this.addr = addr;
        if(addr == null)
            this.type = Type.VOID;
        else
            this.type = addr.type();
        this.nextlist = new ArrayList<>();
        this.truelist = new ArrayList<>();
        this.falselist = new ArrayList<>();
    }

    public Expression(boolean hasError){
        this.addr = null;
        if(hasError)
            this.type = Type.ERROR;
        else
            this.type = Type.VOID;
        this.nextlist = new ArrayList<>();
        this.truelist = new ArrayList<>();
        this.falselist = new ArrayList<>();
    }

    public String addr(){
        if(addr!=null)
            return addr.value();
        else
            return "none";
    }

    public Type type(){
        return this.type;
    }

    public List<Integer> getNextlist() {
        return nextlist;
    }

    public List<Integer> getTruelist() {
        return truelist;
    }

    public List<Integer> getFalselist() {
        return falselist;
    }

    public void addToNextList(List<Integer> list){
        nextlist.addAll(list);
    }

    public void addToTrueList(List<Integer> list){
        truelist.addAll(list);
    }

    public void addToFalseList(List<Integer> list){
        falselist.addAll(list);
    }

    public void addToNextList(int[] list){
        for (Integer inst:list) {
            nextlist.add(inst);
        }
    }

    public void addToTrueList(int[] list){
        for (Integer inst:list) {
            truelist.add(inst);
        }
    }

    public void addToFalseList(int[] list){
        for (Integer inst:list) {
            falselist.add(inst);
        }
    }
}
