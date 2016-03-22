package de.themoep.entitydetection.commands;

import de.themoep.entitydetection.EntityDetection;
import de.themoep.entitydetection.searcher.EntitySearch;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

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
public class StopSubCommand extends SubCommand {
    public StopSubCommand(EntityDetection plugin) {
        super(plugin, plugin.getName().toLowerCase(), "stop",
                "",
                "Stop the currently running search."
        );
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if(getPlugin().stopSearch(sender.getName())) {
            EntitySearch search = getPlugin().getCurrentSearch();
            sender.sendMessage(ChatColor.YELLOW + "Stopped " + search.getType().toString() + " search by " + search.getOwner() + " after " + search.getDuration() + "s!" );
        } else {
            sender.sendMessage(ChatColor.RED + "There is no search running currently.");
        }
        return true;
    }
}
