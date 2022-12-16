package generator.addresses.value;

public class StringValue extends Value<String>{
    private String value;

    public StringValue(String value){
        this.value = "\""+value+"\"";
    }

    @Override
    public String value() {
        return value;
    }
}
