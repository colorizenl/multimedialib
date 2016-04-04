//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2016 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.graphics;

import java.util.ArrayList;
import java.util.List;

import nl.colorize.multimedialib.mock.MockImageData;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for the {@code Sprite} class.
 */
public class TestSprite {
	
	@Test
	public void testBoundingShape() {
		Sprite sprite = new Sprite();
		sprite.addState("test", MockImageData.create(100, 100));
		sprite.setPosition(100, 100);
		assertTrue(sprite.getBounds().contains(100, 100));
		assertTrue(sprite.getBounds().contains(51, 100));
		assertFalse(sprite.getBounds().contains(49, 100));
	}
	
	@Test
	public void testBoundingWhenRotated() {
		Sprite sprite = new Sprite();
		sprite.addState("test", MockImageData.create(100, 100));
		assertTrue(sprite.getBounds().contains(-45, -45));
		sprite.getTransform().setRotation(45);
		assertFalse(sprite.getBounds().contains(-45, -45));
	}
	
	@Test
	public void testBoundingShapeWhenScaled() {
		Sprite sprite = new Sprite();
		sprite.addState("test", new MockImageData());
		assertFalse(sprite.getBounds().contains(-75, 0));
		sprite.getTransform().setScale(200);
		assertTrue(sprite.getBounds().contains(-75, 0));
	}
	
	@Test
	public void testChangeState() {
		MockImageData first = new MockImageData();
		MockImageData second = new MockImageData();
		
		Sprite sprite = new Sprite();
		sprite.addState("first", first);
		sprite.addState("second", second);
		
		assertEquals("first", sprite.getCurrentState());
		assertEquals(first, sprite.getCurrentGraphics().getImage());
		
		sprite.changeState("second");
		
		assertEquals("second", sprite.getCurrentState());
		assertEquals(second, sprite.getCurrentGraphics().getImage());
	}
	
	@Test
	public void testStateWithAnimation() {
		List<ImageRegion> frames = new ArrayList<ImageRegion>();
		frames.add(new MockImageData().toRegion());
		frames.add(new MockImageData().toRegion());
		frames.add(new MockImageData().toRegion());
		Animation anim = new Animation(frames, 1f, true);
		
		Sprite sprite = new Sprite();
		sprite.addState("default", new MockImageData());
		sprite.addState("anim", anim);
		sprite.changeState("anim");
		
		assertEquals(frames.get(0), sprite.getCurrentGraphics());
		
		sprite.onFrame(1f);
		
		assertEquals(frames.get(1), sprite.getCurrentGraphics());
		
		sprite.changeState("default");
		sprite.changeState("anim");

		assertEquals(frames.get(0), sprite.getCurrentGraphics());
	}
	
	@Test
	public void testSettingSameStateDoesNotResetIt() {
		List<ImageRegion> frames = new ArrayList<ImageRegion>();
		frames.add(new MockImageData().toRegion());
		frames.add(new MockImageData().toRegion());
		Animation anim = new Animation(frames, 1f, true);
		
		Sprite sprite = new Sprite();
		sprite.addState("anim", anim);
		sprite.onFrame(1f);
		sprite.changeState("anim");
		
		assertEquals(frames.get(1), sprite.getCurrentGraphics());
	}
}
