package com.hoyi.alexbornbans;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = AlexBornBans.MODID)
public final class CommonEvents {
    private static final Map<UUID, PendingDrop> PENDING_DROPS = new HashMap<>();

    private CommonEvents() {
    }

    private static final class PendingDrop {
        private final ItemStack stack;
        private final long gameTime;
        private boolean cancelled;

        private PendingDrop(ItemStack stack, long gameTime) {
            this.stack = stack;
            this.gameTime = gameTime;
        }
    }

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) {
            return;
        }
        if (event.getEntity() instanceof ItemEntity item) {
            if (!PENDING_DROPS.isEmpty()) {
                for (Player player : event.getLevel().players()) {
                    PendingDrop pending = PENDING_DROPS.get(player.getUUID());
                    if (pending == null || pending.stack.isEmpty() || item.tickCount != 0
                            || player.distanceToSqr(item) > 9.0D
                            || !ItemStack.matches(item.getItem(), pending.stack)) {
                        continue;
                    }
                    pending.cancelled = true;
                    event.setCanceled(true);
                    return;
                }
            }
            return;
        }
        if (!(event.getEntity() instanceof Mob mob)) {
            return;
        }
        var key = Targets.id(mob);
        if (key == null) {
            return;
        }

        if (Targets.ALEX_BEAR.equals(key) && Config.ALEX_BEAR_JUMPSCARE.get()) {
            Targets.removeGoals(mob, "GrizzlyBearAIAprilFools", "GrizzlyBearAIHalloween", "Freddy");
        }
        if (Targets.ALEX_SEAGULL.equals(key) && Config.ALEX_SEAGULL_STEAL.get()) {
            Targets.removeGoals(mob, "SeagullAISteal");
        }
        if (Targets.IAF_PIXIE.equals(key) && Config.IAF_PIXIE_STEAL.get()) {
            Targets.removeGoals(mob, "PixieAISteal");
        }

        if ((Targets.ALEX_STRADDLER.equals(key) || Targets.ALEX_BONE_SERPENT.equals(key) || Targets.isDragon(key))
                && Targets.isNeutralSwitchOn(key) && mob.getTarget() instanceof Player) {
            mob.setTarget(null);
        }

        if (Targets.ALEX_MOSQUITO.equals(key) && isMosquitoNeutral()) {
            if (mob.getTarget() instanceof Player) {
                mob.setTarget(null);
            }
            if (mob.getVehicle() instanceof Player) {
                mob.stopRiding();
            }
        }

        if (Targets.IAF_SEA_SERPENT.equals(key) && Config.IAF_SEA_SERPENT_RANGE.get()
                && !mob.getPersistentData().getBoolean(Targets.SEA_SERPENT_RANGE_FLAG)) {
            var range = mob.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.FOLLOW_RANGE);
            if (range != null && range.getBaseValue() > 1.0D) {
                range.setBaseValue(range.getBaseValue() * 0.5D);
                mob.getPersistentData().putBoolean(Targets.SEA_SERPENT_RANGE_FLAG, true);
            }
        }

        if (Config.ALEX_COCKROACH_AGGRO.get() && Targets.isHostileMob(mob) && !Targets.ALEX_COCKROACH.equals(key)
                && !hasCockroachGoal(mob)) {
            EntityType<?> cockroach = ForgeRegistries.ENTITY_TYPES.getValue(Targets.ALEX_COCKROACH);
            if (cockroach != null) {
                mob.targetSelector.addGoal(0, new CockroachTargetGoal(mob, cockroach));
            }
        }
    }

    private static boolean hasCockroachGoal(Mob mob) {
        for (var wrapped : mob.targetSelector.getAvailableGoals()) {
            if (wrapped.getGoal() instanceof CockroachTargetGoal) {
                return true;
            }
        }
        return false;
    }

    private static final class CockroachTargetGoal extends NearestAttackableTargetGoal<LivingEntity> {
        private CockroachTargetGoal(Mob mob, EntityType<?> cockroach) {
            super(mob, LivingEntity.class, 10, true, false,
                    target -> target.getType() == cockroach);
        }
    }

    private static boolean isMosquitoNeutral() {
        return Config.ALEX_MOSQUITO_NEUTRAL.get() || Config.ALEX_MOSQUITO_FACE.get();
    }

    @SubscribeEvent
    public static void onChangeTarget(LivingChangeTargetEvent event) {
        if (event.getEntity().level().isClientSide() || !(event.getNewTarget() instanceof Player player)) {
            return;
        }
        LivingEntity entity = event.getEntity();
        if (!(entity instanceof Mob mob)) {
            return;
        }
        var key = Targets.id(mob);
        if (key == null) {
            return;
        }

        if (Targets.ALEX_MOSQUITO.equals(key) && isMosquitoNeutral()) {
            event.setNewTarget(null);
            return;
        }
        if (Targets.isNeutralSwitchOn(key) && !Targets.wasProvokedBy(mob, player)) {
            event.setNewTarget(null);
        }
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide || !(entity instanceof Mob mob)) {
            return;
        }
        if (mob.tickCount % 10 != 0) {
            return;
        }
        EntityType<?> sirenType = Targets.sirenType();
        if (sirenType == null || mob.getType() != sirenType) {
            return;
        }
        boolean sight = Config.IAF_SIREN_SIGHT.get();
        boolean timeout = Config.IAF_SIREN_TIMEOUT.get();
        if (!sight && !timeout) {
            return;
        }
        SirenSupport.tick(mob, sight, timeout);
    }

    @SubscribeEvent
    public static void onHurt(LivingHurtEvent event) {
        if (Config.ALEX_BEAR_JUMPSCARE.get() && "freddy".equals(event.getSource().getMsgId())) {
            event.setCanceled(true);
            return;
        }
        if (event.getEntity().level().isClientSide() || !(event.getEntity() instanceof Mob mob)) {
            return;
        }
        if (!(event.getSource().getEntity() instanceof Player player)) {
            return;
        }
        var key = Targets.id(mob);
        if (key != null && Targets.isNeutralSwitchOn(key)) {
            Targets.rememberProvocation(mob, player);
        }
    }

    @SubscribeEvent
    public static void onFinalizeSpawn(MobSpawnEvent.FinalizeSpawn event) {
        if (event.getLevel().isClientSide()) {
            return;
        }
        MobSpawnType spawnType = event.getSpawnType();
        if (spawnType != MobSpawnType.NATURAL && spawnType != MobSpawnType.CHUNK_GENERATION) {
            return;
        }
        Entity entity = event.getEntity();
        var key = Targets.id(entity);
        if (key == null) {
            return;
        }

        if (isBornInChaosSpawnBlocked(key)) {
            event.setSpawnCancelled(true);
            return;
        }
        boolean fly = Targets.ALEX_FLY.equals(key) && Config.ALEX_FLY_SPAWN.get();
        boolean cockroach = Targets.ALEX_COCKROACH.equals(key) && Config.ALEX_COCKROACH_SPAWN.get();
        if (!fly && !cockroach) {
            return;
        }
        BlockPos pos = BlockPos.containing(event.getX(), event.getY(), event.getZ());
        if (fly) {
            if (!event.getLevel().canSeeSky(pos)) {
                event.setSpawnCancelled(true);
                return;
            }
            if (event.getLevel() instanceof ServerLevel server && nearPlayerRespawn(server, pos)) {
                event.setSpawnCancelled(true);
            }
            return;
        }
        if (!event.getLevel().canSeeSky(pos)
                || event.getLevel().getMaxLocalRawBrightness(pos) != 9
                || event.getLevel().getRandom().nextFloat() >= 0.005F) {
            event.setSpawnCancelled(true);
        }
    }

    private static boolean isBornInChaosSpawnBlocked(net.minecraft.resources.ResourceLocation key) {
        if (Targets.BIC_CORPSE_FLY.equals(key)) {
            return Config.BIC_CORPSE_FLY_SPAWN.get();
        }
        if (Targets.BIC_MAGGOT.equals(key)) {
            return Config.BIC_MAGGOT_SPAWN.get();
        }
        if (Targets.BIC_SWARMER.equals(key)) {
            return Config.BIC_SWARMER_SPAWN.get();
        }
        return false;
    }

    private static boolean nearPlayerRespawn(ServerLevel level, BlockPos pos) {
        for (ServerPlayer player : level.players()) {
            BlockPos respawn = player.getRespawnPosition();
            if (respawn != null && player.getRespawnDimension().equals(level.dimension())
                    && Math.abs(pos.getX() - respawn.getX()) <= 56
                    && Math.abs(pos.getZ() - respawn.getZ()) <= 56) {
                return true;
            }
        }
        BlockPos worldSpawn = level.getSharedSpawnPos();
        return level.dimension().equals(Level.OVERWORLD)
                && Math.abs(pos.getX() - worldSpawn.getX()) <= 56
                && Math.abs(pos.getZ() - worldSpawn.getZ()) <= 56;
    }

    @SubscribeEvent
    public static void onSleep(PlayerSleepInBedEvent event) {
        if (!Config.VANILLA_SLEEP_KILL.get()) {
            return;
        }
        Player player = event.getEntity();
        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }
        ChunkPos chunk = player.chunkPosition();
        double minX = (chunk.x - 1) * 16.0D;
        double minZ = (chunk.z - 1) * 16.0D;
        AABB area = new AABB(minX, level.getMinBuildHeight(), minZ,
                minX + 48.0D, level.getMaxBuildHeight(), minZ + 48.0D);
        for (net.minecraft.world.entity.monster.Phantom phantom
                : level.getEntitiesOfClass(net.minecraft.world.entity.monster.Phantom.class, area)) {
            phantom.kill();
        }
    }

    @SubscribeEvent
    public static void onExplosion(ExplosionEvent.Detonate event) {
        if (Config.VANILLA_CREEPER_EXPLOSION_NO_BLOCKS.get() && !event.getLevel().isClientSide()) {
            Entity source = event.getExplosion().getDirectSourceEntity();
            if (source != null && Targets.isCreeperLike(source)) {
                event.getAffectedBlocks().clear();
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onKrampusAttackPre(LivingAttackEvent event) {
        if (!Config.BIC_KRAMPUS_STEAL.get() || !(event.getEntity() instanceof Player player)
                || !isKrampus(event.getSource().getEntity())) {
            return;
        }
        long now = player.level().getGameTime();
        PENDING_DROPS.entrySet().removeIf(entry -> now - entry.getValue().gameTime > 2L);
        PENDING_DROPS.put(player.getUUID(), new PendingDrop(player.getMainHandItem().copy(), now));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onKrampusAttack(LivingAttackEvent event) {
        if (!Config.BIC_KRAMPUS_STEAL.get() || !(event.getEntity() instanceof Player player)
                || !isKrampus(event.getSource().getEntity())) {
            return;
        }

        PendingDrop pending = PENDING_DROPS.remove(player.getUUID());
        if (pending != null && pending.cancelled && !pending.stack.isEmpty()
                && player.getMainHandItem().isEmpty()) {
            player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, pending.stack.copy());
        }

        MobEffectInstance lostItems = null;
        for (MobEffectInstance instance : player.getActiveEffects()) {
            if (instance.getDuration() <= 40
                    && Targets.BIC_LOST_ITEMS.equals(ForgeRegistries.MOB_EFFECTS.getKey(instance.getEffect()))) {
                lostItems = instance;
                break;
            }
        }
        if (lostItems != null) {
            player.removeEffect(lostItems.getEffect());
        }
    }

    private static boolean isKrampus(Entity entity) {
        if (entity == null) {
            return false;
        }
        var key = Targets.id(entity);
        return Targets.BIC_KRAMPUS.equals(key) || Targets.BIC_KRAMPUS_HENCHMAN.equals(key);
    }
}
