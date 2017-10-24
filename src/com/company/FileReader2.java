package com.company;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;

public class FileReader2 {
    public static void main(String[] args) throws IOException {
        readFileToMap("none");
    }

    public static HashMap readFileToMap(String filePath) throws IOException {
        HashMap<String, Integer> map = new HashMap<>();

        FileInputStream inputStream = null;
        Scanner sc = null;
        try {
            inputStream = new FileInputStream(filePath);
            sc = new Scanner(inputStream, "UTF-8");

            int counter = 0;
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                String[] a = line.split(":");
                map.put(a[0], Integer.parseInt(a[1].trim()));
                counter++;
            }
            System.out.println(map.size());
            System.out.println(counter);
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
            if (sc != null) {
                sc.close();
            }
        }
        return map;
    }
}
