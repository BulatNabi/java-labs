import java.io.*;
import java.util.*;

public class FileInputSource {
    private IFileInputEvent listener;

    public FileInputSource(IFileInputEvent listener) {
        this.listener = listener;
    }

    public List<String> readLines(String path) throws IOException {
        listener.onFileInput(path);
        List<String> lines = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader(path));
        try {
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
        } finally {
            br.close();
        }
        return lines;
    }
}
