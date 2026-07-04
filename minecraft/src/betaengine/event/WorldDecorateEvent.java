package betaengine.event;

import java.util.Random;

import net.minecraft.src.World;

/**
 * Posted from ChunkProviderGenerate.populate() after vanilla decoration of a
 * chunk, so mods can add their own worldgen (ore veins, plants, structures).
 * Block coordinates of the decorated area start at chunkX*16 / chunkZ*16.
 */
public class WorldDecorateEvent extends Event {
	public final World world;
	public final int chunkX;
	public final int chunkZ;
	public final Random random;

	public WorldDecorateEvent(World world, int chunkX, int chunkZ, Random random) {
		this.world = world;
		this.chunkX = chunkX;
		this.chunkZ = chunkZ;
		this.random = random;
	}
}
