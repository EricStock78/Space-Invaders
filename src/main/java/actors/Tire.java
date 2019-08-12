package actors;

import game.Stage;

import java.util.Random;

/**
 * Tire class that extends Actor
 * Instantiates a tire power-up in Moose: The Game
 */
public class Tire extends Actor {
    private Random randy = new Random();

    /**
     * Constructor for the Tire object
     * @param canvas
     */
    public Tire(Stage canvas) {
        super(canvas, 60, 60, 60, 60);

        vx = -2;

        sprites = new String[]{"tire.png"};

        posX = 1000;
        posY =randy.nextInt(600);
    }

    public void update() {
        super.update();
        posX += vx;
        posY += vy;
        if (posX == 0) {setMarkedForRemoval(true);}
    }
}