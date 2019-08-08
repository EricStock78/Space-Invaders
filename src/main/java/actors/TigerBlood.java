package actors;

import game.Stage;

import java.util.Random;

public class TigerBlood extends Actor{
    private Random randy = new Random();
    public TigerBlood(Stage canvas) {
        super(canvas, 80, 80, 80, 80);

        vx = -2;

        sprites = new String[] {"tigerBlood.png"};

        posX = 1000;
        posY = randy.nextInt(600);
    }

    public void update() {
        super.update();
        posX += vx;
        posY += vy;
    }

    public void giveTigerBlood(Car car) {

    }

}