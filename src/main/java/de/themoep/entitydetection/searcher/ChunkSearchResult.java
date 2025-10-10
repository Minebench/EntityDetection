package de.themoep.entitydetection.searcher;

import com.tcoded.folialib.impl.PlatformScheduler;
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

    private final PlatformScheduler scheduler;

    public ChunkSearchResult(EntitySearch search) {
        super(search);
        this.scheduler = search.getScheduler();
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

            targetWorld.getChunkAtAsync(cx, cz, true, chunk -> scheduler.runAtLocation(location, task -> {
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

        } catch(IllegalArgumentException e) {
            sender.sendMessage(ChatColor.RED + e.getMessage());
        }
    }
}
