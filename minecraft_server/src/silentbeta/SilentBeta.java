package silentbeta;

import betaengine.Registry;
import betaengine.Textures;
import betaengine.event.Event;
import betaengine.event.EventBus;
import betaengine.event.EventHandler;
import betaengine.event.WorldDecorateEvent;
import betaengine.mod.Mod;
import net.minecraft.src.Block;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.WorldGenMinable;

/**
 * Silent Beta — a quiet fork of Beta 1.7.3, built entirely on the BetaEngine
 * API: no content below is wired into vanilla by hand-edited IDs or atlases.
 */
public class SilentBeta implements Mod {
	public static final String NAME = "Silent Beta";
	public static final String VERSION = "0.0.1";
	public static final String DISPLAY = NAME + " " + VERSION;

	/** Mixed into the main menu splash pool with a 50% pick chance. */
	public static final String[] SPLASHES = new String[] {
		"Silent but deadly!",
		"Тихо...",
		"The other timeline!",
		"Obsidian-flavoured!",
		"Now with copper!",
		"Forked with love!",
		"Ещё бета, уже другая!",
		"b1.7.3 forever!",
		"Shhh...",
		"No herobrine — he left quietly."
	};

	public static Block copperOre;
	public static Block copperBlock;
	public static Block obsidianBricks;
	public static Item copperIngot;
	public static Item copperPickaxe;
	public static Item copperSpade;
	public static Item copperAxe;
	public static Item copperSword;
	public static Item copperHoe;

	public String id() {
		return "silentbeta";
	}

	public void init() {
		// the menu dirt is for the other timeline — ours is obsidian
		Textures.override("/gui/background.png", "gui/background");

		copperOre = Registry.registerBlock("copper_ore", new Registry.BlockFactory() {
			public Block create(int blockId) {
				return new BlockCopperOre(blockId);
			}
		});
		copperBlock = Registry.registerBlock("copper_block", new Registry.BlockFactory() {
			public Block create(int blockId) {
				return new BlockCopper(blockId);
			}
		});
		obsidianBricks = Registry.registerBlock("obsidian_bricks", new Registry.BlockFactory() {
			public Block create(int blockId) {
				return new BlockObsidianBricks(blockId);
			}
		});

		copperIngot = Registry.registerItem("copper_ingot", new Registry.ItemFactory() {
			public Item create(int itemId) {
				return new ItemCopperIngot(itemId);
			}
		});
		copperPickaxe = Registry.registerItem("copper_pickaxe", new Registry.ItemFactory() {
			public Item create(int itemId) {
				return new ItemCopperPickaxe(itemId);
			}
		});
		copperSpade = Registry.registerItem("copper_shovel", new Registry.ItemFactory() {
			public Item create(int itemId) {
				return new ItemCopperSpade(itemId);
			}
		});
		copperAxe = Registry.registerItem("copper_axe", new Registry.ItemFactory() {
			public Item create(int itemId) {
				return new ItemCopperAxe(itemId);
			}
		});
		copperSword = Registry.registerItem("copper_sword", new Registry.ItemFactory() {
			public Item create(int itemId) {
				return new ItemCopperSword(itemId);
			}
		});
		copperHoe = Registry.registerItem("copper_hoe", new Registry.ItemFactory() {
			public Item create(int itemId) {
				return new ItemCopperHoe(itemId);
			}
		});

		this.addRecipes();

		EventBus.subscribe(WorldDecorateEvent.class, new EventHandler() {
			public void handle(Event event) {
				generateOres((WorldDecorateEvent)event);
			}
		});
	}

	private void addRecipes() {
		Registry.addSmelting(copperOre.blockID, new ItemStack(copperIngot));

		Registry.addShapedRecipe(new ItemStack(copperBlock), new Object[]{"XXX", "XXX", "XXX", Character.valueOf('X'), copperIngot});
		Registry.addShapelessRecipe(new ItemStack(copperIngot, 9), new Object[]{copperBlock});
		Registry.addShapedRecipe(new ItemStack(obsidianBricks, 4), new Object[]{"XX", "XX", Character.valueOf('X'), Block.obsidian});

		Object[] tools = new Object[]{
			copperPickaxe, "XXX", " # ", " # ",
			copperSpade,   "X",   "#",   "#",
			copperAxe,     "XX",  "X#",  " #",
			copperSword,   "X",   "X",   "#",
			copperHoe,     "XX",  " #",  " #"
		};
		for(int i = 0; i < tools.length; i += 4) {
			Registry.addShapedRecipe(new ItemStack((Item)tools[i]), new Object[]{
				tools[i + 1], tools[i + 2], tools[i + 3],
				Character.valueOf('X'), copperIngot, Character.valueOf('#'), Item.stick
			});
		}
	}

	/** Copper is common like iron but sits a bit shallower. */
	private static void generateOres(WorldDecorateEvent e) {
		int baseX = e.chunkX * 16;
		int baseZ = e.chunkZ * 16;

		for(int i = 0; i < 16; ++i) {
			int x = baseX + e.random.nextInt(16);
			int y = e.random.nextInt(72);
			int z = baseZ + e.random.nextInt(16);
			(new WorldGenMinable(copperOre.blockID, 7)).generate(e.world, e.random, x, y, z);
		}
	}
}
