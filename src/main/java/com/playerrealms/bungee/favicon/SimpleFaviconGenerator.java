package com.playerrealms.bungee.favicon;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.Random;

import com.playerrealms.bungee.ServerManager;
import com.playerrealms.common.ServerStatus;
import com.playerrealms.common.ServerType;

import net.md_5.bungee.api.Favicon;

public class SimpleFaviconGenerator implements FaviconGenerator {

	private BufferedImage blocks, base;
	private Font font;
	
	private long cacheTimePeriod;
	private long lastGenerate;
	
	private Favicon generated;
	
	public SimpleFaviconGenerator(BufferedImage blocks, BufferedImage base, Font font) {
		this.font = font;
		this.blocks = blocks;
		this.base = base;
		cacheTimePeriod = 30000;
		lastGenerate = 0;
	}
	
	public void setCacheTimePeriod(long cacheTimePeriod) {
		if(cacheTimePeriod < 0){
			throw new IllegalArgumentException("cacheTimePeriod must be greater than or equal to 0");
		}
		this.cacheTimePeriod = cacheTimePeriod;
	}

	@Override
	public Favicon generate() {
		if(System.currentTimeMillis() - lastGenerate > cacheTimePeriod || generated == null){
			generated = Favicon.create(generateImage());
			lastGenerate = System.currentTimeMillis();
		}
		return generated;
	}
	
	private BufferedImage getRandomBlock(){
		
		Random random = new Random();
		
		int amount = blocks.getWidth() / 64;
		
		int n = random.nextInt(amount);
		
		return blocks.getSubimage(n * 64, 0, 64, 64);
	}
	
	private void drawString(Graphics g, String str, int x, int y, boolean outline, boolean centered){
		
		if(centered){
			
			FontMetrics metrics = g.getFontMetrics();
			
			int width = metrics.stringWidth(str);
			
			x -= width / 2;
			
		}
		
		if(outline){
			g.setColor(Color.black);

			g.drawString(str, x-1, y);
			g.drawString(str, x+1, y);
			g.drawString(str, x, y-1);
			g.drawString(str, x, y+1);
		}
		
		g.setColor(Color.white);
		
		g.drawString(str, x, y);
		
	}
	
	private BufferedImage generateImage(){
		
		BufferedImage image = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
		
		Graphics2D g = (Graphics2D) image.getGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		
		g.drawImage(getRandomBlock(), 0, 0, null);
		g.drawImage(base, 0, 0, null);
		g.setFont(font);
		
		long online = ServerManager.getInstance().getClient().getServers().stream().filter(s -> s.getStatus() == ServerStatus.ONLINE).filter(s -> s.getServerType() == ServerType.PLAYER).count();
		
		drawString(g, String.valueOf(online), 48, 54, true, true);
		
		return image;
		
	}
	
}
