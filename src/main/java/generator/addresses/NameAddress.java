package generator.addresses;

import generator.Type;

public class NameAddress extends Address{
    private final String id;
    private final int width;

    public NameAddress(String name,Type type){
        super(type);
        this.id = name;
        switch (type){
            case INTEGER:
                this.width = 4;
                break;
            case FLOAT:
                this.width = 8;
                break;
            case STRING:
                this.width = 40;
                break;
            default:
                this.width = 0;
                break;
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
