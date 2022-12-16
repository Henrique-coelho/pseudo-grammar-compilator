package generator.addresses;

import generator.Type;

public class TempAddress extends Address{
    private static int copies = 0;
    private String id;
    private int copy;
    private int width;


    public TempAddress(Type type){
        super(type);
        this.id = "t";
        this.copy = copies;
        copies++;

        switch (type){
            case INTEGER:
                this.width = 4;
            case FLOAT:
                this.width = 8;
            case STRING:
                this.width = 40;
            default:
                this.width = 0;
        }
    }

    @Override
    public String value() {
        if(copy>0)
            return id+Integer.toString(copy);
        return this.id;
    }
}
