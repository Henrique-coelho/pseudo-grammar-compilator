package generator.addresses.value;

public class IntegerValue extends Value<Integer>{
    private Integer value;

    public IntegerValue(int value){
        this.value = value;
    }

    @Override
    public Integer value(){
        return value;
    }
}
