package generator;

import generator.codeline.IncompleteLine;
import generator.codeline.Line;
import generator.codeline.LineModel;

import java.util.*;

public class Code {
    Map<Integer, LineModel> code;
    int nextAddr;
    int offset;
    public Code(){
        this.code = new HashMap<>();
        this.nextAddr = 100;
        this.offset = 0;
    }

    public Code(int startAddr){
        this.code = new HashMap<>();
        this.nextAddr = startAddr;
        this.offset = 0;
    }

    public void emit(String command){
        this.code.put(this.nextAddr,new Line(command));
        this.nextAddr++;
    }

    public void emit(String command, String missing) throws Exception{
        System.out.println("Emmiting: "+command);
        if(!command.contains(missing)){
            throw new Exception("Substring \'"+missing+"\' não presente na linha de código \'"+command+"\'que requer \'backpatch\' quando emitindo-a!");
        }
        else{
            this.code.put(this.nextAddr,new IncompleteLine(command,missing));
            this.nextAddr++;
        }
    }

    public void backpatch(List<Integer> addrs, int target) throws Exception{
        for (Integer addr:addrs) {
            if(!this.code.containsKey(addr))
                throw new Exception("O endereço " + addr.toString() + " não foi referenciado pelo código gerado!");
            LineModel codeline = this.code.get(addr);
            if(codeline instanceof IncompleteLine)
                codeline = ((IncompleteLine) codeline).backpatch(target);
            this.code.replace(addr,codeline);
        }
    }

    public int nextInst(){
        return nextAddr;
    }

    @Override
    public String toString(){
        StringBuilder codelines = new StringBuilder();
        Set<Integer> addrs = new TreeSet<>(this.code.keySet());
        for (Integer addr:addrs) {
            codelines.append(addr.toString())
                    .append("\t")
                    .append(this.code.get(addr).showLine())
                    .append("\n");
        }
        return codelines.toString();
    }
}
