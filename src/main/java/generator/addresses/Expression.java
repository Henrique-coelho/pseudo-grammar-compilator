package generator.addresses;

import generator.Type;

import java.util.ArrayList;
import java.util.List;

public class Expression {
    private Address addr;
    private Type type;
    private List<Integer> nextlist;
    private List<Integer> truelist;
    private List<Integer> falselist;

    public Expression(Address addr){
        this.addr = addr;
        if(addr == null)
            this.type = Type.VOID;
        else
            this.type = addr.type();
        this.nextlist = new ArrayList<Integer>();
        this.truelist = new ArrayList<Integer>();
        this.falselist = new ArrayList<Integer>();
    }

    public Expression(boolean hasError){
        this.addr = null;
        if(hasError)
            this.type = Type.ERROR;
        else
            this.type = Type.VOID;
        this.nextlist = new ArrayList<Integer>();
        this.truelist = new ArrayList<Integer>();
        this.falselist = new ArrayList<Integer>();
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
        for (Integer inst:list) {
            nextlist.add(inst);
        }
    }

    public void addToTrueList(List<Integer> list){
        for (Integer inst:list) {
            truelist.add(inst);
        }
    }

    public void addToFalseList(List<Integer> list){
        for (Integer inst:list) {
            falselist.add(inst);
        }
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
