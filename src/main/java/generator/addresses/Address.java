package generator.addresses;

import generator.Type;

public abstract class Address {
    protected Type type;

    protected Address(Type type){
        this.type = type;
    }

    public Type type(){
        return this.type;
    }

    public abstract String value();
}
