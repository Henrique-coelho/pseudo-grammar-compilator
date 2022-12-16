package generator.addresses.value;

public abstract class Value<T> {
    protected T value;

    protected Value(){}

    public abstract T value();
}
