package silentbeta;

import betaengine.Registry;
import betaengine.Textures;
import betaengine.event.Event;
import betaengine.event.EventBus;
import betaengine.event.EventHandler;
import betaengine.event.WorldDecorateEvent;
import betaengine.mod.Mod;
import betaengine.event.BlockBreakEvent;
import net.minecraft.src.Block;
import net.minecraft.src.BlockContainer;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.MathHelper;
import net.minecraft.src.WorldGenMinable;

/**
 * Silent Beta — a quiet fork of Beta 1.7.3, built entirely on the BetaEngine
 * API: no content below is wired into vanilla by hand-edited IDs or atlases.
 */
public class SilentBeta implements Mod {
	public static final String NAME = "Silent Beta";
	public static final String VERSION = "0.0.2";
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
	public static Block obsidianVault;
	public static Item copperIngot;
	public static Item copperPickaxe;
	public static Item copperSpade;
	public static Item copperAxe;
	public static Item copperSword;
	public static Item copperHoe;
	public static Item copperHammer;

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
		obsidianVault = Registry.registerBlock("obsidian_vault", new Registry.BlockFactory() {
			public Block create(int blockId) {
				return new BlockVault(blockId);
			}
		});
		Registry.registerTileEntity(TileEntityVault.class, "SilentVault");

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
		copperHammer = Registry.registerItem("copper_hammer", new Registry.ItemFactory() {
			public Item create(int itemId) {
				return new ItemCopperHammer(itemId);
			}
		});

		this.addRecipes();

		EventBus.subscribe(WorldDecorateEvent.class, new EventHandler() {
			public void handle(Event event) {
				generateOres((WorldDecorateEvent)event);
			}
		});

		EventBus.subscribe(BlockBreakEvent.class, new EventHandler() {
			public void handle(Event event) {
				hammerBreak((BlockBreakEvent)event);
			}
		});
	}

	private void addRecipes() {
		Registry.addSmelting(copperOre.blockID, new ItemStack(copperIngot));

		Registry.addShapedRecipe(new ItemStack(copperBlock), new Object[]{"XXX", "XXX", "XXX", Character.valueOf('X'), copperIngot});
		Registry.addShapelessRecipe(new ItemStack(copperIngot, 9), new Object[]{copperBlock});
		Registry.addShapedRecipe(new ItemStack(obsidianBricks, 4), new Object[]{"XX", "XX", Character.valueOf('X'), Block.obsidian});
		Registry.addShapedRecipe(new ItemStack(obsidianVault), new Object[]{"OOO", "OCO", "OOO", Character.valueOf('O'), obsidianBricks, Character.valueOf('C'), Block.chest});
		Registry.addShapedRecipe(new ItemStack(copperHammer), new Object[]{"XXX", "X#X", " # ", Character.valueOf('X'), copperIngot, Character.valueOf('#'), Item.stick});

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

	/**
	 * Copper hammer: breaking a block also breaks the surrounding 3x3 plane —
	 * horizontal when looking up/down, otherwise the wall you are facing.
	 * Runs where the break is authoritative (client in SP, server in SMP).
	 */
	private static void hammerBreak(BlockBreakEvent e) {
		if(e.isCancelled() || e.player == null || e.world == null || e.blockId <= 0) {
			return;
		}

		ItemStack held = e.player.getCurrentEquippedItem();
		if(held == null || held.getItem() != copperHammer) {
			return;
		}

		Block broken = Block.blocksList[e.blockId];
		float brokenStrength = broken == null ? 0.0F : broken.blockStrength(e.player);

		int axis;
		if(e.player.rotationPitch < -45.0F || e.player.rotationPitch > 45.0F) {
			axis = 0; // looking up/down: horizontal XZ plane
		} else {
			int facing = MathHelper.floor_double((double)(e.player.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
			axis = facing == 0 || facing == 2 ? 2 : 1; // wall plane facing the player
		}

		for(int u = -1; u <= 1; ++u) {
			for(int v = -1; v <= 1; ++v) {
				if(u == 0 && v == 0) {
					continue;
				}

				int x = e.x;
				int y = e.y;
				int z = e.z;
				if(axis == 0) {
					x += u;
					z += v;
				} else if(axis == 1) {
					z += u;
					y += v;
				} else {
					x += u;
					y += v;
				}

				int id = e.world.getBlockId(x, y, z);
				if(id <= 0) {
					continue;
				}

				Block target = Block.blocksList[id];
				if(target == null || target instanceof BlockContainer || target.blockMaterial.getIsLiquid()) {
					continue;
				}

				if(!e.player.canHarvestBlock(target)) {
					continue;
				}

				float strength = target.blockStrength(e.player);
				if(strength <= 0.0F || strength * 3.0F < brokenStrength) {
					continue; // don't smash through much harder material
				}

				target.dropBlockAsItemWithChance(e.world, x, y, z, e.world.getBlockMetadata(x, y, z), 1.0F);
				e.world.setBlockWithNotify(x, y, z, 0);
				held.damageItem(1, e.player);
				if(held.stackSize == 0) {
					return; // hammer broke mid-swing
				}
			}
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
