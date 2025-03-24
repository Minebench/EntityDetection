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
import de.themoep.minedown.adventure.MineDown;
import de.themoep.minedown.adventure.Replacer;
import de.themoep.utils.lang.bukkit.LanguageManager;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.text.SimpleDateFormat;
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
    private LanguageManager lang;

    private EntitySearch currentSearch;

    private Map<SearchType, SearchResult<?>> results = new HashMap<>();
    private Map<String, SearchResult<?>> customResults = new HashMap<>();
    private Map<String, SearchResult<?>> lastResultViewed = new HashMap<>();

    public void onEnable() {
        lang = new LanguageManager(this, System.getProperty("de.themoep.entitydetection.default-language", "en"));
        PluginCommandExecutor cmdEx = new PluginCommandExecutor(this);
        cmdEx.register(new SearchSubCommand(this));
        cmdEx.register(new TpSubCommand(this));
        cmdEx.register(new ListSubCommand(this));
        cmdEx.register(new StopSubCommand(this));
    }

    public String getRawMessage(CommandSender sender, String key, String... replacements) {
        return lang.getConfig(sender).get(key, replacements);
    }

    public Component getMessage(CommandSender sender, String key, String... replacements) {
        return MineDown.parse(getRawMessage(sender, key, replacements));
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
        clearCurrentSearch();
        return true;
    }

    public void addResult(SearchResult<?> result) {
        if(result.getType() == SearchType.CUSTOM && result.getSearched().size() == 1) {
            Set<String> searchedEntities = result.getSearched();
            customResults.put(searchedEntities.toArray(new String[searchedEntities.size()])[0], result);
        } else {
            results.put(result.getType(), result);
        }
    }

    public void clearCurrentSearch() {
        currentSearch = null;
    }

    public EntitySearch getCurrentSearch() {
        return currentSearch;
    }

    public void send(CommandSender sender, SearchResult<?> result) {
        send(sender, result, 0);
    }


    public void send(CommandSender sender, SearchResult<?> result, int page) {
        lastResultViewed.put(sender.getName(), result);

        String dateStr = new SimpleDateFormat(getRawMessage(sender, "result.time-format")).format(new Date(result.getEndTime()));

        int start = page * 10;
        Component searchedTypes = getMessage(sender, "result.searched-types.head");
        Iterator<String> typeIter = result.getSearched().iterator();
        while (typeIter.hasNext()) {
            searchedTypes = searchedTypes.append(Component.newline())
                    .append(getMessage(sender, "result.searched-types.entry", "type", Utils.enumToHumanName(typeIter.next())));
        }

        Component message = getMessage(sender, "result.head", "type", Utils.enumToHumanName(result.getType()), "timestamp", dateStr);
        message = Replacer.replaceIn(message, "searchedtypes", searchedTypes);

        List<? extends SearchResultEntry<?>> results = result.getSortedEntries();
        if (results.size() > 0) {
            for (int line = start; line < start + 10 && line < results.size(); line++) {
                SearchResultEntry<?> entry = results.get(line);

                Component resultLine = getMessage(sender, "result.entry",
                        "line", String.valueOf(line + 1),
                        "location", String.valueOf(entry.getLocation()),
                        "size", String.valueOf(entry.getSize())
                );

                Component entityCounts = Component.empty();
                int entitiesListed = 0;
                for(Entry<String, Integer> entityEntry : entry.getEntryCount()) {
                    entityCounts = entityCounts.append(getMessage(sender, "result.entity-count",
                            "type", Utils.enumToHumanName(entityEntry.getKey()),
                            "count", String.valueOf(entityEntry.getValue())
                    ));

                    entitiesListed++;
                    if(entitiesListed >= 3)
                        break;
                }

                resultLine = Replacer.replaceIn(resultLine, "entitycounts", entityCounts);

                message = message.append(Component.newline()).append(resultLine);
            }
        } else {
            message = message.append(Component.newline()).append(getMessage(sender, "result.no-entries"));
        }

        sender.sendMessage(message);
    }

    public SearchResult<?> getResult(CommandSender sender) {
        return lastResultViewed.get(sender.getName());
    }

    public SearchResult<?> getResult(String type) {
        return customResults.get(type);
    }

    public SearchResult<?> getResult(SearchType type) {
        return results.get(type);
    }
}
