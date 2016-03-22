package de.themoep.entitydetection;

import de.themoep.entitydetection.commands.ListSubCommand;
import de.themoep.entitydetection.commands.PluginCommandExecutor;
import de.themoep.entitydetection.commands.SearchSubCommand;
import de.themoep.entitydetection.commands.StopSubCommand;
import de.themoep.entitydetection.commands.TpSubCommand;
import de.themoep.entitydetection.searcher.EntitySearch;
import de.themoep.entitydetection.searcher.SearchResult;
import de.themoep.entitydetection.searcher.SearchResultEntry;
import de.themoep.entitydetection.searcher.SearchType;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
public class EntityDetection extends JavaPlugin {

    private EntitySearch currentSearch;

    private Map<SearchType, SearchResult> results = new HashMap<SearchType, SearchResult>();
    private Map<EntityType, SearchResult> customResults = new HashMap<EntityType, SearchResult>();
    private Map<String, SearchResult> lastResultViewed = new HashMap<String, SearchResult>();

    private boolean serverIsSpigot = true;

    public void onEnable() {
        try {
            Bukkit.class.getMethod("spigot");
        } catch (NoSuchMethodException noSpigot) {
            serverIsSpigot = false;
        }
        PluginCommandExecutor cmdEx = new PluginCommandExecutor(this);
        cmdEx.register(new SearchSubCommand(this));
        cmdEx.register(new TpSubCommand(this));
        cmdEx.register(new ListSubCommand(this));
        cmdEx.register(new StopSubCommand(this));
    }

    public boolean startSearch(EntitySearch search) {
        if(currentSearch != null && currentSearch.isRunning()) {
            return false;
        }
        currentSearch = search;
        return search.start() != null;
    }

    public boolean stopSearch(String stopper) {
        if(currentSearch == null || !currentSearch.isRunning()) {
            return false;
        }
        currentSearch.stop(stopper);
        return true;
    }

    public void addResult(SearchResult result) {
        if(result.getType() == SearchType.CUSTOM && result.getSearchedEntities().size() == 1) {
            Set<EntityType> searchedEntities = result.getSearchedEntities();
            customResults.put(searchedEntities.toArray(new EntityType[searchedEntities.size()])[0], result);
        } else {
            results.put(result.getType(), result);
        }
    }

    public EntitySearch getCurrentSearch() {
        return currentSearch;
    }

    public void send(CommandSender sender, SearchResult result) {
        send(sender, result, 0);
    }


    public void send(CommandSender sender, SearchResult result, int page) {
        lastResultViewed.put(sender.getName(), result);

        String dateStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(result.getEndTime()));

        int start = page * 10;
        if(serverIsSpigot && sender instanceof Player) {
            String searchedTypes = ChatColor.YELLOW + "Entity Types:\n";
            Iterator<EntityType> typeIter = result.getSearchedEntities().iterator();
            while(typeIter.hasNext()) {
                searchedTypes += ChatColor.DARK_PURPLE + Utils.enumToHumanName(typeIter.next());
                if(typeIter.hasNext()) {
                    searchedTypes += "\n";
                }
            }

            ComponentBuilder builder = new ComponentBuilder(Utils.enumToHumanName(result.getType()) + " search")
                    .color(net.md_5.bungee.api.ChatColor.GREEN)
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(searchedTypes)))
                    .append("from " + dateStr + ":")
                    .color(net.md_5.bungee.api.ChatColor.WHITE);

            List<SearchResultEntry> results = result.getSortedEntries();
            if(results.size() > 0) {
                for(int line = start; line < start + 10 && line < results.size(); line++) {
                    SearchResultEntry entry = results.get(line);

                    builder.append("\n")
                            .retain(ComponentBuilder.FormatRetention.NONE)
                            .append(" " + (line + 1) + ": ")
                            .color(net.md_5.bungee.api.ChatColor.WHITE)
                            .event(
                                    new HoverEvent(
                                            HoverEvent.Action.SHOW_TEXT,
                                            new ComponentBuilder("Click to teleport to " + (line + 1))
                                                    .color(net.md_5.bungee.api.ChatColor.BLUE)
                                                    .create()
                                    )
                            )
                            .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/detect tp " + (line + 1)))
                            .append(entry.getChunk() + " ")
                            .color(net.md_5.bungee.api.ChatColor.YELLOW)
                            .append(entry.getSize() + " ")
                            .color(net.md_5.bungee.api.ChatColor.RED);

                    int entitiesListed = 0;
                    for(Entry<EntityType, Integer> entityEntry : entry.getEntityCount()) {
                        builder.append(Utils.enumToHumanName(entityEntry.getKey()) + "[")
                                .color(net.md_5.bungee.api.ChatColor.GREEN)
                                .append(entityEntry.getValue().toString())
                                .color(net.md_5.bungee.api.ChatColor.WHITE)
                                .append("] ")
                                .color(net.md_5.bungee.api.ChatColor.GREEN);

                        entitiesListed++;
                        if(entitiesListed >= 3)
                            break;
                    }
                }
            } else {
                builder.append("\nNo entities of that type found!")
                        .color(net.md_5.bungee.api.ChatColor.RED);
            }

            ((Player) sender).spigot().sendMessage(builder.create());
        } else {
            List<String> msg = new ArrayList<String>();
            msg.add(ChatColor.GREEN + Utils.enumToHumanName(result.getType()) + " search " + ChatColor.WHITE + "from " + dateStr + ":");

            List<SearchResultEntry> chunkEntries = result.getSortedEntries();
            if(chunkEntries.size() > 0) {
                for(int line = start; line < start + 10 && line < chunkEntries.size(); line++) {
                    SearchResultEntry chunkEntry = chunkEntries.get(line);

                    String lineText = ChatColor.WHITE + " " + (line + 1) + ": " + ChatColor.YELLOW + chunkEntry.getChunk() + " " + ChatColor.RED + chunkEntry.getSize() + " ";

                    int entitiesListed = 0;
                    for(Entry<EntityType, Integer> entityEntry : chunkEntry.getEntityCount()) {
                        lineText += ChatColor.GREEN + Utils.enumToHumanName(entityEntry.getKey()) + "[" + ChatColor.WHITE + entityEntry.getValue().toString() + ChatColor.GREEN + "] ";
                        entitiesListed++;
                        if(entitiesListed >= 3)
                            break;
                    }

                    msg.add(lineText);
                }
            } else {
                msg.add(ChatColor.RED + "No entities of that type found!");
            }
            sender.sendMessage(msg.toArray(new String[msg.size()]));
        }
    }

    public SearchResult getResult(CommandSender sender) {
        return lastResultViewed.get(sender.getName());
    }

    public SearchResult getResult(EntityType type) {
        return customResults.get(type);
    }

    public SearchResult getResult(SearchType type) {
        return results.get(type);
    }
}
