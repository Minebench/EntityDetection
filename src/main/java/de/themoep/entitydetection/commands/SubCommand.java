package de.themoep.entitydetection.commands;

import de.themoep.entitydetection.EntityDetection;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

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
public abstract class SubCommand {
    private final EntityDetection plugin;
    private final String command;
    private final String path;
    private final Permission permission;
    private final String arguments;
    private final String help;

    public SubCommand(EntityDetection plugin, String command, String path, String arguments, String help) {
        this.plugin = plugin;
        this.command = command;
        this.path = path;
        this.permission = new Permission(
                plugin.getName().toLowerCase() + ".command." + getPath().replace(' ', '.').toLowerCase(),
                "Get access to the /" + getCommand() + " " + getPath() + " subcommand.",
                PermissionDefault.OP
        );
        this.arguments = arguments;
        this.help = help;
    }

    public abstract boolean execute(CommandSender sender, String[] args);

    public String getCommand() {
        return command;
    }

    public String getPath() {
        return path;
    }

    public Permission getPermission() {
        return permission;
    }

    /**
     * Get the help text
     * @return The help text
     */
    public String getHelp() {
        return help;
    }

    public EntityDetection getPlugin() {
        return plugin;
    }

    /**
     * Get the usage of this command containing command, path and arguments
     * @return The usage in the pattern of "/command sub path [arguments...]"
     */
    public String getUsage() {
        return getUsage(getCommand());
    }

    /**
     * Get the usage of this command containing command, path and arguments
     * @param label The name that should be displayed as the command
     * @return The usage in the pattern of "/label sub path [arguments...]"
     */
    public String getUsage(String label) {
        return "/" + label + " " + getPath() + " " + arguments;
    }
}
