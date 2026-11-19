package com.hoyi.alexbornbans;

import com.hoyi.alexbornbans.client.ClientSetup;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;

@Mod(AlexBornBans.MODID)
public final class AlexBornBans {
    public static final String MODID = "alex_born_bans";

    public AlexBornBans(ModContainer container) {
        container.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        if (FMLEnvironment.dist.isClient()) {
            ClientSetup.registerConfigScreen(container);
        }
    }
}
