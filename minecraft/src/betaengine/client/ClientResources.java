package betaengine.client;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.TreeSet;
import javax.imageio.ImageIO;

import betaengine.BetaEngine;
import betaengine.Textures;
import net.minecraft.client.Minecraft;
import net.minecraft.src.Block;
import net.minecraft.src.Item;
import net.minecraft.src.TexturePackBase;

/**
 * Client half of the resource loader.
 *
 * install() runs before BetaEngine.init() and plugs a real allocator into the
 * Textures facade. wrapResource() is the single hook all texture-pack reads go
 * through (TexturePackBase/TexturePackCustom.getResourceAsStream): when the
 * requested resource is an atlas the engine has stitched textures into, the
 * atlas image is composited on the fly. Because the hook sits at the pack
 * level, vanilla texture-pack switching in the menu (refreshTextures) picks up
 * engine textures — and packs can override them — with no extra code.
 *
 * Engine texture lookup order for betaengine/textures/&lt;dir&gt;/&lt;name&gt;.png:
 *   1. game-dir folder  resources/betaengine/textures/... (dev/user override)
 *   2. selected texture pack zip, same path inside the zip
 *   3. classpath (textures shipped next to the code)
 */
public final class ClientResources {
	private static Minecraft mc = null;
	private static TextureStitcher terrain = null;
	private static TextureStitcher items = null;
	/** vanilla resource path -> engine resource path ("/betaengine/textures/...") */
	private static final java.util.TreeMap fileOverrides = new java.util.TreeMap();

	private ClientResources() {
	}

	/** Call before BetaEngine.init(). mc may be null in headless tests. */
	public static void install(Minecraft minecraft) {
		mc = minecraft;

		terrain = new TextureStitcher("/terrain.png", "blocks", new TextureStitcher.UsedIndexScanner() {
			public Set scanUsed() {
				return scanTerrainUsed();
			}
		}, terrainReserved());

		items = new TextureStitcher("/gui/items.png", "items", new TextureStitcher.UsedIndexScanner() {
			public Set scanUsed() {
				return scanItemsUsed();
			}
		}, new TreeSet());

		Textures.setAllocator(new Textures.Allocator() {
			public int blockTexture(String name) {
				return terrain.allocate(name);
			}

			public int itemTexture(String name) {
				return items.allocate(name);
			}

			public void overrideTexture(String vanillaPath, String textureRelPath) {
				fileOverrides.put(vanillaPath, "/betaengine/textures/" + textureRelPath + ".png");
				BetaEngine.log("Texture override: " + vanillaPath + " -> " + textureRelPath);
			}
		});

		BetaEngine.log("Client resource loader installed");
	}

	/**
	 * Hook called from TexturePackBase/TexturePackCustom.getResourceAsStream.
	 * Non-atlas resources pass through untouched.
	 */
	public static InputStream wrapResource(String name, InputStream vanilla) {
		String overridePath = (String)fileOverrides.get(name);
		if(overridePath != null) {
			InputStream replaced = engineResolver().resolve(overridePath);
			if(replaced != null) {
				if(vanilla != null) {
					try {
						vanilla.close();
					} catch (IOException e) {
					}
				}
				return replaced;
			}
		}

		TextureStitcher stitcher = stitcherFor(name);
		if(stitcher == null || vanilla == null) {
			return vanilla;
		}

		byte[] original;
		try {
			original = readAll(vanilla);
		} catch (IOException e) {
			BetaEngine.log("Failed to buffer " + name + ": " + e);
			return null;
		}

		try {
			BufferedImage atlas = ImageIO.read(new ByteArrayInputStream(original));
			stitcher.composite(atlas, engineResolver());
			ByteArrayOutputStream out = new ByteArrayOutputStream(original.length + 65536);
			ImageIO.write(atlas, "png", out);
			return new ByteArrayInputStream(out.toByteArray());
		} catch (Exception e) {
			BetaEngine.log("Failed to stitch " + name + ", using vanilla atlas: " + e);
			return new ByteArrayInputStream(original);
		}
	}

	private static TextureStitcher stitcherFor(String name) {
		if(terrain != null && terrain.hasAllocations() && terrain.atlasName().equals(name)) {
			return terrain;
		}

		if(items != null && items.hasAllocations() && items.atlasName().equals(name)) {
			return items;
		}

		return null;
	}

