package generator.codeline;

public class IncompleteLine extends LineModel{
    private final String line;
    private final String missing;

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
        return new Line(line.replaceAll(missing, String.valueOf(addr)));
    }
}
