package actors;

import game.Stage;

import java.util.Random;

public class PotHole  extends Actor {
    private Random randy = new Random();

    public PotHole(Stage canvas) {
        super(canvas, 80, 80, 70, 70);

        vx = -10;

        sprites = new String[] {"pothole.png"};

        posX = 1000;
        posY = randy.nextInt(600);
        //posY =500; for debuging
    }


    public void update() {
        super.update();
        posX += vx;
        posY += vy;

    }


}
