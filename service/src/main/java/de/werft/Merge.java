package de.werft;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by ratzeputz on 23.01.17.
 */
public class Merge {

    public static String merge(String old, String input) {
        Map<String, Set<String>> file1 = new HashMap<>();
        Map<String, Set<String>> file2 = new HashMap<>();
        String merged = "";

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
                set = new HashSet<>();
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
                set = new HashSet<>();
                file2.put(subpred, set);
            }
            set.add(rest);
        }

        Set<String> keys = new HashSet<>();
        keys.addAll(file1.keySet());
        keys.addAll(file2.keySet());

        for (String key : keys) {
            Set<String> dq = file1.get(key);
            Set<String> pp = file2.get(key);

            if (dq == null) {
                for (String s : pp) {
                    merged = merged + key+" "+s+"\n";
                }
            }

            if (pp == null) {
                for (String s : dq) {
                    merged = merged + key+" "+s+"\n";
                }
            }

            if (dq != null && pp != null) {
//				String[] split = key.split(" ");
//				if (datProps.contains(split[1])) {
//
//				}

                for (String s : pp) {
                    merged = merged + key+" "+s+"\n";
                }
            }
        }

        return merged;
    }

}
