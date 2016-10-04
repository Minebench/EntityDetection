package de.themoep.entitydetection.commands;

import de.themoep.entitydetection.EntityDetection;
import de.themoep.entitydetection.Utils;
import de.themoep.entitydetection.searcher.SearchResult;
import de.themoep.entitydetection.searcher.SearchResultEntry;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

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
public class TpSubCommand extends SubCommand {
    public TpSubCommand(EntityDetection plugin) {
        super(plugin, plugin.getName().toLowerCase(), "tp",
                "<#result>",
                "Teleports you to the chunk of the result entry."
        );
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if(!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This sub command can only be run by a player!");
            return true;
        }

        if(args.length == 0) {
            return false;
        }
        SearchResult lastResult = getPlugin().getResult(sender);
        if(lastResult == null) {
            sender.sendMessage(ChatColor.RED + "You have to view a search result before teleporting to an entry! Use /detect search or /detect list [<type>]");
            return true;
        }

        int i;
        try {
            i = Integer.parseInt(args[0]);
        } catch(NumberFormatException e) {
            sender.sendMessage(ChatColor.YELLOW + args[0] + ChatColor.RED + " is not a proper number input!");
            return false;
        }

        if(i == 0 || lastResult.getSortedEntries().size() < i) {
            sender.sendMessage(ChatColor.RED + "Result " + ChatColor.YELLOW + args[0] + ChatColor.RED + " is not in the list!");
            return true;
        }

        SearchResultEntry entry = lastResult.getSortedEntries().get(i - 1);

        try {
            Chunk chunk = entry.getChunk().toBukkit(getPlugin().getServer());

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

            ((Player) sender).teleport(loc, PlayerTeleportEvent.TeleportCause.PLUGIN);
            sender.sendMessage(
                    ChatColor.GREEN + "Teleported to entry " + ChatColor.WHITE + i + ": " +
                            ChatColor.YELLOW + entry.getChunk() + " " + ChatColor.RED + entry.getSize() + " " +
                            ChatColor.GREEN + Utils.enumToHumanName(entry.getEntryCount().get(0).getKey()) + "[" +
                            ChatColor.WHITE + entry.getEntryCount().get(0).getValue() + ChatColor.GREEN + "]"
            );

        } catch(IllegalArgumentException e) {
            sender.sendMessage(ChatColor.RED + e.getMessage());
        }
        return true;
    }
}
