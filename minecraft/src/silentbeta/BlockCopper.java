package silentbeta;

import betaengine.Textures;
import net.minecraft.src.Block;
import net.minecraft.src.Material;

public class BlockCopper extends Block {
	public BlockCopper(int blockId) {
		super(blockId, Material.iron);
		this.blockIndexInTexture = Textures.block("copper_block");
		this.setHardness(3.0F);
		this.setResistance(10.0F);
		this.setStepSound(soundMetalFootstep);
		this.setBlockName("copperBlock");
	}
}
