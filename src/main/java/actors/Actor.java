package actors;

import game.ResourceLoader;
import game.Stage;
import java.awt.Graphics;
import java.awt.Rectangle;


public class Actor {

	public static boolean debugCollision = false;

	private static final int POINT_VALUE = 0;
	protected int vx; 
	protected int vy;
	protected int posX;
	protected int posY;
	private int height;
	private int width;
	protected int collisionWidth;
	protected int collisionHeight;
	protected int frame;
	protected int frameSpeed;
	protected int actorSpeed;
	protected int time;
	private boolean markedForRemoval = false;
	protected String[] sprites = null; 
	protected Stage stage = null;

	private String debugSpriteName = "squareOutline.png";

	public Actor(Stage canvas, int width, int height, int collisionWidth, int collisionHeight) {
		this.stage = canvas;
		frame = 0;
		frameSpeed = 15;
		actorSpeed = 10;
		time = 0;
		this.width = width;
		this.height = height;
		this.collisionWidth = collisionWidth;
		this.collisionHeight = collisionHeight;
	}
	
	public void update() {
		updateSpriteAnimation();
	}
	
	private void updateSpriteAnimation() {
		time++;
		if (time % frameSpeed == 0) {
			time = 0;
			frame = (frame + 1) % sprites.length;
		}
	}
	
	public void playSound(final String name) {
		new Thread(new Runnable() {
			public void run() {
				ResourceLoader.getInstance().getSound(name).play();
			}
		}).start();
	}

	private boolean tileImage = false;
	private int tileWidth;
	private int tileHeight;

	public void SetTilingEnabled(int width, int height) {
		tileImage = true;
		tileWidth = width;
		tileHeight = height;
	}

	public void SetTilingDisabled() {
		tileImage = false;
	}
			
	public  void paint(Graphics g) {
		if( debugCollision ) {
			int horizOffset = (width - collisionWidth) / 2;
			int vertOffset = (height - collisionHeight) / 2;

			g.drawImage(ResourceLoader.getInstance().getSprite(debugSpriteName),
					posX + horizOffset, posY + vertOffset, collisionWidth, collisionHeight, stage);
		}
		else {
			int tileX = 0;

			if( tileImage ) {
				while(true) {
					int offsetX = tileWidth * tileX;
					tileX++;
					if( (width - offsetX) > tileWidth ) {
						int tileY = 0;
						while( true ) {
							int offsetY = tileHeight * tileY;
							tileY++;
							if( (height - offsetY ) > tileHeight ) {
								g.drawImage(ResourceLoader.getInstance().getSprite(sprites[frame]), posX + offsetX, posY + offsetY, tileWidth, tileHeight, stage);
							}
							else {
								g.drawImage(ResourceLoader.getInstance().getSprite(sprites[frame]), posX + offsetX, posY + offsetY,
										posX + offsetX + tileWidth, (posY + offsetY) + height - offsetY, 0, 0,
										tileWidth, height - offsetY, stage);
								break;
							}
						}
					}
					else {
						int tileY = 0;
						while( true ) {
							int offsetY = tileHeight * tileY;
							tileY++;
							if( (height - offsetY ) > tileHeight ) {
								g.drawImage(ResourceLoader.getInstance().getSprite(sprites[frame]), posX + offsetX, posY + offsetY,
										(posX + offsetX )+ (width - offsetX), posY + offsetY + tileHeight, 0, 0,
										width - offsetX, tileHeight, stage);}
							else {
								g.drawImage(ResourceLoader.getInstance().getSprite(sprites[frame]), posX + offsetX, posY + offsetY,
										(posX + offsetX )+ (width - offsetX), (posY + offsetY) + (height - offsetY), 0, 0,
										width - offsetX, height - offsetY, stage);
								break;
							}
						}
						break;
					}
				}
			}
			else {
				g.drawImage(ResourceLoader.getInstance().getSprite(sprites[frame]), posX, posY, width, height, stage);
			}
		}
	}
	
	public void setX(int posX) {
		this.posX = posX;
	}
	
	public void setY(int posY) {
		this.posY = posY;
	}
	
	public int getX() {
		return posX;
	}
	
	public int getY() {
		return posY;
	}
	
	public void setWidth(int width) {
		this.width = width;
	}

	public int getWidth() {
		return width;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int getHeight() {
		return height;
	}

	public void setCollisionHeight(int height) {
		collisionHeight = height;
	}

	public void setCollisionWidth(int width) {
		collisionWidth = width;
	}
	
	public void setVx(int vx) {
		this.vx = vx;
	}

	public int getVx() {
		return vx;
	}
	
	public void setVy(int vy) {
		this.vy = vy;
	}

	public int getVy() {
		return vy;
	}

	public Rectangle getBounds() {
		int horizOffset = (width - collisionWidth) / 2;
		int vertOffset = (height - collisionHeight) / 2;
		return new Rectangle(posX + horizOffset,posY + vertOffset,collisionWidth, collisionHeight);
	}
	
	public void collision(Actor a) {		
	}
	
	public void setMarkedForRemoval(boolean markedForRemoval) {
		this.markedForRemoval = markedForRemoval;
	}

	public boolean isMarkedForRemoval() {
		return markedForRemoval;
	}
	
	public int getPointValue() {
		return Actor.POINT_VALUE;
	}
}
