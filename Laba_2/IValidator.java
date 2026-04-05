public interface IValidator {
    int MAX_SIZE = 100; 

    void validateSize(int maxSize) throws TooManyElementsException;
    void validateMinValue(int minValue) throws ValueTooSmallException;
}
