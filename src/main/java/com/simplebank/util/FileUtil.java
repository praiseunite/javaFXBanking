package com.simplebank.util;

import com.simplebank.model.User;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

// Session 3 - File handling and I/O streams
public class FileUtil {
    private static final String DATA_DIR = "data";

    static {
        // Create data directory if it doesn't exist
        File directory = new File(DATA_DIR);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    // Session 2 - Generic method with type parameter
    // Session 3 - File output streams and object serialization
    public static <T extends Serializable> void saveToFile(List<User> object, String filename) throws IOException {
        File file = new File(DATA_DIR + File.separator + filename);

        // Session 3 - BufferedOutputStream for performance, try-with-resources for automatic resource management
        try (FileOutputStream fos = new FileOutputStream(file);
             BufferedOutputStream bos = new BufferedOutputStream(fos);
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {

            oos.writeObject(object);
        }
    }

    // Session 2 - Generic method with type parameter
    // Session 3 - File input streams and object deserialization
    @SuppressWarnings("unchecked")
    public static <T extends Serializable> T loadFromFile(String filename) throws IOException, ClassNotFoundException {
        File file = new File(DATA_DIR + File.separator + filename);

        if (!file.exists()) {
            return null;
        }

        try (FileInputStream fis = new FileInputStream(file);
             BufferedInputStream bis = new BufferedInputStream(fis);
             ObjectInputStream ois = new ObjectInputStream(bis)) {

            return (T) ois.readObject();
        }
    }

    // Session 3 - File listing and directory operations
    public static List<String> listFilesInDirectory() {
        File directory = new File(DATA_DIR);
        File[] files = directory.listFiles();
        List<String> fileNames = new ArrayList<>();

        if (files != null) {
            for (File file : files) {
                fileNames.add(file.getName());
            }
        }

        return fileNames;
    }
}