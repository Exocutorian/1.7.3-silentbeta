package betaengine.client;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.imageio.ImageIO;

import betaengine.BetaEngine;

/**
 * Manages one 16x16-cell atlas (terrain.png or gui/items.png): finds cells no
 * vanilla content renders from, hands them out to engine textures, and later
 * composites the per-texture PNGs into the atlas image as it is loaded.
 *
 * Free cells are computed at runtime by brute-forcing the actual game objects
 * (every block side/metadata, every item damage value) rather than from a
 * hardcoded table, so the list stays correct as vanilla code is modified.
 * World-state-dependent lookups that the brute force cannot see (double
 * chests, snowy grass, animated 2x2 flow textures) are excluded via the
 * reserved set supplied by the caller.
 */
public class TextureStitcher {
	/** Computes atlas indices the game can actually render from. */
	public interface UsedIndexScanner {
		Set scanUsed();
	}

	private final String atlasName;
	private final String texturePath;
	private final UsedIndexScanner scanner;
	private final Set reserved;

	private final TreeMap indexByName = new TreeMap();
	private final TreeMap pathByIndex = new TreeMap();
	private List freeIndices = null;

	public TextureStitcher(String atlasName, String textureSubdir, UsedIndexScanner scanner, Set reserved) {
		this.atlasName = atlasName;
		this.texturePath = "/betaengine/textures/" + textureSubdir + "/";
		this.scanner = scanner;
		this.reserved = reserved;
	}

	public String atlasName() {
		return this.atlasName;
	}

	public boolean hasAllocations() {
		return !this.indexByName.isEmpty();
	}

	/** Allocates (or returns the existing) atlas cell for the named texture. */
	public synchronized int allocate(String name) {
		Integer existing = (Integer)this.indexByName.get(name);
		if(existing != null) {
			return existing.intValue();
		}

		if(this.freeIndices == null) {
			this.freeIndices = this.computeFreeIndices();
			BetaEngine.log(this.atlasName + ": " + this.freeIndices.size() + " free atlas cells");
		}

		if(this.freeIndices.isEmpty()) {
			throw new IllegalStateException("No free cells left in " + this.atlasName + " for texture \"" + name + "\"");
		}

		Integer index = (Integer)this.freeIndices.remove(0);
		this.indexByName.put(name, index);
		this.pathByIndex.put(index, this.texturePath + name + ".png");
		BetaEngine.log(this.atlasName + ": texture \"" + name + "\" -> cell " + index);
		return index.intValue();
	}

	/**
	 * Draws every allocated texture into its cell of the freshly loaded atlas.
	 * Cell size scales with the atlas, so HD texture packs keep working.
	 */
	public void composite(BufferedImage atlas, ResourceResolver resolver) {
		int cell = atlas.getWidth() / 16;
		Graphics2D g = atlas.createGraphics();

		Iterator it = this.pathByIndex.keySet().iterator();
		while(it.hasNext()) {
			Integer index = (Integer)it.next();
			String path = (String)this.pathByIndex.get(index);
			int x = index.intValue() % 16 * cell;
			int y = index.intValue() / 16 * cell;

			BufferedImage tile = null;
			InputStream in = resolver.resolve(path);
			if(in != null) {
				try {
					tile = ImageIO.read(in);
				} catch (IOException e) {
					BetaEngine.log("Failed to read texture " + path + ": " + e);
				} finally {
					try {
						in.close();
					} catch (IOException e) {
					}
				}
			}

			java.awt.Composite oldComposite = g.getComposite();
			g.setComposite(java.awt.AlphaComposite.Src);
			if(tile != null) {
				g.drawImage(tile, x, y, cell, cell, null);
			} else {
				BetaEngine.log("Missing texture " + path + " — painting placeholder");
				drawMissingPattern(g, x, y, cell);
			}
			g.setComposite(oldComposite);
		}

		g.dispose();
	}

	private static void drawMissingPattern(Graphics2D g, int x, int y, int cell) {
		int half = cell / 2;
		g.setColor(java.awt.Color.MAGENTA);
		g.fillRect(x, y, half, half);
		g.fillRect(x + half, y + half, cell - half, cell - half);
		g.setColor(java.awt.Color.BLACK);
		g.fillRect(x + half, y, cell - half, half);
		g.fillRect(x, y + half, half, cell - half);
	}

	private List computeFreeIndices() {
		Set used = this.scanner.scanUsed();
		List free = new ArrayList();

		for(int i = 1; i < 256; ++i) {
			Integer boxed = Integer.valueOf(i);
			if(!used.contains(boxed) && !this.reserved.contains(boxed)) {
				free.add(boxed);
			}
		}

		return free;
	}

	/** For diagnostics/tests. */
	public TreeMap allocations() {
		return new TreeMap(this.indexByName);
	}

	public static Set range(int from, int toInclusive) {
		TreeSet set = new TreeSet();
		for(int i = from; i <= toInclusive; ++i) {
			set.add(Integer.valueOf(i));
		}
		return set;
	}
}
