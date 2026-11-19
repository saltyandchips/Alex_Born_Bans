package com.hoyi.alexbornbans;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLEnvironment;

import com.hoyi.alexbornbans.client.ClientSetup;

@Mod(AlexBornBans.MODID)
public final class AlexBornBans {
    public static final String MODID = "alex_born_bans";

    public AlexBornBans() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ClientSetup.registerConfigScreen();
        }
    }
}
