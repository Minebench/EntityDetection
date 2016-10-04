package de.themoep.entitydetection;

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
public class Utils {

    /**
     * Capitalize a string
     * @param string The string to capitalize
     * @return The capitalized string (first letter uppercase, every other letter lowercase)
     */
    public static String capitalize(String string) {
        return string.substring(0, 1).toUpperCase() + string.substring(1).toLowerCase();
    }

    /**
     * Converts an uppercase enum to a human readable string
     * @param convert The Enum to convert
     * @return The converted name; capitalizes each word and replaces underscores with spaces
     */
    public static String enumToHumanName(Enum convert) {
        return enumToHumanName(convert.toString());
    }

    /**
     * Converts an uppercase enum to a human readable string
     * @param convert The name of the Enum to convert
     * @return The converted name; capitalizes each word and replaces underscores with spaces
     */
    public static String enumToHumanName(String convert) {
        String[] parts = convert.split("_");
        if(parts.length == 0) {
            return "";
        }
        String human = capitalize(parts[0]);
        for(int i = 1; i < parts.length; i++) {
            human += " " + capitalize(parts[i]);
        }
        return human;
    }
}
