package de.themoep.entitydetection.searcher;

import de.themoep.entitydetection.ChunkLocation;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Copyright 2016 Max Lee (https://github.com/Phoenix616/)
 * <p/>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Mozilla Public License as published by
 * the Mozilla Foundation, version 2.
 * <p/>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * Mozilla Public License v2.0 for more details.
 * <p/>
 * You should have received a copy of the Mozilla Public License v2.0
 * along with this program. If not, see <http://mozilla.org/MPL/2.0/>.
 */
public class SearchResultEntry implements Comparable<SearchResultEntry> {
    private ChunkLocation chunk;
    private Map<EntityType, Integer> entityCount = new HashMap<EntityType, Integer>();
    private List<Map.Entry<EntityType, Integer>> finalList = new ArrayList<Map.Entry<EntityType, Integer>>();
    private int size = 0;

    SearchResultEntry(ChunkLocation chunk) {
        this.chunk = chunk;
    }

    public void increment(EntityType type) {
        size++;
        if(!entityCount.containsKey(type)) {
            entityCount.put(type, 1);
        } else {
            entityCount.put(type, entityCount.get(type) + 1);
        }
    }

    public int getSize() {
        return size;
    }

    public ChunkLocation getChunk() {
        return chunk;
    }

    public List<Map.Entry<EntityType, Integer>> getEntityCount() {
        return finalList;
    }

    public void sort() {
        finalList = new ArrayList<Map.Entry<EntityType, Integer>>(entityCount.entrySet());
        Collections.sort(finalList, Collections.reverseOrder(new Comparator<Map.Entry<EntityType, Integer>>() {
            public int compare(Map.Entry<EntityType, Integer> o1, Map.Entry<EntityType, Integer> o2) {
                return Integer.compare(o1.getValue(), o2.getValue());
            }
        }));
    }

    public int compareTo(SearchResultEntry o) {
        return Integer.compare(size, o.getSize());
    }
}
