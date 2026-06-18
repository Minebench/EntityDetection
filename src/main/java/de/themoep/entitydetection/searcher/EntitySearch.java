package de.themoep.entitydetection.searcher;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.Multimaps;
import com.tcoded.folialib.impl.PlatformScheduler;
import com.tcoded.folialib.wrapper.task.WrappedTask;
import de.themoep.entitydetection.EntityDetection;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

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
public class EntitySearch implements Consumer<WrappedTask> {
    private final EntityDetection plugin;
    private final PlatformScheduler scheduler;
    private final CommandSender owner;
    private SearchType type = SearchType.CUSTOM;
    private Set<EntityType> searchedEntities = new HashSet<>();
    private Set<Class<?>> searchedBlockStates = new HashSet<>();
    private Set<Material> searchedMaterial = new HashSet<>();
    private long startTime;
    private boolean running = true;
    // Populated concurrently from multiple region threads on Folia, so both must be thread-safe.
    private Multimap<EntityType, Location> entities = Multimaps.synchronizedMultimap(MultimapBuilder.hashKeys().arrayListValues().build());
    private Map<Material, Multimap<Class, Location>> blockStates = new ConcurrentHashMap<>();

    private boolean isWorldGuardRegion = false;
    private final AtomicInteger pending = new AtomicInteger(0);

    public EntitySearch(EntityDetection plugin, CommandSender sender) {
        this.plugin = plugin;
        this.scheduler = plugin.getScheduler();
        owner = sender;
    }

    public PlatformScheduler getScheduler() {
        return scheduler;
    }

    public SearchType getType() {
        return type;
    }

    public void setType(SearchType type) {
        if(getSearchedEntities().size() == 0 && getSearchedBlockStates().size() == 0 && getSearchedMaterial().size() == 0) {
            this.type = type;
        } else {
            this.type = SearchType.CUSTOM;
        }
        Collections.addAll(searchedEntities, type.getEntities());
        Collections.addAll(searchedBlockStates, type.getBlockStates());
    }

    public void addSearchedType(EntityType type) {
        searchedEntities.add(type);
        this.type = SearchType.CUSTOM;
    }

    public void addSearchedBlockState(Class<?> c) {
        searchedBlockStates.add(c);
        this.type = SearchType.CUSTOM;
    }

    public void addSearchedMaterial(Material material) {
        searchedMaterial.add(material);
        this.type = SearchType.CUSTOM;
    }

    public Set<EntityType> getSearchedEntities() {
        return searchedEntities;
    }

    public Set<Class<?>> getSearchedBlockStates() {
        return searchedBlockStates;
    }

    public Set<Material> getSearchedMaterial() {
        return searchedMaterial;
    }

    public String getOwner() {
        return owner.getName();
    }

    public long getStartTime() {
        return startTime;
    }

    public boolean isWorldGuardRegion() {
        return this.isWorldGuardRegion;
    }

    public void setWorldGuardRegion(boolean value) {
        this.isWorldGuardRegion = value;
    }

    /**
     * Get the duration since this search started
     * @return The duration in seconds
     */
    public long getDuration() {
        return (System.currentTimeMillis() - getStartTime()) / 1000;
    }

    public void start() {
        int scheduled = 0;
        if (searchedEntities.size() > 0) {
            for (World world : plugin.getServer().getWorlds()) {
                pending.incrementAndGet();
                scheduled++;
                scheduler.runAtLocation(world.getSpawnLocation(), task -> {
                    try {
                        for (Entity entity : world.getEntities()) {
                            entities.put(entity.getType(), entity.getLocation());
                        }
                    } finally {
                        if (pending.decrementAndGet() == 0) {
                            scheduler.runAsync(this);
                        }
                    }
                });
            }
        }
        if (searchedBlockStates.size() > 0 || searchedMaterial.size() > 0) {
            for (World world : plugin.getServer().getWorlds()) {
                for (Chunk chunk : world.getLoadedChunks()) {
                    pending.incrementAndGet();
                    scheduled++;
                    scheduler.runAtLocation(chunk.getBlock(0, 0, 0).getLocation(), task -> {
                        try {
                            for (BlockState state : chunk.getTileEntities()) {
                                Multimap<Class, Location> multiMap = blockStates.computeIfAbsent(state.getType(),
                                        k -> Multimaps.synchronizedMultimap(MultimapBuilder.hashKeys().arrayListValues().build()));
                                multiMap.put(state.getClass(), state.getLocation());
                            }
                        } finally {
                            if (pending.decrementAndGet() == 0) {
                                scheduler.runAsync(this);
                            }
                        }
                    });
                }
            }
        }
        if (scheduled == 0) {
            scheduler.runAsync(this);
        }
    }

    public boolean isRunning() {
        return running;
    }

    public void stop(String name) {
        running = false;
        if(!owner.getName().equals(name)) {
            owner.sendMessage(ChatColor.YELLOW + name + ChatColor.RED + " stopped your " + getType() + " search after " + getDuration() + "s!");
        }
    }

    public void accept(WrappedTask task) {
        startTime = System.currentTimeMillis();
        SearchResult<?> result;
        if (isWorldGuardRegion) result = new WGSearchResult(this);
        else result = new ChunkSearchResult(this);

        entities.forEach((type, location) -> {
            if (searchedEntities.contains(type)) {
                result.add(location, type.toString());
            }
        });

        blockStates.forEach((type, blockLocations) -> {
            blockLocations.forEach((clazz, location) -> {
                if (searchedBlockStates.contains(BlockState.class) || searchedMaterial.contains(type) || searchedBlockStates.contains(clazz)) {
                    result.add(location, type.toString());
                }
            });
        });

        if (!running) {
            return;
        }

        result.sort();
        plugin.addResult(result);
        plugin.send(owner, result);
        running = false;
        plugin.clearCurrentSearch();
    }
}
