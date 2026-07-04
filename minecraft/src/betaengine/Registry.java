package betaengine;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.src.Block;
import net.minecraft.src.CraftingManager;
import net.minecraft.src.FurnaceRecipes;
import net.minecraft.src.Item;
import net.minecraft.src.ItemBlock;
import net.minecraft.src.ItemStack;
import net.minecraft.src.TileEntity;

/**
 * Central registry for engine-added content.
 *
 * Vanilla constructors demand a numeric ID up front, so registration goes
 * through small factories: the registry finds a free ID, hands it to the
 * factory, and records the result under a stable string name.
 *
 * <pre>
 * Block copper = Registry.registerBlock("copper_block", new Registry.BlockFactory() {
 *     public Block create(int id) {
 *         return new BlockCopper(id);
 *     }
 * });
 * </pre>
 *
 * String names are the identity that mods should rely on; numeric IDs are an
 * allocation detail. (Worlds still save raw numeric IDs — persistent per-world
 * ID maps are on the roadmap.)
 */
public final class Registry {
	/** Vanilla b1.7.3 uses block IDs 1..96; engine blocks start above them. */
	private static final int FIRST_BLOCK_ID = 97;
	/** Item array slots 0..255 mirror block IDs; real items start at 256. */
	private static final int FIRST_ITEM_INDEX = 256;

	private static final Map blocksByName = new HashMap();
	private static final Map blockNames = new HashMap();
	private static final Map itemsByName = new HashMap();
	private static final Map itemNames = new HashMap();

	private Registry() {
	}

	/** Receives a free block ID and must construct the block with exactly that ID. */
	public interface BlockFactory {
		Block create(int blockId);
	}

	/**
	 * Receives the value to pass to the Item(int) constructor
	 * (the constructor adds 256 to produce the shifted index).
	 */
	public interface ItemFactory {
		Item create(int itemId);
	}

	public static Block registerBlock(String name, BlockFactory factory) {
		checkName(name);
		if(blocksByName.containsKey(name)) {
			throw new IllegalArgumentException("Block name already registered: " + name);
		}

		int id = nextFreeBlockId();
		Block block = factory.create(id);
		if(block == null || block.blockID != id) {
			throw new IllegalStateException("Factory for \"" + name + "\" must create its block with the supplied ID " + id);
		}

		if(Item.itemsList[id] == null) {
			Item.itemsList[id] = new ItemBlock(id - 256);
		}

		blocksByName.put(name, block);
		blockNames.put(block, name);
		BetaEngine.log("Registered block \"" + name + "\" -> ID " + id);
		return block;
	}

	public static Item registerItem(String name, ItemFactory factory) {
		checkName(name);
		if(itemsByName.containsKey(name)) {
			throw new IllegalArgumentException("Item name already registered: " + name);
		}

		int index = nextFreeItemIndex();
		Item item = factory.create(index - 256);
		if(item == null || item.shiftedIndex != index) {
			throw new IllegalStateException("Factory for \"" + name + "\" must create its item with the supplied ID " + (index - 256));
		}

		itemsByName.put(name, item);
		itemNames.put(item, name);
		BetaEngine.log("Registered item \"" + name + "\" -> index " + index);
		return item;
	}

	/**
	 * Shaped recipe, vanilla pattern syntax:
	 * addShapedRecipe(new ItemStack(out), new Object[]{"XX", "XX", 'X', in})
	 */
	public static void addShapedRecipe(ItemStack result, Object[] pattern) {
		CraftingManager.getInstance().addRecipe(result, pattern);
	}

	public static void addShapelessRecipe(ItemStack result, Object[] ingredients) {
		CraftingManager.getInstance().addShapelessRecipe(result, ingredients);
	}

	/** inputId is a block ID or item shiftedIndex. */
	public static void addSmelting(int inputId, ItemStack output) {
		FurnaceRecipes.smelting().addSmelting(inputId, output);
	}

	/**
	 * Registers a tile entity class under a save-format id so worlds can
	 * persist it (TileEntity.addMapping is opened up for this).
	 */
	public static void registerTileEntity(Class tileEntityClass, String id) {
		TileEntity.addMapping(tileEntityClass, id);
		BetaEngine.log("Registered tile entity \"" + id + "\"");
	}

	public static Block getBlock(String name) {
		return (Block)blocksByName.get(name);
	}

	public static Item getItem(String name) {
		return (Item)itemsByName.get(name);
	}

	public static String nameOf(Block block) {
		return (String)blockNames.get(block);
	}

	public static String nameOf(Item item) {
		return (String)itemNames.get(item);
	}

	public static Map blocks() {
		return Collections.unmodifiableMap(blocksByName);
	}

	public static Map items() {
		return Collections.unmodifiableMap(itemsByName);
	}

	public static int blockCount() {
		return blocksByName.size();
	}

	public static int itemCount() {
		return itemsByName.size();
	}

	private static int nextFreeBlockId() {
		for(int id = FIRST_BLOCK_ID; id < Block.blocksList.length; ++id) {
			if(Block.blocksList[id] == null && Item.itemsList[id] == null) {
				return id;
			}
		}

		throw new IllegalStateException("No free block IDs left");
	}

	private static int nextFreeItemIndex() {
		for(int index = FIRST_ITEM_INDEX; index < Item.itemsList.length; ++index) {
			if(Item.itemsList[index] == null) {
				return index;
			}
		}

		throw new IllegalStateException("No free item indices left");
	}

	private static void checkName(String name) {
		if(name == null || name.length() == 0) {
			throw new IllegalArgumentException("Registry name must not be empty");
		}

		for(int i = 0; i < name.length(); ++i) {
			char c = name.charAt(i);
			if((c < 'a' || c > 'z') && (c < '0' || c > '9') && c != '_') {
				throw new IllegalArgumentException("Registry name \"" + name + "\" must match [a-z0-9_]+");
			}
		}
	}
}
