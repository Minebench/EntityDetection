package de.themoep.entitydetection.searcher;

import de.themoep.entitydetection.EntityDetection;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
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
public class EntitySearch extends BukkitRunnable {
    private final EntityDetection plugin;
    private final CommandSender owner;
    private SearchType type = SearchType.CUSTOM;
    private Set<EntityType> searchedEntities = new HashSet<EntityType>();
    private Set<Class<?>> searchedBlockStates = new HashSet<Class<?>>();
    private Set<Material> searchedMaterial = new HashSet<Material>();
    private long startTime;
    private boolean running = true;
    private List<Entity> entities = new ArrayList<Entity>();
    private List<BlockState> blockStates = new ArrayList<BlockState>();

    public EntitySearch(EntityDetection plugin, CommandSender sender) {
        this.plugin = plugin;
        owner = sender;
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

    /**
     * Get the duration since this search started
     * @return The duration in seconds
     */
    public long getDuration() {
        return (System.currentTimeMillis() - getStartTime()) / 1000;
    }

    public BukkitTask start() {
        if (searchedEntities.size() > 0) {
            for (World world : plugin.getServer().getWorlds()) {
                entities.addAll(world.getEntities());
            }
        }
        if (searchedBlockStates.size() > 0 || searchedMaterial.size() > 0) {
            for (World world : plugin.getServer().getWorlds()) {
                for (Chunk chunk : world.getLoadedChunks()) {
                    blockStates.addAll(Arrays.asList(chunk.getTileEntities()));
                }
            }
        }
        return runTaskAsynchronously(plugin);
    }

    public boolean isRunning() {
        return running;
    }

    public void stop(String name) {
        running = false;
        cancel();
        if(!owner.getName().equals(name)) {
            owner.sendMessage(ChatColor.YELLOW + name + ChatColor.RED + " stopped your " + getType() + " search after " + getDuration() + "s!");
        }
    }

    public void run() {
        startTime = System.currentTimeMillis();
        SearchResult result = new SearchResult(this);

        for(Entity e : entities) {
            if(!running) {
                return;
            }
            if(searchedEntities.contains(e.getType())) {
                result.addEntity(e);
            }
        }

        for (BlockState blockState : blockStates) {
            if (!running) {
                return;
            }
            if (searchedBlockStates.contains(BlockState.class) || searchedMaterial.contains(blockState.getType()) || searchedBlockStates.contains(blockState.getClass())) {
                result.addBlockState(blockState);
            }
        }

        result.sort();
        plugin.addResult(result);;
        plugin.send(owner, result);
        running = false;
    }
}
