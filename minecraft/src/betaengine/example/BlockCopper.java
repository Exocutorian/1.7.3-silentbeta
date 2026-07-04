package betaengine.example;

import net.minecraft.src.Block;
import net.minecraft.src.Material;

/**
 * Demo block. Note there is no hardcoded ID: the Registry supplies one.
 * Reuses the iron block texture (atlas index 22) until the engine grows a
 * resource loader that can stitch custom textures into terrain.png.
 */
public class BlockCopper extends Block {
	public BlockCopper(int blockId) {
		super(blockId, 22, Material.rock);
		this.setHardness(3.0F);
		this.setResistance(10.0F);
		this.setStepSound(soundMetalFootstep);
		this.setBlockName("copperBlock");
	}
}
