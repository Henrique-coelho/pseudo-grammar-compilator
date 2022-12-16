package generator.codeline;

import generator.addresses.value.IntegerValue;

public class IncompleteLine extends LineModel{
    String line;
    String missing;

    public IncompleteLine(String line,String missing){
        this.line = line;
        this.missing = missing;
    }

    public IncompleteLine(String line){
        this.line = line;
        this.missing = "???";
    }

    @Override
    public String showLine() {
        return line;
    }

    public Line backpatch(int addr){
        String s = "teste";
        return new Line(line.replaceAll(missing, String.valueOf(addr)));
    }
}
