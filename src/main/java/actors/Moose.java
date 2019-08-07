package actors;

import game.Stage;

/**
 * this is a copy paste of the tnt class
 */
public class Moose extends Actor {
    public Moose(Stage canvas) {
        super(canvas, 80, 80, 80, 80);

        vx = -1;

        sprites = new String[] {"mooseBig.png"};

        posX = 500;
    }


    public void update() {
        super.update();
        posX += vx;
        posY += vy;
    }


}
