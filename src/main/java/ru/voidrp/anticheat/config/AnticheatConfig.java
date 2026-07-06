package ru.voidrp.anticheat.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class AnticheatConfig {

    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.BooleanValue ENABLED;
    public static final ModConfigSpec.ConfigValue<String> BACKEND_URL;
    public static final ModConfigSpec.ConfigValue<String> GAME_AUTH_SECRET;
    public static final ModConfigSpec.ConfigValue<String> SERVER_SLUG;
    public static final ModConfigSpec.IntValue VL_THRESHOLD;

    public static final ModConfigSpec.DoubleValue SPEED_THRESHOLD;
    public static final ModConfigSpec.IntValue FLY_TICKS_THRESHOLD;
    public static final ModConfigSpec.DoubleValue REACH_THRESHOLD;
    public static final ModConfigSpec.IntValue KILLAURA_TARGETS_PER_SECOND;
    public static final ModConfigSpec.IntValue CPS_THRESHOLD;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();

        b.comment("VoidRP Anticheat configuration").push("general");
        ENABLED = b.define("enabled", true);
        BACKEND_URL = b.define("backendUrl", "https://api.void-rp.ru");
        GAME_AUTH_SECRET = b.define("gameAuthSecret", "");
        b.comment("Optional multi-server slug for explicit attribution; backend also resolves by secret. Empty = default server.");
        SERVER_SLUG = b.define("serverSlug", "");
        VL_THRESHOLD = b.defineInRange("vlThreshold", 10, 1, 100);
        b.pop();

        b.push("checks");
        SPEED_THRESHOLD = b.defineInRange("speedThreshold", 0.75, 0.3, 5.0);
        FLY_TICKS_THRESHOLD = b.defineInRange("flyTicksThreshold", 40, 5, 200);
        REACH_THRESHOLD = b.defineInRange("reachThreshold", 6.5, 3.0, 20.0);
        KILLAURA_TARGETS_PER_SECOND = b.defineInRange("killauraTargetsPerSecond", 6, 2, 20);
        CPS_THRESHOLD = b.defineInRange("cpsThreshold", 25, 10, 50);
        b.pop();

        SPEC = b.build();
    }
}
