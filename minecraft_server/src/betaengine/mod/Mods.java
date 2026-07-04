package betaengine.mod;

import java.util.ArrayList;
import java.util.List;

/**
 * Keeps the list of loaded mods and runs their init in registration order.
 */
public final class Mods {
	private static final List MODS = new ArrayList();
	private static boolean initialized = false;

	private Mods() {
	}

	public static void register(Mod mod) {
		if(initialized) {
			throw new IllegalStateException("Mods are already initialized; register before BetaEngine.init() finishes");
		}

		for(int i = 0; i < MODS.size(); ++i) {
			if(((Mod)MODS.get(i)).id().equals(mod.id())) {
				throw new IllegalArgumentException("Duplicate mod id: " + mod.id());
			}
		}

		MODS.add(mod);
	}

	public static void initAll() {
		initialized = true;

		for(int i = 0; i < MODS.size(); ++i) {
			Mod mod = (Mod)MODS.get(i);
			System.out.println("[BetaEngine] Initializing mod \"" + mod.id() + "\"");
			mod.init();
		}
	}

	public static int count() {
		return MODS.size();
	}
}
