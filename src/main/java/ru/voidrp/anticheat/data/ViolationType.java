package ru.voidrp.anticheat.data;

public enum ViolationType {
    SPEED("Speed", 2, "HIGH"),
    FLY("Fly", 3, "HIGH"),
    REACH("Reach", 5, "HIGH"),
    KILLAURA("KillAura", 4, "HIGH"),
    CPS("Cps", 3, "MEDIUM"),
    NO_FALL("NoFall", 5, "MEDIUM"),
    MOD_SUSPICIOUS("SuspiciousMod", 1, "HIGH"),
    CLIENT_UNVERIFIED("UnverifiedClient", 1, "LOW");

    public final String displayName;
    /** How many VL points each trigger adds */
    public final int vlPerTrigger;
    public final String defaultSeverity;

    ViolationType(String displayName, int vlPerTrigger, String defaultSeverity) {
        this.displayName = displayName;
        this.vlPerTrigger = vlPerTrigger;
        this.defaultSeverity = defaultSeverity;
    }
}
