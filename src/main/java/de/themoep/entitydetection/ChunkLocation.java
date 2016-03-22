package de.themoep.entitydetection;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;

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
public class ChunkLocation {
    private final String world;
    private final int x;
    private final int z;

    public ChunkLocation(Location location) {
        this.world = location.getWorld().getName().toLowerCase();
        this.x = (int) Math.floor(location.getX() / 16);
        this.z = (int) Math.floor(location.getZ() / 16);
    }

    public String getWorld() {
        return world;
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    public String toString() {
        return x + ", " + z + ": " + world;
    }

    public Chunk toBukkit(Server server) {
        World world = server.getWorld(getWorld());
        if(world == null) {
            throw new IllegalArgumentException("No world with the name " + getWorld() + " found on the server for this chunk entry?");
        }
        return world.getChunkAt(x, z);
    }

    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof ChunkLocation)) {
            return false;
        }

        if(o == this) {
            return true;
        }

        ChunkLocation other = (ChunkLocation) o;
        return x == other.x && z == other.z && world.equalsIgnoreCase(other.world);
    }
}
