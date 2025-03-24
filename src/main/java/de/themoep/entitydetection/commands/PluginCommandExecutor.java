package de.themoep.entitydetection.commands;

import de.themoep.entitydetection.EntityDetection;
import de.themoep.minedown.adventure.Replacer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

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
public class PluginCommandExecutor implements CommandExecutor {
    private final EntityDetection plugin;

    private final Map<String, Map<String, SubCommand>> subCommands = new HashMap<>();
    private final Component header;

    public PluginCommandExecutor(EntityDetection plugin) {
        this.plugin = plugin;
        header = Component.text(plugin.getDescription().getAuthors().get(0) + "'s ").color(NamedTextColor.GRAY)
                .append(Component.text(plugin.getName()).color(NamedTextColor.RED))
                .append(Component.text(" v" + plugin.getDescription().getVersion()).color(NamedTextColor.GRAY));
        plugin.getCommand(plugin.getName().toLowerCase()).setExecutor(this);
    }

    public void register(SubCommand sub) {
        if(!subCommands.containsKey(sub.getCommand())) {
            subCommands.put(sub.getCommand(), new LinkedHashMap<String, SubCommand>());
        }
        if(subCommands.get(sub.getCommand()).containsKey(sub.getPath())) {
            throw new IllegalArgumentException("A sub command with the path '" + sub.getPath() + "' is already defined for command '" + sub.getCommand() + "'!");
        }
        subCommands.get(sub.getCommand()).put(sub.getPath(), sub);
        try {
            plugin.getServer().getPluginManager().addPermission(sub.getPermission());
        } catch(IllegalArgumentException ignore) {
            // Permission was already defined correctly in the plugin.yml
        }
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            Component helpText = Component.empty().append(header);
            if (subCommands.containsKey(cmd.getName())) {
                for (SubCommand sub : subCommands.get(cmd.getName()).values()) {
                    if (!sender.hasPermission(sub.getPermission())) {
                        continue;
                    }
                    Component commandHelp = plugin.getMessage(sender, "help.sub-command.usage", "usage", sub.getUsage(label));
                    commandHelp = Replacer.replaceIn(commandHelp,
                            "description", plugin.getMessage(sender, "help." + sub.getPath().replace(' ', '.'))
                    );
                    helpText = helpText.append(Component.newline()).append(commandHelp);
                }
            } else {
                helpText = helpText.append(Component.newline())
                        .append(plugin.getMessage(sender, "help.sub-command.none", "command", cmd.getName()));
            }
            sender.sendMessage(helpText);
            return true;
        }

        SubCommand sub = null;

        int pathPartCount = 1;
        if (subCommands.containsKey(cmd.getName())) {
            String path = args[0];
            while (!subCommands.get(cmd.getName()).containsKey(path) && pathPartCount < args.length) {
                path += " " + args[pathPartCount].toLowerCase();
                pathPartCount++;
            }
            sub = subCommands.get(cmd.getName()).get(path);
        }

        if (sub == null) {
            if (subCommands.containsKey(cmd.getName())) {
                Set<String> subCmdsStr = subCommands.get(cmd.getName()).keySet();
                sender.sendMessage(plugin.getMessage(sender, "help.usage", "usage", "/" + label + " " + Arrays.toString(subCmdsStr.toArray(new String[subCmdsStr.size()]))));
                return true;
            } else {
                return false;
            }
        }

        if (!sender.hasPermission(sub.getPermission())) {
            sender.sendMessage(plugin.getMessage(sender, "error.no-permission",
                    "action", sub.getPath(),
                    "permission", sub.getPermission().getName())
            );
            return true;
        }

        String[] subArgs = new String[]{};
        if (args.length > pathPartCount) {
            subArgs = Arrays.copyOfRange(args, pathPartCount, args.length);
        }
        if (!sub.execute(sender, subArgs)) {
            sender.sendMessage(plugin.getMessage(sender, "help.usage", "usage", sub.getUsage(label)));
        }
        return true;
    }
}
