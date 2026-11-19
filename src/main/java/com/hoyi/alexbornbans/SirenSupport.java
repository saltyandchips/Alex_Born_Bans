package com.hoyi.alexbornbans;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Ice and Fire and its Community Edition keep the siren's song state in the entity
 * itself.  Both variants expose the same three members, so a tiny reflection
 * bridge keeps this mod free of a hard dependency on either jar.
 */
public final class SirenSupport {
    private static final String TAG_START = "alex_born_bans_siren_sing_start";
    private static final Map<Class<?>, Method> SINGING_METHODS = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Method> SET_SINGING_METHODS = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Field> COOLDOWN_FIELDS = new ConcurrentHashMap<>();

    private SirenSupport() {
    }

    public static void tick(Mob siren, boolean requireSight, boolean twoMinuteTimeout) {
        if (siren.level().isClientSide) {
            return;
        }
        boolean singing = isActuallySinging(siren);
        Player seen = nearestPlayer(siren, 50.0D, requireSight);
        if (requireSight && seen == null) {
            stop(siren, 20);
            return;
        }
        if (!twoMinuteTimeout) {
            return;
        }

        CompoundTag data = siren.getPersistentData();
        long now = siren.level().getGameTime();
        if (!singing || seen == null) {
            data.remove(TAG_START);
            return;
        }
        Player close = nearestPlayer(siren, 5.0D, false);
        if (close != null) {
            data.remove(TAG_START);
            return;
        }
        if (!data.contains(TAG_START)) {
            data.putLong(TAG_START, now);
        } else if (now - data.getLong(TAG_START) >= 2400L) {
            data.remove(TAG_START);
            stop(siren, 2400);
        }
    }

    private static Player nearestPlayer(Mob siren, double range, boolean requireSight) {
        Player best = null;
        double bestDistance = range * range;
        for (Player player : siren.level().players()) {
            if (player.isCreative() || player.isSpectator()) {
                continue;
            }
            double distance = siren.distanceToSqr(player);
            if (distance > bestDistance) {
                continue;
            }
            if (requireSight && !siren.hasLineOfSight(player)) {
                continue;
            }
            bestDistance = distance;
            best = player;
        }
        return best;
    }

    private static boolean isActuallySinging(Mob siren) {
        Method method = SINGING_METHODS.computeIfAbsent(siren.getClass(), type -> find(type, "isActuallySinging"));
        if (method == null) {
            return false;
        }
        try {
            return Boolean.TRUE.equals(method.invoke(siren));
        } catch (ReflectiveOperationException ignored) {
            return false;
        }
    }

    private static void stop(Mob siren, int cooldown) {
        Field field = COOLDOWN_FIELDS.computeIfAbsent(siren.getClass(), type -> findField(type, "singCooldown"));
        if (field != null) {
            try {
                field.setInt(siren, cooldown);
            } catch (ReflectiveOperationException ignored) {
            }
        }
        Method method = SET_SINGING_METHODS.computeIfAbsent(siren.getClass(), type -> find(type, "setSinging", boolean.class));
        if (method != null) {
            try {
                method.invoke(siren, false);
            } catch (ReflectiveOperationException ignored) {
            }
        }
    }

    private static Method find(Class<?> type, String name, Class<?>... parameters) {
        try {
            Method method = type.getMethod(name, parameters);
            method.setAccessible(true);
            return method;
        } catch (NoSuchMethodException ignored) {
            return null;
        }
    }

    private static Field findField(Class<?> type, String name) {
        try {
            Field field = type.getField(name);
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException ignored) {
            return null;
        }
    }
}