	private static ResourceResolver engineResolver() {
		return new ResourceResolver() {
			public InputStream resolve(String path) {
				File dir = mc != null ? Minecraft.getMinecraftDir() : new File(".");
				File file = new File(dir, "resources" + path);
				if(file.isFile()) {
					try {
						return new FileInputStream(file);
					} catch (IOException e) {
					}
				}

				if(mc != null && mc.texturePackList != null) {
					TexturePackBase pack = mc.texturePackList.selectedTexturePack;
					if(pack != null) {
						InputStream in = pack.getResourceAsStream(path);
						if(in != null) {
							return in;
						}
					}
				}

				return ClientResources.class.getResourceAsStream(path);
			}
		};
	}

	/**
	 * Every atlas index vanilla rendering can reach through block objects:
	 * all sides, all metadata values.
	 */
	private static Set scanTerrainUsed() {
		TreeSet used = new TreeSet();

		for(int i = 0; i < Block.blocksList.length; ++i) {
			Block block = Block.blocksList[i];
			if(block == null) {
				continue;
			}

			for(int side = 0; side < 6; ++side) {
				for(int meta = 0; meta < 16; ++meta) {
					try {
						used.add(Integer.valueOf(block.getBlockTextureFromSideAndMetadata(side, meta)));
					} catch (Throwable t) {
					}
				}
			}
		}

		return used;
	}

	private static Set scanItemsUsed() {
		TreeSet used = new TreeSet();

		for(int i = 256; i < Item.itemsList.length; ++i) {
			Item item = Item.itemsList[i];
			if(item == null) {
				continue;
			}

			for(int damage = 0; damage < 64; ++damage) {
				try {
					used.add(Integer.valueOf(item.getIconFromDamage(damage)));
				} catch (Throwable t) {
				}
			}
		}

		return used;
	}

	/**
	 * Indices the brute force cannot see because they depend on world state
	 * or are animated in-place by TextureFX.
	 */
	private static Set terrainReserved() {
		TreeSet reserved = new TreeSet();

		// crack/destroy animation row (RenderGlobal uses 240 + stage)
		reserved.addAll(TextureStitcher.range(240, 255));

		// water/lava: still cell + 2x2 flow animation region (TextureWaterFlowFX
		// and TextureLavaFlowFX have tileSize 2 and write at still+1)
		addFlowRegion(reserved, Block.waterMoving.blockIndexInTexture);
		addFlowRegion(reserved, Block.lavaMoving.blockIndexInTexture);

		// fire animates two cells: base and base+16 (TextureFlamesFX 0 and 1)
		reserved.add(Integer.valueOf(Block.fire.blockIndexInTexture));
		reserved.add(Integer.valueOf(Block.fire.blockIndexInTexture + 16));
		reserved.add(Integer.valueOf(Block.portal.blockIndexInTexture));

		// BlockGrass.getBlockTexture(IBlockAccess...) returns 68 for snowy sides
		reserved.add(Integer.valueOf(68));

		// BlockChest.getBlockTexture(IBlockAccess...) uses tex-1..+1, +16, +32
		// variants for double chests (single 25/26/27, double 41/42, 57/58)
		int chest = Block.chest.blockIndexInTexture;
		reserved.addAll(TextureStitcher.range(chest - 1, chest + 1));
		reserved.addAll(TextureStitcher.range(chest + 15, chest + 17));
		reserved.addAll(TextureStitcher.range(chest + 31, chest + 33));

		return reserved;
	}

	private static void addFlowRegion(TreeSet reserved, int stillIndex) {
		reserved.add(Integer.valueOf(stillIndex));
		int flow = stillIndex + 1;
		reserved.add(Integer.valueOf(flow));
		reserved.add(Integer.valueOf(flow + 1));
		reserved.add(Integer.valueOf(flow + 16));
		reserved.add(Integer.valueOf(flow + 17));
	}

	private static byte[] readAll(InputStream in) throws IOException {
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream(262144);
			byte[] buf = new byte[8192];
			int n;
			while((n = in.read(buf)) > 0) {
				out.write(buf, 0, n);
			}
			return out.toByteArray();
		} finally {
			try {
				in.close();
			} catch (IOException e) {
			}
		}
	}
}
