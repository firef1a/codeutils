package mia.codeutils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import mia.codeutils.config.ConfigStore;
import mia.codeutils.features.FeatureManager;
import mia.codeutils.features.listeners.impl.*;
import mia.codeutils.render.util.ARGB;
import mia.codeutils.render.util.HudMatrixRegistry;
import mia.codeutils.render.util.RenderContextHelper;
import net.fabricmc.api.ClientModInitializer;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class Mod implements ClientModInitializer {
	public static final String MOD_ID = "codeutils";
	public static final String MOD_NAME = "CodeUtils";

	private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final Minecraft MC = Minecraft.getInstance();
	public static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
	public static int tick = 0;

	@Override
	public void onInitializeClient() {
		log("");
		log("codeutils client initializer loaded...");
		System.setProperty("java.awt.headless", "false");


		ConfigStore.load();
		FeatureManager.init();
		Mod.registerCallbacks();
		FeatureManager.getFeaturesByIdentifier(RegisterKeyBindEvent.class).forEach(RegisterKeyBindEvent::registerKeyBind);

		log("codeutils initialization complete! :3");
		log("""
                
                                 .___             __  .__.__         \s
                  ____  ____   __| _/____  __ ___/  |_|__|  |   ______
                _/ ___\\/  _ \\ / __ |/ __ \\|  |  \\   __\\  |  |  /  ___/
                \\  \\__(  <_> ) /_/ \\  ___/|  |  /|  | |  |  |__\\___ \\\s
                 \\___  >____/\\____ |\\___  >____/ |__| |__|____/____  >
                     \\/           \\/    \\/                         \\/\s
                """);
	}


	public static void shutdownClient() {
		ConfigStore.save();
		log("shutting down client");
	}

	private static void registerCallbacks() {
		log("registering callbacks...");


		WorldRenderEvents.END_MAIN.register(context -> {
			HudMatrixRegistry.register(context);
			FeatureManager.implementFeatureListener(WorldRenderEventListener.class, feature -> { feature.WorldRenderEvents_END_MAIN(context); });
		});
		WorldRenderEvents.BEFORE_TRANSLUCENT.register(context -> {FeatureManager.implementFeatureListener(WorldRenderEventListener.class, feature -> { feature.WorldRenderEvents_BEFORE_TRANSLUCENT(context); });});

		ClientTickEvents.START_CLIENT_TICK.register(client -> FeatureManager.implementFeatureListener(TickEvent.class, feature -> { feature.tickR(tick); }));
		ClientTickEvents.END_CLIENT_TICK.register(client -> FeatureManager.implementFeatureListener(TickEvent.class, feature -> {
			tick++;
			feature.tickF(tick);
		}));
		ClientLifecycleEvents.CLIENT_STARTED.register(client -> FeatureManager.implementFeatureListener(ClientEventListener.class, ClientEventListener::clientInitialize));
		ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {FeatureManager.implementFeatureListener(ClientEventListener.class, ClientEventListener::clientShutdown); shutdownClient();});
		ItemTooltipCallback.EVENT.register(((itemStack, tooltipContext, tooltipType, list) -> FeatureManager.implementFeatureListener(RenderTooltip.class, feature -> feature.tooltip(itemStack, tooltipContext, tooltipType, list))));

		HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT, Identifier.fromNamespaceAndPath(MOD_ID,"before_chat"), (context, tickDelta) -> {
			HudMatrixRegistry.setRenderHUDTickCounter(tickDelta);
			FeatureManager.implementFeatureListener(RenderHUD.class, feature -> feature.renderHUD(context, tickDelta));
		});
		ClientPlayConnectionEvents.INIT.register((handler, client) -> FeatureManager.implementFeatureListener(ServerConnectionEventListener.class, feature -> feature.serverConnectInit(handler, client)));
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> FeatureManager.implementFeatureListener(ServerConnectionEventListener.class, feature -> feature.serverConnectJoin(handler, sender, client)));
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> FeatureManager.implementFeatureListener(ServerConnectionEventListener.class, feature -> feature.serverConnectDisconnect(handler, client)));
		UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
			FeatureManager.implementFeatureListener(PlayerUseEventListener.class, feature -> feature.useBlockCallback(player, world, hand, hitResult));
			return InteractionResult.PASS;
		});
		UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
			FeatureManager.implementFeatureListener(PlayerUseEventListener.class, feature -> feature.useEntityCallback(player, world, hand, entity, hitResult));
			return InteractionResult.PASS;
		});
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> FeatureManager.implementFeatureListener(RegisterCommandListener.class, feature -> feature.register(dispatcher, registryAccess)));

		log("callback registering complete!");
	}

	public static void sendCommand(String command) {
		if (command.charAt(0) == '/') {
			if (Mod.MC.getConnection() != null) {
				Mod.MC.getConnection().sendCommand(command.substring(1));
			}
		}
	}

	private static void message(Component message, int mia, int main) {
		if (Mod.MC.player == null) return;
		Mod.MC.player.displayClientMessage(Component.empty()
						.append(Component.literal(MOD_ID + " ").withColor(mia))
						.append(Component.literal("᛬ ").withColor(0x9c9c9c))
						.append(message.copy().withColor(main))
				, false);
	}

	public static void message(Component message) {
		message(message, ColorBank.MIA_PURPLE, ColorBank.WHITE);
	}

	public static void message(String message) {
		message(Component.literal(message), ColorBank.MIA_PURPLE, ColorBank.WHITE);
	}


	public static void messageError(Component message) {
		message(message, 0xff695c, 0xff6052);
	}
	public static void messageError(String message) {
		messageError(Component.literal(message));
	}


	public static Screen getCurrentScreen() { return Mod.MC.screen; }
	public static void setCurrentScreen(Screen screen) { Mod.MC.setScreen(screen); }
	public static int getScaledWindowWidth() {
		return Mod.MC.getWindow().getGuiScaledWidth();
	}
	public static int getScaledWindowHeight() {
		return Mod.MC.getWindow().getGuiScaledHeight();
	}

	public static String getModVersion() { return FabricLoader.getInstance().getModContainer(MOD_ID).isPresent() ? FabricLoader.getInstance().getModContainer(MOD_ID).get().getMetadata().getVersion().getFriendlyString() : null; }
	public static String getPlayerName() { return Mod.MC.getUser().getName(); }
	public static UUID getPlayerUUID() { return Mod.MC.getUser().getProfileId(); }

	public static void log(String msg) { LOGGER.info(msg); }
	public static void warn(String msg) { LOGGER.warn(msg); }
	public static void error(String msg) { LOGGER.error(msg); }
}