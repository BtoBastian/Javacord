package org.javacord.api.entity.user;

public enum UserFlag {

    DISCORD_EMPLOYEE(1 << 0, "Discord Employee"),
    DISCORD_PARTNER(1 << 1, "Discord Partner"),
    HYPESQUAD_EVENTS(1 << 2, "HypeSquad Events"),
    BUG_HUNTER_LEVEL_1(1 << 3, "Bug Hunter Level 1"),
    HOUSE_BRAVERY(1 << 6, "House Bravery"),
    HOUSE_BRILLIANCE(1 << 7, "House Brilliance"),
    HOUSE_BALANCE(1 << 8, "House Bravery"),
    EARLY_SUPPORTER(1 << 9, "Early Supporter"),
    TEAM_USER(1 << 10, "Team User"),
    BUG_HUNTER_LEVEL_2(1 << 14, "Bug Hunter Level 2"),
    VERIFIED_BOT(1 << 16, "Verified Bot"),
    EARLY_VERIFIED_BOT_DEVELOPER(1 << 17, "Early Verified Bot Developer"),
    DISCORD_CERTIFIED_MODERATOR(1 << 18, "Discord Certified Moderator");

    private final int flag;
    private final String description;

    /**
     * Class constructor.
     *
     * @param flag The bitmask of the flag.
     * @param description The description of the flag.
     */
    UserFlag(int flag, String description) {
        this.flag = flag;
        this.description = description;
    }

    /**
     * Gets the integer value of the flag.
     *
     * @return The integer value of the flag.
     */
    public int asInt() {
        return flag;
    }

    /**
     * Gets the description of the flag.
     *
     * @return The description of the flag.
     */
    public String getDescription() {
        return description;
    }
}
