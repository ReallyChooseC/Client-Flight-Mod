package cn.choosec.clientflightmod;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ConfigScreen {
    public static Screen createConfigScreen(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.translatable("config.clientflightmod.title"))
                .setSavingRunnable(Config::saveConfig);

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        ConfigCategory general = builder.getOrCreateCategory(Component.translatable("category.clientflightmod.general"));

        general.addEntry(entryBuilder.startBooleanToggle(
                        Component.translatable("clientflightmod.elytra_toggle"),
                        ClientFlightMod.elytraToggle)
                .setDefaultValue(true)
                .setTooltip(Component.translatable("config.clientflightmod.elytra_toggle.tooltip"))
                .setSaveConsumer(value -> ClientFlightMod.elytraToggle = value)
                .build());

        general.addEntry(entryBuilder.startBooleanToggle(
                        Component.translatable("clientflightmod.nofall_toggle"),
                        ClientFlightMod.nofallToggle)
                .setDefaultValue(true)
                .setTooltip(Component.translatable("config.clientflightmod.nofall_toggle.tooltip"))
                .setSaveConsumer(value -> ClientFlightMod.nofallToggle = value)
                .build());

        general.addEntry(entryBuilder.startBooleanToggle(
                        Component.translatable("clientflightmod.forceflight_toggle"),
                        ClientFlightMod.forceflightToggle)
                .setDefaultValue(false)
                .setTooltip(Component.translatable("config.clientflightmod.forceflight_toggle.tooltip"))
                .setSaveConsumer(value -> ClientFlightMod.forceflightToggle = value)
                .build());

        general.addEntry(entryBuilder.startDoubleField(
                        Component.translatable("config.clientflightmod.speed"),
                        ClientFlightMod.speed)
                .setDefaultValue(1.0)
                .setMin(0.0)
                .setMax(10.0)
                .setTooltip(Component.translatable("config.clientflightmod.speed.tooltip"))
                .setSaveConsumer(value -> ClientFlightMod.speed = value)
                .build());

        return builder.build();
    }
}