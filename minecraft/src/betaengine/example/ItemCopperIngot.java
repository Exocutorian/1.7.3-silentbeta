package betaengine.example;

import betaengine.Textures;
import net.minecraft.src.Item;

/**
 * Demo item showing gui/items.png stitching
 * (betaengine/textures/items/copper_ingot.png).
 */
public class ItemCopperIngot extends Item {
	public ItemCopperIngot(int itemId) {
		super(itemId);
		this.setIconIndex(Textures.item("copper_ingot"));
		this.setItemName("copperIngot");
	}
}
