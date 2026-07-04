package silentbeta;

import betaengine.Textures;
import net.minecraft.src.Block;
import net.minecraft.src.Material;

/** Crafted obsidian: almost as blast-proof, far easier to mine. */
public class BlockObsidianBricks extends Block {
	public BlockObsidianBricks(int blockId) {
		super(blockId, Material.rock);
		this.blockIndexInTexture = Textures.block("obsidian_bricks");
		this.setHardness(8.0F);
		this.setResistance(1200.0F);
		this.setStepSound(soundStoneFootstep);
		this.setBlockName("obsidianBricks");
	}
}
