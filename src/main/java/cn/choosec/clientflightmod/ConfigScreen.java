package cn.choosec.clientflightmod;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class ConfigScreen {
    public static Screen createConfigScreen(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.translatable("config.clientflightmod.title"))
                .setSavingRunnable(Config::saveConfig);

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        ConfigCategory general = builder.getOrCreateCategory(Text.translatable("category.clientflightmod.general"));

        general.addEntry(entryBuilder.startBooleanToggle(
                        Text.translatable("clientflightmod.elytra_toggle"),
                        ClientFlightMod.elytraToggle)
                .setDefaultValue(true)
                .setTooltip(Text.translatable("config.clientflightmod.elytra_toggle.tooltip"))
                .setSaveConsumer(value -> ClientFlightMod.elytraToggle = value)
                .build());

        general.addEntry(entryBuilder.startBooleanToggle(
                        Text.translatable("clientflightmod.nofall_toggle"),
                        ClientFlightMod.nofallToggle)
                .setDefaultValue(true)
                .setTooltip(Text.translatable("config.clientflightmod.nofall_toggle.tooltip"))
                .setSaveConsumer(value -> ClientFlightMod.nofallToggle = value)
                .build());

        general.addEntry(entryBuilder.startBooleanToggle(
                        Text.translatable("clientflightmod.forceflight_toggle"),
                        ClientFlightMod.forceflightToggle)
                .setDefaultValue(false)
                .setTooltip(Text.translatable("config.clientflightmod.forceflight_toggle.tooltip"))
                .setSaveConsumer(value -> ClientFlightMod.forceflightToggle = value)
                .build());

        general.addEntry(entryBuilder.startDoubleField(
                        Text.translatable("config.clientflightmod.speed"),
                        ClientFlightMod.speed)
                .setDefaultValue(1.0)
                .setMin(0.0)
                .setMax(10.0)
                .setTooltip(Text.translatable("config.clientflightmod.speed.tooltip"))
                .setSaveConsumer(value -> ClientFlightMod.speed = value)
                .build());

        return builder.build();
    }
}