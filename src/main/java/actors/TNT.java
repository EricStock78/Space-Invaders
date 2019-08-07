package actors;

import game.Stage;

public class TNT extends Actor {
    public TNT(Stage canvas) {
        super(canvas, 80, 80, 80, 80);

        vx = -1;

        sprites = new String[] {"tnt.png"};

        posX = 500;
    }


    public void update() {
        super.update();
        posX += vx;
        posY += vy;
    }


}
