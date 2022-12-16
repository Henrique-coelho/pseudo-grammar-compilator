package generator.codeline;

public class Line extends LineModel{
    String line;

    public Line(String line){
        this.line = line;
    }

    @Override
    public String showLine(){
        return line;
    }
}
