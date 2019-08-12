package actors;

import game.Stage;

import java.util.Random;

/**
 * Coffee class that extends Actor
 * Instantiates a Coffee power-up in Moose: The Game
 */
public class Coffee extends Actor {
    private Random randy = new Random();

    /**
     * Constructor for the Coffee object
     * @param canvas
     */
    public Coffee(Stage canvas) {
        super(canvas, 80, 80, 80, 80);

        vx = -2;

        sprites = new String[] {"coffee.png"};

        posX = 1000;
        posY = randy.nextInt(600);
    }

    public void update() {
        super.update();
        posX += vx;
        posY += vy;
        if (posX == 0) {setMarkedForRemoval(true);}
    }

}