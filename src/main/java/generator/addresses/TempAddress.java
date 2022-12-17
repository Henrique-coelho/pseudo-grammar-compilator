package generator.addresses;

import generator.Type;
import lombok.Getter;

@Getter
public class TempAddress extends Address{
    private static int copies = 0;
    private final String id;
    private final int copy;
    private final int width;


    public TempAddress(Type type){
        super(type);
        this.id = "t";
        this.copy = copies;
        copies++;

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
        if(copy>0){
            return id + copy;
        }
        return this.id;
    }
}
