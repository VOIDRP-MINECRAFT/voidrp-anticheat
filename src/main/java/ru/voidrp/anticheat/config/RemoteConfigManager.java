package ru.voidrp.anticheat.config;

import java.util.Collections;
import java.util.Map;

/**
 * Holds threshold values fetched from the backend admin panel.
 * Falls back to local TOML values when not yet loaded.
 */
public class RemoteConfigManager {

    private static volatile Map<String, Double> values = Collections.emptyMap();
    private static volatile boolean loaded = false;

    public static void onConfigLoaded(Map<String, Double> newValues) {
        values = Map.copyOf(newValues);
        loaded = true;
    }

    /** Returns the remote value for key, or fallback if not loaded or key absent. */
    public static double get(String key, double fallback) {
        if (!loaded) return fallback;
        return values.getOrDefault(key, fallback);
    }

    public static boolean isLoaded() {
        return loaded;
    }
}
