package actors;

import game.Stage;

import java.util.Random;

/**
 * Pothole class that extends Actor
 * Instantiates a pothole hazard in Moose: The Game
 */
public class PotHole  extends Actor {
    private Random randy = new Random();

    /**
     * Constructor for the PotHole object
     * @param canvas
     */
    public PotHole(Stage canvas) {
        super(canvas, 80, 80, 70, 70);

        vx = -10;

        sprites = new String[] {"pothole.png"};

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
