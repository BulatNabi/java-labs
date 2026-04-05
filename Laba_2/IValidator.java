public interface IValidator {
    void validateSize(int maxSize) throws TooManyElementsException;
    void validateMinValue(int minValue) throws ValueTooSmallException;
}
