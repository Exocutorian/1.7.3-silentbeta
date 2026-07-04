package betaengine.event;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.World;

/**
 * Posted right before a player finishes breaking a block, on both sides
 * (client: PlayerControllerSP, server: ItemInWorldManager). Cancelling keeps
 * the block in the world.
 */
public class BlockBreakEvent extends Event {
	public final World world;
	public final EntityPlayer player;
	public final int x;
	public final int y;
	public final int z;
	public final int blockId;
	public final int metadata;

	public BlockBreakEvent(World world, EntityPlayer player, int x, int y, int z, int blockId, int metadata) {
		this.world = world;
		this.player = player;
		this.x = x;
		this.y = y;
		this.z = z;
		this.blockId = blockId;
		this.metadata = metadata;
	}

	public boolean isCancellable() {
		return true;
	}
}
