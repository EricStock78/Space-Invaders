package actors;

import game.Stage;

import java.util.Random;

/**
 * TigerBlood class that extends Actor
 * Instantiates a TigerBlood power-up in Moose: The Game
 */
public class TigerBlood extends Actor{
    private Random randy = new Random();

    /**
     * Constructor for the TigerBlood object
     * @param canvas
     */
    public TigerBlood(Stage canvas) {
        super(canvas, 80, 80, 80, 80);

        vx = -2;

        sprites = new String[] {"tigerBlood.png"};

        posX = 1000;
        posY =400;

    }

    public void update() {
        super.update();
        posX += vx;
        posY += vy;
        if (posX == 0) {setMarkedForRemoval(true);}

    }

}