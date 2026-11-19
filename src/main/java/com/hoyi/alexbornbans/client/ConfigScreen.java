package com.hoyi.alexbornbans.client;

import com.hoyi.alexbornbans.Config;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.ArrayList;
import java.util.List;

public final class ConfigScreen extends Screen {
    private record Toggle(String key, ForgeConfigSpec.BooleanValue value) {
    }

    private static final String[] TABS = {"alexsmobs", "iceandfire", "borninchaos", "vanilla"};
    private static final List<List<Toggle>> TOGGLES = List.of(
            List.of(
                    new Toggle("alexsmobs.bear_jumpscare", Config.ALEX_BEAR_JUMPSCARE),
                    new Toggle("alexsmobs.seagull_steal", Config.ALEX_SEAGULL_STEAL),
                    new Toggle("alexsmobs.mosquito_face", Config.ALEX_MOSQUITO_FACE),
                    new Toggle("alexsmobs.mosquito_neutral", Config.ALEX_MOSQUITO_NEUTRAL),
                    new Toggle("alexsmobs.fly_spawn", Config.ALEX_FLY_SPAWN),
                    new Toggle("alexsmobs.straddler_neutral", Config.ALEX_STRADDLER_NEUTRAL),
                    new Toggle("alexsmobs.bone_serpent_neutral", Config.ALEX_BONE_SERPENT_NEUTRAL),
                    new Toggle("alexsmobs.cockroach_spawn", Config.ALEX_COCKROACH_SPAWN),
                    new Toggle("alexsmobs.cockroach_aggro", Config.ALEX_COCKROACH_AGGRO)),
            List.of(
                    new Toggle("iceandfire.dragons_neutral", Config.IAF_DRAGONS_NEUTRAL),
                    new Toggle("iceandfire.pixie_steal", Config.IAF_PIXIE_STEAL),
                    new Toggle("iceandfire.sea_serpent_range", Config.IAF_SEA_SERPENT_RANGE),
                    new Toggle("iceandfire.siren_sight", Config.IAF_SIREN_SIGHT),
                    new Toggle("iceandfire.siren_timeout", Config.IAF_SIREN_TIMEOUT)),
            List.of(
                    new Toggle("borninchaos.infected_ore", Config.BIC_INFECTED_ORE),
                    new Toggle("borninchaos.corpse_fly_spawn", Config.BIC_CORPSE_FLY_SPAWN),
                    new Toggle("borninchaos.maggot_spawn", Config.BIC_MAGGOT_SPAWN),
                    new Toggle("borninchaos.swarmer_spawn", Config.BIC_SWARMER_SPAWN),
                    new Toggle("borninchaos.krampus_steal", Config.BIC_KRAMPUS_STEAL)),
            List.of(
                    new Toggle("vanilla.sleep_kill_phantoms", Config.VANILLA_SLEEP_KILL),
                    new Toggle("vanilla.creeper_explosion_no_blocks", Config.VANILLA_CREEPER_EXPLOSION_NO_BLOCKS)));

    private final Screen parent;
    private final List<AbstractWidget> toggleWidgets = new ArrayList<>();
    private int tab;

    public ConfigScreen(Screen parent) {
        super(Component.translatable("alex_born_bans.config.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int tabWidth = Math.max(72, this.width / TABS.length);
        for (int i = 0; i < TABS.length; i++) {
            final int index = i;
            this.addRenderableWidget(Button.builder(Component.translatable("alex_born_bans.config.tab." + TABS[i]),
                    button -> {
                        this.tab = index;
                        this.rebuildToggles();
                    }).bounds(i * tabWidth, 30, tabWidth - 3, 20).build());
        }
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.onClose())
                .bounds(this.width / 2 - 100, this.height - 28, 200, 20).build());
        this.rebuildToggles();
    }

    private void rebuildToggles() {
        for (AbstractWidget widget : this.toggleWidgets) {
            this.removeWidget(widget);
        }
        this.toggleWidgets.clear();

        List<Toggle> list = TOGGLES.get(this.tab);
        int columns = list.size() > 4 ? 2 : 1;
        int buttonWidth = Math.min(180, (this.width - 16 - (columns - 1) * 8) / columns);
        int totalWidth = columns * buttonWidth + (columns - 1) * 8;
        int startX = this.width / 2 - totalWidth / 2;
        for (int i = 0; i < list.size(); i++) {
            Toggle toggle = list.get(i);
            int x = startX + (i % columns) * (buttonWidth + 8);
            int y = 62 + (i / columns) * 24;
            Button button = Button.builder(label(toggle), b -> {
                toggle.value().set(!toggle.value().get());
                Config.SPEC.save();
                b.setMessage(label(toggle));
            }).bounds(x, y, buttonWidth, 20).build();
            this.toggleWidgets.add(button);
            this.addRenderableWidget(button);
        }
    }

    private static Component label(Toggle toggle) {
        return CommonComponents.optionStatus(Component.translatable("alex_born_bans.config." + toggle.key()), toggle.value().get());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 10, 0xFFFFFF);
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
    }
}
