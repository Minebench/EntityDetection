package de.themoep.entitydetection.searcher;

import de.themoep.entitydetection.ChunkLocation;
import de.themoep.entitydetection.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
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
        add(entity.getLocation(), entity.getType().toString());
    }

    @Override
    public void addBlockState(BlockState blockState) {
        add(blockState.getLocation(), blockState.getType().toString());
    }

    @Override
    public void add(Location location, String type) {
        ChunkLocation chunkLocation = new ChunkLocation(location);

        if(!resultEntryMap.containsKey(chunkLocation)) {
            resultEntryMap.put(chunkLocation, new SearchResultEntry<>(chunkLocation));
        }
        resultEntryMap.get(chunkLocation).increment(type);
    }

    @Override
    public void teleport(Player sender, SearchResultEntry<ChunkLocation> entry, int i) {
        try {
            Chunk chunk = entry.getLocation().toBukkit(Bukkit.getServer());

            Location loc = null;

            for(Entity e : chunk.getEntities()) {
                if(e.getType().toString().equals(entry.getEntryCount().get(0).getKey())) {
                    loc = e.getLocation();
                    break;
                }
            }

            for (BlockState b : chunk.getTileEntities()) {
                if(b.getType().toString().equals(entry.getEntryCount().get(0).getKey())) {
                    loc = b.getLocation().add(0, 1, 0);
                    break;
                }
            }

            if (loc == null) {
                loc = chunk.getWorld().getHighestBlockAt(chunk.getX() * 16 + 8, chunk.getZ() * 16 + 8).getLocation().add(0, 2, 0);
            }

            sender.teleport(loc, PlayerTeleportEvent.TeleportCause.PLUGIN);
            sender.sendMessage(
                    ChatColor.GREEN + "Teleported to entry " + ChatColor.WHITE + i + ": " +
                            ChatColor.YELLOW + entry.getLocation() + " " + ChatColor.RED + entry.getSize() + " " +
                            ChatColor.GREEN + Utils.enumToHumanName(entry.getEntryCount().get(0).getKey()) + "[" +
                            ChatColor.WHITE + entry.getEntryCount().get(0).getValue() + ChatColor.GREEN + "]"
            );

        } catch(IllegalArgumentException e) {
            sender.sendMessage(ChatColor.RED + e.getMessage());
        }
    }
}
