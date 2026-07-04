package silentbeta;

import java.util.Random;

import betaengine.Textures;
import net.minecraft.src.BlockContainer;
import net.minecraft.src.EntityItem;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Material;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;

/**
 * Obsidian Vault: a creeper-proof 54-slot chest in one block.
 */
public class BlockVault extends BlockContainer {
	private final Random dropRandom = new Random();

	public BlockVault(int blockId) {
		super(blockId, Material.rock);
		this.blockIndexInTexture = Textures.block("obsidian_vault");
		this.setHardness(8.0F);
		this.setResistance(6000.0F);
		this.setStepSound(soundStoneFootstep);
		this.setBlockName("obsidianVault");
	}

	protected TileEntity getBlockEntity() {
		return new TileEntityVault();
	}

	public boolean blockActivated(World world, int x, int y, int z, EntityPlayer player) {
		TileEntity tile = world.getBlockTileEntity(x, y, z);
		if(tile instanceof TileEntityVault) {
			player.displayGUIChest((TileEntityVault)tile);
		}

		return true;
	}

	public void onBlockRemoval(World world, int x, int y, int z) {
		TileEntity tile = world.getBlockTileEntity(x, y, z);
		if(tile instanceof TileEntityVault) {
			TileEntityVault vault = (TileEntityVault)tile;

			for(int slot = 0; slot < vault.getSizeInventory(); ++slot) {
				ItemStack stack = vault.getStackInSlot(slot);
				if(stack == null) {
					continue;
				}

				float ox = this.dropRandom.nextFloat() * 0.8F + 0.1F;
				float oy = this.dropRandom.nextFloat() * 0.8F + 0.1F;
				float oz = this.dropRandom.nextFloat() * 0.8F + 0.1F;

				while(stack.stackSize > 0) {
					int count = this.dropRandom.nextInt(21) + 10;
					if(count > stack.stackSize) {
						count = stack.stackSize;
					}

					stack.stackSize -= count;
					EntityItem drop = new EntityItem(world, (double)((float)x + ox), (double)((float)y + oy), (double)((float)z + oz), new ItemStack(stack.itemID, count, stack.getItemDamage()));
					drop.motionX = (double)((float)this.dropRandom.nextGaussian() * 0.05F);
					drop.motionY = (double)((float)this.dropRandom.nextGaussian() * 0.05F + 0.2F);
					drop.motionZ = (double)((float)this.dropRandom.nextGaussian() * 0.05F);
					world.entityJoinedWorld(drop);
				}
			}
		}

		super.onBlockRemoval(world, x, y, z);
	}
}
