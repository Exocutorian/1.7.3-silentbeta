package betaengine;

/**
 * Side-neutral facade over the client texture stitcher.
 *
 * On the client, betaengine.client.ClientResources installs an allocator
 * before BetaEngine.init(), so block/item constructors can ask for texture
 * slots. On the dedicated server there is no renderer and no allocator — the
 * calls return 0, which is harmless because texture indices are never saved
 * or sent over the network.
 *
 * <pre>
 * this.blockIndexInTexture = Textures.block("copper_block");
 * // resolves betaengine/textures/blocks/copper_block.png into a free
 * // terrain.png cell allocated at startup
 * </pre>
 */
public final class Textures {
	public interface Allocator {
		int blockTexture(String name);

		int itemTexture(String name);
	}

	private static Allocator allocator = null;

	private Textures() {
	}

	public static void setAllocator(Allocator newAllocator) {
		allocator = newAllocator;
	}

	/** Atlas index in /terrain.png for betaengine/textures/blocks/&lt;name&gt;.png */
	public static int block(String name) {
		return allocator == null ? 0 : allocator.blockTexture(name);
	}

	/** Atlas index in /gui/items.png for betaengine/textures/items/&lt;name&gt;.png */
	public static int item(String name) {
		return allocator == null ? 0 : allocator.itemTexture(name);
	}
}
