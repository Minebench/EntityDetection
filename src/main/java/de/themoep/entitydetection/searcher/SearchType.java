package de.themoep.entitydetection.searcher;

import org.bukkit.block.BlockState;
import org.bukkit.entity.Ambient;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Golem;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.Item;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Monster;
import org.bukkit.entity.NPC;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Slime;
import org.bukkit.entity.WaterMob;
import org.bukkit.entity.boat.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
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
public enum SearchType {
    MONSTER(
            new Class[]{
                    Monster.class,
                    Slime.class
            }
    ),
    PASSIVE(
            new String[]{"ANIMAL"},
            new Class[]{
                    Animals.class,
                    Ambient.class,
                    NPC.class,
                    WaterMob.class,
                    Golem.class
            }
    ),
    MISC(
            new EntityType[]{
                    EntityType.FIREWORK_ROCKET,
                    EntityType.EYE_OF_ENDER,
                    EntityType.ACACIA_BOAT,
                    EntityType.ACACIA_CHEST_BOAT,
                    EntityType.BAMBOO_RAFT,
                    EntityType.BAMBOO_CHEST_RAFT,
                    EntityType.BIRCH_BOAT,
                    EntityType.BIRCH_CHEST_BOAT,
                    EntityType.CHERRY_BOAT,
                    EntityType.CHERRY_CHEST_BOAT,
                    EntityType.DARK_OAK_BOAT,
                    EntityType.DARK_OAK_CHEST_BOAT,
                    EntityType.JUNGLE_BOAT,
                    EntityType.JUNGLE_CHEST_BOAT,
                    EntityType.MANGROVE_BOAT,
                    EntityType.MANGROVE_CHEST_BOAT,
                    EntityType.OAK_BOAT,
                    EntityType.OAK_CHEST_BOAT,
                    EntityType.PALE_OAK_BOAT,
                    EntityType.PALE_OAK_CHEST_BOAT,
                    EntityType.SPRUCE_BOAT,
                    EntityType.SPRUCE_CHEST_BOAT
            },
            new Class[]{
                    Projectile.class,
                    Minecart.class,
                    Item.class
            }
    ),
    BLOCK(
            new EntityType[]{
                    EntityType.ARMOR_STAND,
                    EntityType.FALLING_BLOCK,
                    EntityType.END_CRYSTAL
            },
            new Class[]{Hanging.class}
    ),
    ENTITY(
            new String[]{"ENTITIES"},
            EntityType.values()
    ),
    TILE(
            new String[]{"BLOCKSTATE"},
            new Class[]{BlockState.class}
    ),
    ALL(
            EntityType.values(),
            new Class[]{BlockState.class}
    ),
    CUSTOM;

    private String[] aliases;

    private EntityType[] entityTypes;
    private Class<?>[] blockStates;

    SearchType(String[] aliases, EntityType[] eTypes, Class[] classes) {
        this.aliases = aliases;
        Set<EntityType> typeSet = new HashSet<EntityType>();
        Collections.addAll(typeSet, eTypes);

        List<Class> classList = new LinkedList<Class>(Arrays.asList(classes));

        if(classes.length > 0) {
            for(EntityType et : EntityType.values()) {
                if(typeSet.contains(et)) {
                    continue;
                }
                Class<? extends Entity> e = et.getEntityClass();
                if(e == null) {
                    continue;
                }
                for (Class eClass : classList) {
                    if (eClass.isAssignableFrom(e)) {
                        typeSet.add(et);
                        break;
                    }
                }
            }
        }
        entityTypes = typeSet.toArray(new EntityType[typeSet.size()]);
        blockStates = classList.toArray(new Class[classList.size()]);
    }

    SearchType(Class<? extends Entity>[] classes) {
        this(new String[]{}, new EntityType[]{}, classes);
    }

    SearchType(EntityType[] types) {
        this(new String[]{}, types, new Class[]{});
    }

    SearchType(String[] aliases, Class<? extends Entity>[] classes) {
        this(aliases, new EntityType[]{}, classes);
    }

    SearchType(EntityType[] types, Class<? extends Entity>[] classes) {
        this(new String[]{}, types, classes);
    }

    SearchType(String[] aliases, EntityType[] entityTypes) {
        this(aliases, entityTypes, new Class[]{});
    }

    SearchType() {
        this(new String[]{}, new EntityType[]{}, new Class[]{});
    }

    /**
     * Get a sub command by its alias
     * @param alias The alias to search for
     * @return The sub command
     * @throws IllegalArgumentException Thrown when there is no sub command with this alias
     */
    public static SearchType getByAlias(String alias) throws IllegalArgumentException{
        for(SearchType type : SearchType.values()) {
            for(String a : type.aliases) {
                if(a.equals(alias)) {
                    return type;
                }
            }
        }
        throw new IllegalArgumentException(alias + " is not an alias of any SearchType.");
    }

    /**
     * Get all the entity types that belong to this search type
     *
     * @return An Array of EntityTypes, CUSTOM's list is empty and should be filled by you per search
     */
    public EntityType[] getEntities() {
        return entityTypes;
    }

    /**
     * Get all the classes of tile entity blockstates that belong to this search type
     *
     * @return An Array of Classes, CUSTOM's list is empty and should be filled by you per search
     */
    public Class<?>[] getBlockStates() {
        return blockStates;
    }
}
