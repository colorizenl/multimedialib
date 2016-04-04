//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2016 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.graphics;

import com.google.common.collect.ImmutableList;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit test for the {@code DisplayList} class.
 */
public class TestDisplayList {

	@Test
	public void testSortByZIndex() {
		Sprite first = createSpriteAtZ(100);
		Sprite second = createSpriteAtZ(5);
		Sprite third = createSpriteAtZ(20);
		
		DisplayList displayList = new DisplayList();
		displayList.addAll(first, second, third);
		
		assertEquals(3, displayList.size());
		assertEquals(ImmutableList.of(second, third, first), displayList.toList());
	}
	
	@Test
	public void testKeepInsertionOrderForSameZIndex() {
		Sprite first = createSpriteAtZ(0);
		Sprite second = createSpriteAtZ(1);
		Sprite third = createSpriteAtZ(0);
		
		DisplayList displayList = new DisplayList();
		displayList.addAll(first, second, third);
		
		assertEquals(3, displayList.size());
		assertEquals(ImmutableList.of(first, third, second), displayList.toList());
	}
	
	private Sprite createSpriteAtZ(int zIndex) {
		Sprite sprite = new Sprite();
		sprite.setZIndex(zIndex);
		return sprite;
	}
}
