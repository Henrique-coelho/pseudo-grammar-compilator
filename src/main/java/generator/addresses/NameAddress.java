package generator.addresses;

import generator.Type;

public class NameAddress extends Address{
    private String id;
    private int width;

    public NameAddress(String name,Type type){
        super(type);
        this.id = name;
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
        return id;
    }

    public int width(){
        return width;
    }
}
