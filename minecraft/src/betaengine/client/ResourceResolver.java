package betaengine.client;

import java.io.InputStream;

/**
 * Resolves an engine resource path (e.g. "/betaengine/textures/blocks/copper_block.png")
 * to a stream, or null if not found anywhere.
 */
public interface ResourceResolver {
	InputStream resolve(String path);
}
