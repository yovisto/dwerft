package de.werft;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Created by ratzeputz on 23.01.17.
 */
public class Merge {

    public static String merge(String old, String input) {
        Map<String, Set<String>> file1 = new TreeMap<String, Set<String>>();
        Map<String, Set<String>> file2 = new TreeMap<String, Set<String>>();
        
        StringBuilder mergedTriples = new StringBuilder();

        String[] split = old.split("\n");
        for (String s : split) {
            String[] words = s.split(" ");

            String rest = "";
            for (int i = 2; i < words.length; i++) {
                rest = rest + words[i] + " ";
            }

            String subpred = words[0] + " " + words[1];

            Set<String> set = file1.get(subpred);
            if (set == null) {
                set = new HashSet<String>();
                file1.put(subpred, set);
            }
            set.add(rest);
        }

        split = input.split("\n");
        for (String s : split) {
            String[] words = s.split(" ");

            String rest = "";
            for (int i = 2; i < words.length; i++) {
                rest = rest + words[i] + " ";
            }

            String subpred = words[0] + " " + words[1];

            Set<String> set = file2.get(subpred);
            if (set == null) {
                set = new HashSet<String>();
                file2.put(subpred, set);
            }
            set.add(rest);
        }

        Set<String> keys = new HashSet<String>();
        keys.addAll(file1.keySet());
        keys.addAll(file2.keySet());
        
        for (String key : keys) {
            Set<String> oldTriples = file1.get(key);
            Set<String> newTriples = file2.get(key);

            if (newTriples == null) {
                for (String s : oldTriples) {
                	mergedTriples.append(key+" "+s+" ");
                }
            } else {
                for (String s : newTriples) {
                	mergedTriples.append(key+" "+s+" ");
                }
            }

        }

        return mergedTriples.toString();
    }

}
