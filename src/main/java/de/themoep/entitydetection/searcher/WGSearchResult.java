package de.themoep.entitydetection.searcher;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import de.themoep.entitydetection.Utils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.lang.ref.WeakReference;
import java.util.Objects;

public class WGSearchResult extends SearchResult<WGSearchResult.ProtectedRegionEntry> {
    public WGSearchResult(EntitySearch search) {
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
        RegionQuery query = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();
        ApplicableRegionSet applicableRegions = query.getApplicableRegions(BukkitAdapter.adapt(location));

        applicableRegions.forEach(region -> {
            ProtectedRegionEntry protectedRegionEntry = new ProtectedRegionEntry(location.getWorld(), region);
            if(!resultEntryMap.containsKey(protectedRegionEntry)) {
                resultEntryMap.put(protectedRegionEntry, new SearchResultEntry<>(protectedRegionEntry));
            }
            resultEntryMap.get(protectedRegionEntry).increment(type);
        });
    }

    @Override
    public void teleport(Player sender, SearchResultEntry<WGSearchResult.ProtectedRegionEntry> entry, int i) {
        com.sk89q.worldedit.util.Location wgLocation = entry.getLocation().region.getFlag(Flags.TELE_LOC);
        try {
            World world = entry.getLocation().world.get();
            if(world == null) {
                sender.sendMessage(ChatColor.RED + "World " + ChatColor.WHITE + entry.getLocation().worldName + ChatColor.RED + " is not loaded anymore.");
                return;
            }
            Location loc = wgLocation != null ? BukkitAdapter.adapt(wgLocation) : null;
            if(loc == null) {
                loc = BukkitAdapter.adapt(world, entry.getLocation().region.getMinimumPoint().add(
                        entry.getLocation().region.getMaximumPoint().subtract(entry.getLocation().region.getMinimumPoint()).divide(2)));
            }

            sender.teleport(loc, PlayerTeleportEvent.TeleportCause.PLUGIN);
            sender.sendMessage(
                    ChatColor.GREEN + "Teleported to entry " + ChatColor.WHITE + i + ": " +
                            ChatColor.YELLOW + entry.getLocation().region.getId() + " " + ChatColor.RED + entry.getSize() + " " +
                            ChatColor.GREEN + Utils.enumToHumanName(entry.getEntryCount().get(0).getKey()) + "[" +
                            ChatColor.WHITE + entry.getEntryCount().get(0).getValue() + ChatColor.GREEN + "]"
            );
        } catch(IllegalArgumentException e) {
            sender.sendMessage(ChatColor.RED + e.getMessage());
        }
    }

    public static class ProtectedRegionEntry {
        String worldName;
        WeakReference<World> world;
        ProtectedRegion region;

        public ProtectedRegionEntry(World world, ProtectedRegion region) {
            this.worldName = world.getName();
            this.world = new WeakReference<>(world);
            this.region = region;
        }

        @Override
        public String toString() {
            World w = world.get();
            return "w: " + worldName + (w == null ? " (unloaded)" : "") + ", r: " + region.getId();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ProtectedRegionEntry that = (ProtectedRegionEntry) o;
            return Objects.equals(world, that.world) &&
                    Objects.equals(region, that.region);
        }

        @Override
        public int hashCode() {
            return Objects.hash(world, region);
        }
    }
}
