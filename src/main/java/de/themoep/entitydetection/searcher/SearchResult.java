package de.themoep.entitydetection.searcher;

import de.themoep.entitydetection.ChunkLocation;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
public class SearchResult {
    private SearchType type;
    private Set<EntityType> searchedEntities;
    private long startTime;
    private long endTime = 0;

    /**
     * Working search map, use resultEntryList after sorting this result!
     */
    private Map<ChunkLocation, SearchResultEntry> resultEntryMap = new HashMap<ChunkLocation, SearchResultEntry>();

    /**
     * Sorted, highest entity count per chunks first, only propagated after running .sort()
     */
    private List<SearchResultEntry> resultEntryList = new ArrayList<SearchResultEntry>();

    public SearchResult(EntitySearch search) {
        type = search.getType();
        searchedEntities = search.getSearchedEntities();
        startTime = search.getStartTime();
    }

    /**
     * Add an entity to this result
     * @param entity The entity to add
     */
    public void addEntity(Entity entity) {
        ChunkLocation chunkLoc = new ChunkLocation(entity.getLocation());
        if(!resultEntryMap.containsKey(chunkLoc)) {
            resultEntryMap.put(chunkLoc, new SearchResultEntry(chunkLoc));
        }
        resultEntryMap.get(chunkLoc).increment(entity.getType());
    }

    public SearchType getType() {
        return type;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime == 0 ? System.currentTimeMillis() : endTime;
    }

    public long getDuration() {
        return getEndTime() - getStartTime();
    }

    public Set<EntityType> getSearchedEntities() {
        return searchedEntities;
    }

    /**
     * Get a list of entries for every chunk, only propagated after calling sort()
     * @return An ArrayList of the chunks sorted from the highest
     */
    public List<SearchResultEntry> getSortedEntries() {
        return resultEntryList;
    }

    /**
     * Sort the results and set the end time
     */
    public void sort() {
        for(SearchResultEntry chunkEntry : resultEntryMap.values()) {
            chunkEntry.sort();
        }
        resultEntryList = new ArrayList<SearchResultEntry>(resultEntryMap.values());
        Collections.sort(resultEntryList, Collections.reverseOrder());

        endTime = System.currentTimeMillis();
    }

}
