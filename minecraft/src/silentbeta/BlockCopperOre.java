package silentbeta;

import betaengine.Textures;
import net.minecraft.src.BlockOre;

/** Drops itself; smelts into a copper ingot. */
public class BlockCopperOre extends BlockOre {
	public BlockCopperOre(int blockId) {
		super(blockId, 0);
		this.blockIndexInTexture = Textures.block("copper_ore");
		this.setHardness(3.0F);
		this.setResistance(5.0F);
		this.setStepSound(soundStoneFootstep);
		this.setBlockName("oreCopper");
	}
}
