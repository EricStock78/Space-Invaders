package actors;

import game.Stage;
import java.awt.event.KeyEvent;

public class Player extends Actor implements KeyboardControllable {
	
	private boolean up,down,left,right;
	private int score = 0;
	private int maxHealth = 100;
	
	public Player(Stage stage) {
		super(stage, 32, 20, 32, 20);

		sprites = new String[]{"player.gif"};
		frame = 0;
		frameSpeed = 35;
		actorSpeed = 10;
		posX = Stage.WIDTH/2;
		posY = Stage.HEIGHT/2;
	}

	public void update() {
		super.update();		
	}
	
	protected void updateSpeed() {
		vx = 0;
		vy = 0;
		if (down)
			vy = actorSpeed;
		if (up)
			vy = -actorSpeed;
		if (left)
			vx = -actorSpeed;
		if (right)
			vx = actorSpeed;
		
		//don't allow scrolling off the edge of the screen		
		if (posX - getWidth()/2 > 0 && vx < 0)
			posX += vx;
		else if (posX + getWidth()  + (getWidth()/2)< Stage.WIDTH && vx > 0)
			posX += vx;
		else if (posY - getHeight()/2 > 0 && vy < 0)
			posY += vy;
		else if (posY + getHeight() + (getHeight()/2) < Stage.HEIGHT && vy > 0)
			posY += vy;
	}

	public void triggerKeyRelease(KeyEvent e) {
		switch (e.getKeyCode()) {
		case KeyEvent.VK_DOWN:
			down = false;
			break;
		case KeyEvent.VK_UP:
			up = false;
			break;
		case KeyEvent.VK_LEFT:
			left = false;
			break;
		case KeyEvent.VK_RIGHT:
			right = false;
			break;
		}
		updateSpeed();
	}

	public void triggerKeyPress(KeyEvent e) {
		switch (e.getKeyCode()) {
		///*
		case KeyEvent.VK_UP:
			up = true;
			break;
		//*/
		case KeyEvent.VK_LEFT:
			left = true;
			break;
		case KeyEvent.VK_RIGHT:
			right = true;
			break;
		///*
		case KeyEvent.VK_DOWN:
			down = true;
			break;
	    //*/
		case KeyEvent.VK_SPACE: 
			fire(); 
			break;

		}
		updateSpeed();
	}

	public void collision(Actor a) {		
		stage.endGame();
	}

	private void fire() {
		Actor shot = new Shot(stage);
		shot.setX(posX);
		shot.setY(posY - shot.getHeight());
		stage.actors.add(shot);
		playSound("photon.wav");
	}

	public void updateScore(int score) {
		this.score += score;
	}

	public int getScore() {
		return score;
	}
        
	public void resetScore() {
            score = 0;
        }
}
