package com.company;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ../wiki/dump -> write file (unique word frequency)
 */
public class WikipediaParser {
    public static void main(String[] args) {
        Map<String, Integer> map = new HashMap<>();

        List<String> files = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            files.add("C:\\wiki\\20140615-wiki-en_00000" + i + ".txt\\20140615-wiki-en_00000" + i + ".txt");
            getWordFrequencyMap(map, files.get(i));
        }
        for (int i = 10; i < 100; i++) {
            files.add("C:\\wiki\\20140615-wiki-en_0000" + i + ".txt\\20140615-wiki-en_0000" + i + ".txt");
            getWordFrequencyMap(map, files.get(i));
        }
        for (int i = 100; i < 1000; i++) {
            files.add("C:\\wiki\\20140615-wiki-en_000" + i + ".txt\\20140615-wiki-en_000" + i + ".txt");
            getWordFrequencyMap(map, files.get(i));
        }
        for (int i = 1000; i <= 4633; i++) {
            files.add("C:\\wiki\\20140615-wiki-en_00" + i + ".txt\\20140615-wiki-en_00" + i + ".txt");
            getWordFrequencyMap(map, files.get(i));
        }

        // write file
        final String FILENAME = "C:\\Avac-beta\\src\\main\\resources\\words";
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILENAME))) {
            map.forEach((s, integer) ->
            {
                try {
                    bw.write("\n" + s + ": " + integer);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println(map.size());

    }

    private static void getWordFrequencyMap(Map<String, Integer> map, String... fileName) {
        for (String f : fileName) {
            try (BufferedReader br = Files.newBufferedReader(Paths.get(f))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] words = line.split("\\s+");
                    for (String w : words) {
                        if (w.length() < 33) {
                            w = w.trim().replaceAll("[^a-zA-Z\\-']+", "").toLowerCase();
                            if (map.containsKey(w)) {
                                map.put(w, map.get(w) + 1);
                            } else {
                                map.put(w, 1);
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("Map.size = " + map.size() + "  -->  " + f);
        }
    }
}
