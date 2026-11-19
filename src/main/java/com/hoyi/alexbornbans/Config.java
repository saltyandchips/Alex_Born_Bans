package com.hoyi.alexbornbans;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * All switches live in one COMMON config. The two Ice and Fire variants share
 * the same switches on purpose: players only see one "Ice and Fire" group.
 */
public final class Config {
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.BooleanValue ALEX_BEAR_JUMPSCARE;
    public static final ModConfigSpec.BooleanValue ALEX_SEAGULL_STEAL;
    public static final ModConfigSpec.BooleanValue ALEX_MOSQUITO_FACE;
    public static final ModConfigSpec.BooleanValue ALEX_MOSQUITO_NEUTRAL;
    public static final ModConfigSpec.BooleanValue ALEX_FLY_SPAWN;
    public static final ModConfigSpec.BooleanValue ALEX_STRADDLER_NEUTRAL;
    public static final ModConfigSpec.BooleanValue ALEX_BONE_SERPENT_NEUTRAL;
    public static final ModConfigSpec.BooleanValue ALEX_COCKROACH_SPAWN;
    public static final ModConfigSpec.BooleanValue ALEX_COCKROACH_AGGRO;

    public static final ModConfigSpec.BooleanValue IAF_DRAGONS_NEUTRAL;
    public static final ModConfigSpec.BooleanValue IAF_PIXIE_STEAL;
    public static final ModConfigSpec.BooleanValue IAF_SEA_SERPENT_RANGE;
    public static final ModConfigSpec.BooleanValue IAF_SIREN_SIGHT;
    public static final ModConfigSpec.BooleanValue IAF_SIREN_TIMEOUT;

    public static final ModConfigSpec.BooleanValue BIC_INFECTED_ORE;
    public static final ModConfigSpec.BooleanValue BIC_CORPSE_FLY_SPAWN;
    public static final ModConfigSpec.BooleanValue BIC_MAGGOT_SPAWN;
    public static final ModConfigSpec.BooleanValue BIC_SWARMER_SPAWN;
    public static final ModConfigSpec.BooleanValue BIC_KRAMPUS_STEAL;

    public static final ModConfigSpec.BooleanValue VANILLA_SLEEP_KILL;
    public static final ModConfigSpec.BooleanValue VANILLA_CREEPER_EXPLOSION_NO_BLOCKS;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();

        b.comment("Alex's Mobs related switches. Every switch is silent when the mod is absent.").push("alexsmobs");
        ALEX_BEAR_JUMPSCARE = b.comment("Disable the grizzly bear jumpscare easter egg (April Fools / secret settings).")
                .define("disable_bear_jumpscare", true);
        ALEX_SEAGULL_STEAL = b.comment("Prevent seagulls from stealing food from players.")
                .define("disable_seagull_stealing", true);
        ALEX_MOSQUITO_FACE = b.comment("Prevent crimson mosquitoes from landing on a player's face and drinking blood.")
                .define("disable_mosquito_face_sucking", true);
        ALEX_MOSQUITO_NEUTRAL = b.comment("Make crimson mosquitoes neutral towards players.")
                .define("mosquito_neutral_to_players", true);
        ALEX_FLY_SPAWN = b.comment("Flies no longer spawn without open sky, nor within 7x7 chunks of a player respawn point.")
                .define("restrict_fly_spawning", true);
        ALEX_STRADDLER_NEUTRAL = b.comment("Make straddlers neutral towards players.")
                .define("straddler_neutral_to_players", true);
        ALEX_BONE_SERPENT_NEUTRAL = b.comment("Make bone serpents neutral towards players.")
                .define("bone_serpent_neutral_to_players", true);
        ALEX_COCKROACH_SPAWN = b.comment("Cockroaches only spawn with open sky, light level exactly 9, and a very low chance.")
                .define("rework_cockroach_spawning", true);
        ALEX_COCKROACH_AGGRO = b.comment("Hostile mobs treat cockroaches as a default target.")
                .define("cockroach_attracts_hostile_mobs", true);
        b.pop();

        b.comment("Ice and Fire / Ice and Fire CE related switches. Both variants share these switches.").push("iceandfire");
        IAF_DRAGONS_NEUTRAL = b.comment("Treat every dragon as neutral: they no longer attack players unless provoked.")
                .define("dragons_neutral_to_players", true);
        IAF_PIXIE_STEAL = b.comment("Disable pixie item theft from players.")
                .define("disable_pixie_stealing", true);
        IAF_SEA_SERPENT_RANGE = b.comment("Halve the sea serpent's target search range.")
                .define("halve_sea_serpent_target_range", true);
        IAF_SIREN_SIGHT = b.comment("A siren only uses its charming song while it can see a player.")
                .define("siren_charm_requires_sight", true);
        IAF_SIREN_TIMEOUT = b.comment("Stop a siren's charm attempt after 2 minutes if the player was not pulled to it.")
                .define("siren_charm_two_minute_timeout", true);
        b.pop();

        b.comment("Born in Chaos related switches. Every switch is silent when the mod is absent.").push("borninchaos");
        BIC_INFECTED_ORE = b.comment("Disable infected diamond ore and infected deepslate diamond ore generation.")
                .define("disable_infected_diamond_ore", true);
        BIC_CORPSE_FLY_SPAWN = b.comment("Disable natural corpse fly spawning.")
                .define("disable_corpse_fly_spawn", true);
        BIC_MAGGOT_SPAWN = b.comment("Disable natural maggot spawning.")
                .define("disable_maggot_spawn", true);
        BIC_SWARMER_SPAWN = b.comment("Disable natural swarmer spawning.")
                .define("disable_swarmer_spawn", true);
        BIC_KRAMPUS_STEAL = b.comment("Prevent krampus and krampus henchmen from knocking items out of players' hands.")
                .define("disable_krampus_item_steal", true);
        b.pop();

        b.comment("Vanilla related switches.").push("vanilla");
        VANILLA_SLEEP_KILL = b.comment("When a player lies in a bed, instantly kill phantoms in the 3x3 chunks around the player.")
                .define("sleep_kills_phantoms", true);
        VANILLA_CREEPER_EXPLOSION_NO_BLOCKS = b.comment("Creeper-like explosions keep their damage but never destroy blocks.")
                .define("creeper_explosions_do_not_break_blocks", true);
        b.pop();

        SPEC = b.build();
    }

    private Config() {
    }
}
