//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2016 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.graphics;

import java.io.IOException;
import java.io.InputStream;

import nl.colorize.util.ResourceFile;

/**
 * Describes the contents of an audio clip that was either loaded from a file or
 * created programmatically. Audio formats guaranteed to be supported by all
 * renderer implementations are MP3 and OGG.
 */
public class AudioData {
	
	private ResourceFile source;
	
	public AudioData(ResourceFile source) {
		this.source = source;
	}
	
	public InputStream openStream() throws IOException {
		return source.openStream();
	}
}
