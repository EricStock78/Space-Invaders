package actors;

import game.Stage;

public class PotHole  extends Actor {
    public PotHole(Stage canvas) {
        super(canvas, 80, 80, 80, 80);

        vx = -10;

        sprites = new String[] {"pothole.png"};

        posX = 1000;
    }


    public void update() {
        super.update();
        posX += vx;

    }


}
