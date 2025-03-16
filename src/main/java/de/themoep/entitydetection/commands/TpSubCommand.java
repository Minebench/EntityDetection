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
                "<#result>"
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
        return teleport((Player)sender, args[0], getPlugin().getResult(sender));
    }

    private <T> boolean teleport(Player sender, String page, SearchResult<T> lastResult) {
        if(lastResult == null) {
            sender.sendMessage(ChatColor.RED + "You have to view a search result before teleporting to an entry! Use /detect search or /detect list [<type>]");
            return true;
        }

        int i;
        try {
            i = Integer.parseInt(page);
        } catch(NumberFormatException e) {
            sender.sendMessage(ChatColor.YELLOW + page + ChatColor.RED + " is not a proper number input!");
            return false;
        }

        if(i == 0 || lastResult.getSortedEntries().size() < i) {
            sender.sendMessage(ChatColor.RED + "Result " + ChatColor.YELLOW + page + ChatColor.RED + " is not in the list!");
            return true;
        }

        SearchResultEntry<T> entry = lastResult.getSortedEntries().get(i - 1);
        lastResult.teleport(sender, entry, i);
        return true;
    }
}
