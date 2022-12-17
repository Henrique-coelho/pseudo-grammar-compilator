package generator.addresses;

import generator.Type;
import generator.addresses.value.FloatValue;
import generator.addresses.value.IntegerValue;
import generator.addresses.value.StringValue;
import generator.addresses.value.Value;

public class ConstAddress extends Address{
    private final Value<?> value;

    public ConstAddress(int value){
        super(Type.INTEGER);
        this.value = new IntegerValue(value);
    }

    public ConstAddress(float value){
        super(Type.FLOAT);
        this.value = new FloatValue(value);
    }

    public ConstAddress(String value){
        super(Type.STRING);
        this.value = new StringValue(value);
    }

    @Override
    public String value(){
        return value.value().toString();
    }
}
