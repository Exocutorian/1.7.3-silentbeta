package silentbeta;

import betaengine.Textures;
import net.minecraft.src.EnumToolMaterial;
import net.minecraft.src.ItemSword;

public class ItemCopperSword extends ItemSword {
	public ItemCopperSword(int itemId) {
		super(itemId, EnumToolMaterial.COPPER);
		this.setIconIndex(Textures.item("copper_sword"));
		this.setItemName("swordCopper");
	}
}
