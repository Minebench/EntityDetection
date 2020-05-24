package de.themoep.entitydetection.commands;

import de.themoep.entitydetection.EntityDetection;
import de.themoep.entitydetection.Utils;
import de.themoep.entitydetection.searcher.SearchResult;
import de.themoep.entitydetection.searcher.SearchResultEntry;
import de.themoep.entitydetection.searcher.SearchType;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
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
public class ListSubCommand extends SubCommand {
    public ListSubCommand(EntityDetection plugin) {
        super(plugin, plugin.getName().toLowerCase(), "list",
                "[<page> [monster|animal|misc|block|tiles|all| <type>]]",
                "Lists the last result. Specify a type to get a specific one."
        );
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        SearchResult<?> result = getPlugin().getResult(sender);
        int page = 1;
        String lastName = sender.getName();
        if(args.length > 0) {
            try {
                page = Integer.parseInt(args[0]);
            } catch(NumberFormatException e) {
                sender.sendMessage(ChatColor.YELLOW + args[0] + ChatColor.RED + " is not a proper number input!");
                return false;
            }
            if(args.length > 1) {
                lastName = args[1];

                String arg = args[1];

                if(arg.endsWith("s")) {
                    arg = arg.substring(0, arg.length() - 1);
                }

                boolean found = false;
                if (!found) {
                    try {
                        result = getPlugin().getResult(EntityType.valueOf(arg.toUpperCase()).toString());
                        found = true;
                    } catch (IllegalArgumentException ignored) {}
                }
                if (!found) {
                    try {
                        result = getPlugin().getResult(Class.forName("org.bukkit.block." + arg, false, getPlugin().getServer().getClass().getClassLoader()).getSimpleName()); //TODO: This is case sensitive :(
                        found = true;
                    } catch (ClassNotFoundException ignored) {}
                }
                if (!found) {
                    try {
                        result = getPlugin().getResult(SearchType.valueOf(arg.toUpperCase()));
                        found = true;
                    } catch (IllegalArgumentException ignored) {}
                }
                if (!found) {
                    try {
                        result = getPlugin().getResult(SearchType.getByAlias(arg.toUpperCase()));
                        found = true;
                    } catch(IllegalArgumentException ignored) {}
                }
                if (!found) {
                    try {
                        result = getPlugin().getResult(Material.valueOf(arg.toUpperCase()).toString()); //TODO: This doesn't check for tile entities
                        found = true;
                    } catch (IllegalArgumentException ignored) {}
                }
                if (!found) {
                    sender.sendMessage(ChatColor.YELLOW + arg + ChatColor.RED + " is neither a valid EntityType, Material, BlockState, SearchType or alias of a search type?");
                    return false;
                }
            }
        }

        if(result == null) {
            sender.sendMessage(ChatColor.RED + "No previous result for " + lastName + " found!");
            return true;
        }

        if(page == 0 || result.getSortedEntries().size() < (page - 1) * 10) {
            sender.sendMessage(ChatColor.RED + "There is no page " + page + " in the last " + lastName + " result!");
            return true;
        }

        getPlugin().send(sender, result, page - 1);
        return true;
    }
}
