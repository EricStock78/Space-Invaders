package actors;

import game.Stage;

import java.util.Random;

/**
 * this is a copy paste of the tnt class
 */
public class Moose extends Actor {
    public Moose(Stage canvas) {
        super(canvas, 80, 80, 50, 50);
        // TODO: 2019-08-08 make a moose generator



        Random randy = new Random();
        vx = -10;// x travel speed should always be negitive to simulate driving -10 is road spead
        vy = randy.nextInt(20) - 10;// y travel speed

        sprites = new String[]{"moose.png"};

        posX = 1000; //randy.nextInt(1000); // start position
        posY = randy.nextInt(600); // start position
    }


    public void update() {
        super.update();
        posX += vx;
        posY += vy;
        if (posX == 0) {
            setMarkedForRemoval(true);
        }
    }


}
