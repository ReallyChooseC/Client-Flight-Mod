package cn.choosec.clientflightmod;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.KeyMapping;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

import java.io.File;

@Mod("clientflightmod")
public class ClientFlightMod {
    private static KeyMapping flyKey;
    static final File CONFIG_FILE = new File("config/clientflight.properties");
    static boolean elytraToggle = true;
    static boolean nofallToggle = true;
    static double speed = 1.0;
    static final double BASE_TWEAKEROO = 0.064;
    static final double SCALE_FACTOR = 0.703;
    static final double VERTICAL_RATIO = 0.689;
    static final String TWEAKEROO_CONFIGS = "fi.dy.masa.tweakeroo.config.Configs";
    static final String TWEAKEROO_FEATURES = "fi.dy.masa.tweakeroo.config.FeatureToggle";

    public ClientFlightMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::clientSetup);
        modEventBus.addListener(this::registerKeyBindings);

        MinecraftForge.EVENT_BUS.register(this);
        loadConfig();
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        // 初始化代码可以放在这里
    }

    private void registerKeyBindings(final RegisterKeyMappingsEvent event) {
        flyKey = new KeyMapping(
                "key.clientflightmod.toggleflight",
                GLFW.GLFW_KEY_UNKNOWN,
                "category.clientflightmod.main"
        );
        event.register(flyKey);
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("cfly")
                        .then(Commands.literal("toggle").executes(ctx -> {
                            toggleFlight();
                            return 1;
                        }))
                        .then(Commands.literal("elytratoggle").executes(ctx -> {
                            toggleElytra();
                            return 1;
                        }))
                        .then(Commands.literal("nofalltoggle").executes(ctx -> {
                            toggleNofall();
                            return 1;
                        }))
                        .then(Commands.literal("speed")
                                .then(Commands.argument("value", DoubleArgumentType.doubleArg(0.0))
                                        .executes(ctx -> {
                                            setSpeed(DoubleArgumentType.getDouble(ctx, "value"));
                                            return 1;
                                        }))
                        )
        );
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Minecraft minecraft = Minecraft.getInstance();
            if (flyKey.consumeClick()) toggleFlight();
            handleElytraMovement(minecraft);
            NofallDamage(minecraft);
        }
    }

    private static void toggleFlight() {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;

        LocalPlayer player = client.player;
        boolean state = !player.getAbilities().mayfly;
        player.getAbilities().mayfly = state;
        if (!state) player.getAbilities().flying = false;
        String statusKey = "clientflightmod." + (state ? "enabled" : "disabled");
        Component message = Component.translatable("clientflightmod.fly")
                .append(Component.literal(": "))
                .append(Component.translatable(statusKey));
        sendCustomFeedback(message);
    }

    // 添加这些方法的引用
    private static void toggleElytra() {
        Elytra.toggleElytra();
    }

    private static void toggleNofall() {
        Nofall.toggleNofall();
    }

    private static void setSpeed(double value) {
        Elytra.setSpeed(value);
    }

    private static void sendCustomFeedback(Component message) {
        Feedback.sendCustomFeedback(message);
    }

    private static void handleElytraMovement(Minecraft client) {
        Elytra.handleElytraMovement(client);
    }

    private static void NofallDamage(Minecraft client) {
        Nofall.NofallDamage(client);
    }

    private static void loadConfig() {
        Config.loadConfig();
    }
}