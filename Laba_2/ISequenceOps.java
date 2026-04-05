import java.util.ArrayList;

public interface ISequenceOps {
    void load(String[] args) throws StringContainsLettersException;
    ArrayList<Integer> getOddIndexed();   
    ArrayList<Integer> getEvenIndexed();  
}
