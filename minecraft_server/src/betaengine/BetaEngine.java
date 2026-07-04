package betaengine;

import betaengine.event.EngineInitEvent;
import betaengine.event.EventBus;
import betaengine.example.ExampleMod;
import betaengine.mod.Mods;

/**
 * Entry point of the BetaEngine modding framework for Beta 1.7.3.
 *
 * The game calls {@link #init()} once during startup (client: Minecraft.startGame,
 * server: MinecraftServer.startServer). Everything else — block/item registration,
 * event subscriptions — happens from mod init methods triggered here.
 */
public final class BetaEngine {
	public static final String VERSION = "0.2.0";

	private static boolean initialized = false;
	private static boolean client = false;

	private BetaEngine() {
	}

	public static void init() {
		if(initialized) {
			return;
		}

		initialized = true;
		client = detectClient();
		log("BetaEngine " + VERSION + " starting on " + (client ? "client" : "server"));

		Mods.register(new ExampleMod());
		Mods.initAll();

		EventBus.post(new EngineInitEvent());
		log("Init done: " + Registry.blockCount() + " engine block(s), " + Registry.itemCount() + " engine item(s), " + Mods.count() + " mod(s)");
	}

	public static boolean isClient() {
		return client;
	}

	public static boolean isInitialized() {
		return initialized;
	}

	public static void log(String message) {
		System.out.println("[BetaEngine] " + message);
	}

	private static boolean detectClient() {
		try {
			Class.forName("net.minecraft.client.Minecraft", false, BetaEngine.class.getClassLoader());
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}
}
