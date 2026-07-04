package betaengine.mod;

/**
 * A unit of content built on the engine. For now mods are compiled in and
 * registered from BetaEngine.init(); loading external mods/ folders is on the
 * roadmap and will reuse this same interface.
 */
public interface Mod {
	/** Stable lowercase identifier, e.g. "example". */
	String id();

	/** Register blocks, items and event handlers here. */
	void init();
}
