package semantic;

public class Axiom {
    private String type;
    private String value;
    //TODO modificar "value" para que tenha o tipo flexível
    public Axiom(String type,String value){
        this.type = type;
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public String getType() {
        return type;
    }

    public boolean sameTypeAs(String type) {
        return this.type == type;
    }
}
