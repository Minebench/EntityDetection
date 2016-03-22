package de.themoep.entitydetection.commands;

import de.themoep.entitydetection.EntityDetection;
import de.themoep.entitydetection.searcher.EntitySearch;
import de.themoep.entitydetection.searcher.SearchType;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;

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
public class SearchSubCommand extends SubCommand {
    public SearchSubCommand(EntityDetection plugin) {
        super(plugin, plugin.getName().toLowerCase(), "search",
                "[monster|passive|misc|block|all|<entitytype>]",
                "Search for groups of entities. Default without an argument is monster."
        );
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        EntitySearch search = new EntitySearch(getPlugin(), sender);
        if(args.length > 0) {
            for(String arg : args) {
                try {
                    search.addSearchedType(EntityType.valueOf(arg.toUpperCase()));
                } catch(IllegalArgumentException notAnEntityType) {
                    if(arg.endsWith("s")) {
                        arg = arg.substring(0, arg.length() - 1);
                    }
                    try {
                        search.setType(SearchType.valueOf(arg.toUpperCase()));
                    } catch(IllegalArgumentException notASearchType) {
                        try {
                            search.setType(SearchType.getByAlias(arg.toUpperCase()));
                        } catch(IllegalArgumentException notAnAlias) {
                            return false;
                        }
                    }
                }
            }
        } else {
            search.setType(SearchType.MONSTER);
        }
        if(!getPlugin().startSearch(search)) {
            sender.sendMessage(ChatColor.YELLOW + search.getOwner() + ChatColor.RED + " already started a search!");
        }
        return true;
    }
}
