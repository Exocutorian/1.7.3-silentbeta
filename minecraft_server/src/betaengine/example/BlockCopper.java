package betaengine.example;

import betaengine.Textures;
import net.minecraft.src.Block;
import net.minecraft.src.Material;

/**
 * Demo block. No hardcoded IDs anywhere: the Registry supplies the block ID
 * and the resource loader allocates an atlas cell for
 * betaengine/textures/blocks/copper_block.png.
 */
public class BlockCopper extends Block {
	public BlockCopper(int blockId) {
		super(blockId, Material.rock);
		this.blockIndexInTexture = Textures.block("copper_block");
		this.setHardness(3.0F);
		this.setResistance(10.0F);
		this.setStepSound(soundMetalFootstep);
		this.setBlockName("copperBlock");
	}
}
