package de.themoep.entitydetection.searcher;

import de.themoep.entitydetection.ChunkLocation;
import de.themoep.entitydetection.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

public class ChunkSearchResult extends SearchResult<ChunkLocation> {

    public ChunkSearchResult(EntitySearch search) {
        super(search);
    }

    @Override
    public void addEntity(Entity entity) {
        if (entity == null || !entity.isValid()) {
            return;
        }
        Location loc = entity.getLocation();
        if (loc.getWorld() != null) {
            add(loc, entity.getType().toString());
        }
    }

    @Override
    public void addBlockState(BlockState blockState) {
        if (blockState == null) {
            return;
        }
        Location loc = blockState.getLocation();
        if (loc.getWorld() != null) {
            add(loc, blockState.getType().toString());
        }
    }

    @Override
    public void add(Location location, String type) {
        if (location == null || location.getWorld() == null) {
            return;
        }
        ChunkLocation chunkLocation = new ChunkLocation(location);

        if (!resultEntryMap.containsKey(chunkLocation)) {
            resultEntryMap.put(chunkLocation, new SearchResultEntry<>(chunkLocation));
        }
        resultEntryMap.get(chunkLocation).increment(type);
    }

    @Override
    public void teleport(Player sender, SearchResultEntry<ChunkLocation> entry, int i) {
        try {
            World targetWorld = Bukkit.getWorld(entry.getLocation().getWorld());
            if (targetWorld == null) {
                sender.sendMessage(ChatColor.RED + "World " + ChatColor.WHITE + entry.getLocation().getWorld() + ChatColor.RED + " is not loaded anymore.");
                return;
            }

            int cx = entry.getLocation().getX();
            int cz = entry.getLocation().getZ();
            int anchorX = (cx << 4) + 8;
            int anchorZ = (cz << 4) + 8;
            Location location = new Location(targetWorld, anchorX, 64, anchorZ);

            scheduler.runAtLocation(location, task -> targetWorld.getChunkAtAsync(cx, cz, false, chunk -> {
                if (chunk == null) {
                    sender.sendMessage(ChatColor.RED + "Chunk " + ChatColor.WHITE + cx + ", " + cz + ChatColor.RED + " could not be loaded.");
                    return;
                }
                Location loc = null;

                for (Entity e : chunk.getEntities()) {
                    if (e.isValid() && e.getType().toString().equals(entry.getEntryCount().get(0).getKey())) {
                        loc = e.getLocation();
                        break;
                    }
                }

                if (loc == null) {
                    for (BlockState b : chunk.getTileEntities()) {
                        if (b.getType().toString().equals(entry.getEntryCount().get(0).getKey())) {
                            Location blockLoc = b.getLocation();
                            loc = blockLoc.add(0, 1, 0);
                            break;
                        }
                    }
                }

                if (loc == null) {
                    loc = chunk.getWorld().getHighestBlockAt(anchorX, anchorZ).getLocation().add(0, 2, 0);
                }

                Location finalLoc = loc;
                scheduler.teleportAsync(sender, finalLoc, PlayerTeleportEvent.TeleportCause.PLUGIN);

                sender.sendMessage(
                        ChatColor.GREEN + "Teleported to entry " + ChatColor.WHITE + i + ": " +
                                ChatColor.YELLOW + entry.getLocation() + " " + ChatColor.RED + entry.getSize() + " " +
                                ChatColor.GREEN + Utils.enumToHumanName(entry.getEntryCount().get(0).getKey()) + "[" +
                                ChatColor.WHITE + entry.getEntryCount().get(0).getValue() + ChatColor.GREEN + "]"
                );
            }));

        } catch (IllegalArgumentException e) {
            sender.sendMessage(ChatColor.RED + e.getMessage());
        }
    }
}
