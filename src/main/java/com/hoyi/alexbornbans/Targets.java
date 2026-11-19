package com.hoyi.alexbornbans;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Set;

public final class Targets {
    public static final ResourceLocation ALEX_BEAR = rl("alexsmobs:grizzly_bear");
    public static final ResourceLocation ALEX_SEAGULL = rl("alexsmobs:seagull");
    public static final ResourceLocation ALEX_MOSQUITO = rl("alexsmobs:crimson_mosquito");
    public static final ResourceLocation ALEX_FLY = rl("alexsmobs:fly");
    public static final ResourceLocation ALEX_STRADDLER = rl("alexsmobs:straddler");
    public static final ResourceLocation ALEX_BONE_SERPENT = rl("alexsmobs:bone_serpent");
    public static final ResourceLocation ALEX_COCKROACH = rl("alexsmobs:cockroach");

    public static final ResourceLocation IAF_FIRE_DRAGON = rl("iceandfire:fire_dragon");
    public static final ResourceLocation IAF_ICE_DRAGON = rl("iceandfire:ice_dragon");
    public static final ResourceLocation IAF_LIGHTNING_DRAGON = rl("iceandfire:lightning_dragon");
    public static final ResourceLocation IAF_PIXIE = rl("iceandfire:pixie");
    public static final ResourceLocation IAF_SEA_SERPENT = rl("iceandfire:sea_serpent");
    public static final ResourceLocation IAF_SIREN = rl("iceandfire:siren");

    public static final ResourceLocation BIC_CORPSE_FLY = rl("born_in_chaos_v1:corpse_fly");
    public static final ResourceLocation BIC_MAGGOT = rl("born_in_chaos_v1:maggot");
    public static final ResourceLocation BIC_SWARMER = rl("born_in_chaos_v1:swarmer");
    public static final ResourceLocation BIC_KRAMPUS = rl("born_in_chaos_v1:krampus");
    public static final ResourceLocation BIC_KRAMPUS_HENCHMAN = rl("born_in_chaos_v1:krampushenchman");
    public static final ResourceLocation BIC_LOST_ITEMS = rl("born_in_chaos_v1:lost_items");

    public static final String PROVOKE_TIME = "alex_born_bans_provoke_time";
    public static final String PROVOKE_PLAYER = "alex_born_bans_provoke_player";
    public static final String SEA_SERPENT_RANGE_FLAG = "alex_born_bans_sea_serpent_range_halved";

    private static EntityType<?> sirenType;
    private static boolean sirenTypeLookedUp;

    private Targets() {
    }

    public static EntityType<?> sirenType() {
        if (!sirenTypeLookedUp) {
            sirenType = ForgeRegistries.ENTITY_TYPES.getValue(IAF_SIREN);
            sirenTypeLookedUp = true;
        }
        return sirenType;
    }

    private static ResourceLocation rl(String s) {
        return new ResourceLocation(s);
    }

    public static ResourceLocation id(Entity entity) {
        return ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
    }

    public static boolean is(Entity entity, ResourceLocation id) {
        return id.equals(id(entity));
    }

    public static boolean isDragon(ResourceLocation id) {
        return IAF_FIRE_DRAGON.equals(id) || IAF_ICE_DRAGON.equals(id) || IAF_LIGHTNING_DRAGON.equals(id);
    }

    public static boolean isNeutralSwitchOn(ResourceLocation id) {
        if (ALEX_STRADDLER.equals(id)) {
            return Config.ALEX_STRADDLER_NEUTRAL.get();
        }
        if (ALEX_BONE_SERPENT.equals(id)) {
            return Config.ALEX_BONE_SERPENT_NEUTRAL.get();
        }
        if (isDragon(id)) {
            return Config.IAF_DRAGONS_NEUTRAL.get();
        }
        return false;
    }

    public static void rememberProvocation(Mob mob, Player player) {
        if (mob.level().isClientSide) {
            return;
        }
        var data = mob.getPersistentData();
        data.putLong(PROVOKE_TIME, mob.level().getGameTime());
        data.putUUID(PROVOKE_PLAYER, player.getUUID());
    }

    public static boolean wasProvokedBy(Mob mob, Player player) {
        var data = mob.getPersistentData();
        if (!data.contains(PROVOKE_PLAYER) || !data.contains(PROVOKE_TIME)) {
            return false;
        }
        if (mob.level().getGameTime() - data.getLong(PROVOKE_TIME) > 600L) {
            return false;
        }
        return player.getUUID().equals(data.getUUID(PROVOKE_PLAYER));
    }

    public static void removeGoals(Mob mob, String... goalNameParts) {
        removeGoal(mob.goalSelector.getAvailableGoals(), goalNameParts);
        removeGoal(mob.targetSelector.getAvailableGoals(), goalNameParts);
    }

    private static void removeGoal(Set<WrappedGoal> goals, String... goalNameParts) {
        goals.removeIf(wrapped -> {
            String name = wrapped.getGoal().getClass().getSimpleName();
            for (String part : goalNameParts) {
                if (name.contains(part)) {
                    return true;
                }
            }
            return false;
        });
    }

    public static boolean isHostileMob(Mob mob) {
        return mob.getType().getCategory() == net.minecraft.world.entity.MobCategory.MONSTER
                || mob instanceof net.minecraft.world.entity.monster.Monster;
    }

    public static boolean isCreeperLike(Entity entity) {
        if (entity instanceof net.minecraft.world.entity.monster.Creeper) {
            return true;
        }
        ResourceLocation key = id(entity);
        return key != null && key.getPath().contains("creeper");
    }
}
