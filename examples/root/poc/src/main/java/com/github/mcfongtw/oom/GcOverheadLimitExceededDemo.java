package com.github.mcfongtw.oom;

import java.util.HashMap;
import java.util.Map;

public class GcOverheadLimitExceededDemo {

    private static class Key {
        Integer id;

        Key(Integer id) {
            this.id = id;
        }

        @Override
        public int hashCode() {
            return id.hashCode();
        }
    }

    public static void main(String[] args) {
        Map map = new HashMap();
        while (true) {
            for (int i = 0; i < 10000; i++) {
                if (!map.containsKey(new Key(i))) {
                    map.put(new Key(i), String.valueOf(i));
                }
            }
        }
    }
}
