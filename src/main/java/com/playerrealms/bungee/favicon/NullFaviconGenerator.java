package com.playerrealms.bungee.favicon;

import java.awt.image.BufferedImage;

import net.md_5.bungee.api.Favicon;

public class NullFaviconGenerator implements FaviconGenerator {

	@Override
	public Favicon generate() {
		return Favicon.create(new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB));
	}

}
