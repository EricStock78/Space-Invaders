package game;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.TexturePaint;
import java.awt.Transparency;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;


import javax.swing.JFrame;
import javax.swing.JPanel;

import actors.Actor;
import actors.Ball;
import actors.Invader;
import actors.Paddle;
import actors.Player;
import actors.Shot;
import actors.Ufo;
import static game.Stage.DESIRED_FPS;
import static game.Stage.HEIGHT;
import static game.Stage.WIDTH;

public class PongGame extends Stage implements KeyListener {

	private static final long serialVersionUID = 1L;

	
	private InputHandler keyPressedHandlerLeft;
	private InputHandler keyReleasedHandlerLeft;
        
        private InputHandler keyPressedHandlerRight;
	private InputHandler keyReleasedHandlerRight;

	public long usedTime;//time taken per game step
	public BufferStrategy strategy;	 //double buffering strategy

	private Paddle paddleLeft;
        private Paddle paddleRight;
        private Ball ball;

	public PongGame() {
		//init the UI
		setBounds(0,0,Stage.WIDTH,Stage.HEIGHT);
		setBackground(Color.BLUE);

		JPanel panel = new JPanel();
		panel.setPreferredSize(new Dimension(Stage.WIDTH,Stage.HEIGHT));
		panel.setLayout(null);

		panel.add(this);

		JFrame frame = new JFrame("Invaders");
		frame.add(panel);

		frame.setBounds(0,0,Stage.WIDTH,Stage.HEIGHT);
		frame.setResizable(false);
		frame.setVisible(true);

		//cleanup resources on exit
		frame.addWindowListener( new WindowAdapter() {
			          public void windowClosing(WindowEvent e) {
			        	ResourceLoader.getInstance().cleanup();
			            System.exit(0);
			          }
			        });


		addKeyListener(this);

		//create a double buffer
		createBufferStrategy(2);
		strategy = getBufferStrategy();
		requestFocus();
		initWorld();

		keyPressedHandlerLeft = new InputHandler(this, paddleLeft);
		keyPressedHandlerLeft.action = InputHandler.Action.PRESS;
		keyReleasedHandlerLeft = new InputHandler(this, paddleLeft);
		keyReleasedHandlerLeft.action = InputHandler.Action.RELSEASE;
                
                keyPressedHandlerRight = new InputHandler(this, paddleRight);
		keyPressedHandlerRight.action = InputHandler.Action.PRESS;
		keyReleasedHandlerRight = new InputHandler(this, paddleRight);
		keyReleasedHandlerRight.action = InputHandler.Action.RELSEASE;
	}

	

	public void initWorld() {
		paddleLeft = new Paddle(this, Paddle.ePlayerNumber.PN_ONE);
                paddleRight = new Paddle(this, Paddle.ePlayerNumber.PN_TWO);
                ball = new Ball(this);
	}

	public void paintWorld() {

		//get the graphics from the buffer
		Graphics g = strategy.getDrawGraphics();
		//init image to background
		g.setColor(getBackground());
		g.fillRect(0, 0, getWidth(), getHeight());
		//load subimage from the background
		
		//paint the actors
		for (int i = 0; i < actors.size(); i++) {
			Actor actor = actors.get(i);
			actor.paint(g);
		}
                paddleLeft.paint(g);
                paddleRight.paint(g);
                ball.paint(g);
		paintFPS(g);
		//swap buffer
		strategy.show();
	}

	public void paintFPS(Graphics g) {
		g.setColor(Color.RED);
		if (usedTime > 0)
			g.drawString(String.valueOf(1000/usedTime)+" fps",0,Stage.HEIGHT-50);
		else
			g.drawString("--- fps",0,Stage.HEIGHT-50);
	}

	public void paint(Graphics g) {}

	public void updateWorld() {
            paddleLeft.update();
            paddleRight.update();
            ball.update();
	}

	private void checkCollision() {
            if( ball.getBounds().intersects(paddleLeft.getBounds())) {
                ball.collision(paddleLeft);
            } else if( ball.getBounds().intersects(paddleRight.getBounds())) {
                ball.collision(paddleRight);
            }

	}

	public void loopSound(final String name) {
		new Thread(new Runnable() {
			public void run() {
				ResourceLoader.getInstance().getSound(name).loop();
			}
		}).start();
	}


	public void game() {
		//loopSound("music.wav");
		usedTime= 0;
		while(isVisible()) {
			long startTime = System.currentTimeMillis();
                        checkCollision();
			updateWorld();
			paintWorld();

                        usedTime = System.currentTimeMillis() - startTime;

			//calculate sleep time
			if (usedTime == 0) usedTime = 1;
			int timeDiff = 1000/DESIRED_FPS - (int)(usedTime);
			if (timeDiff > 0) {
				try {
					Thread.sleep(timeDiff);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
                        usedTime = System.currentTimeMillis() - startTime;
		}
	}

	public void keyPressed(KeyEvent e) {
		keyPressedHandlerLeft.handleInput(e);
		keyPressedHandlerRight.handleInput(e);
	}

	public void keyReleased(KeyEvent e) {
		keyReleasedHandlerLeft.handleInput(e);
		keyReleasedHandlerRight.handleInput(e);
	}

	public void keyTyped(KeyEvent e) {
	}

}