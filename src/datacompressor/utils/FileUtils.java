package datacompressor.utils;

import java.io.File;

public class FileUtils {
    public static File addFileExtension(File file, String extension) {
        return new File(file.getPath() + "." + extension);
    }
}
