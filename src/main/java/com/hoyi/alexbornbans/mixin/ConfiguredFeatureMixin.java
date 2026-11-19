package com.hoyi.alexbornbans.mixin;

import com.hoyi.alexbornbans.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ConfiguredFeature.class)
public abstract class ConfiguredFeatureMixin {
    private static final ClassValue<Boolean> INFECTED_ORE = new ClassValue<>() {
        @Override
        protected Boolean computeValue(Class<?> type) {
            String name = type.getName();
            return name.startsWith("net.mcreator.borninchaosv.world.features.Infected") && name.contains("Ore");
        }
    };

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Redirect(method = "place", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/levelgen/feature/Feature;place(Lnet/minecraft/world/level/levelgen/feature/configurations/FeatureConfiguration;Lnet/minecraft/world/level/WorldGenLevel;Lnet/minecraft/world/level/chunk/ChunkGenerator;Lnet/minecraft/util/RandomSource;Lnet/minecraft/core/BlockPos;)Z"))
    private boolean alexbornBans$cancelInfectedOre(Feature feature, FeatureConfiguration config,
                                                   WorldGenLevel level, ChunkGenerator generator,
                                                   RandomSource random, BlockPos pos) {
        if (Config.BIC_INFECTED_ORE.get() && INFECTED_ORE.get(feature.getClass())) {
            return false;
        }
        return feature.place(config, level, generator, random, pos);
    }
}
