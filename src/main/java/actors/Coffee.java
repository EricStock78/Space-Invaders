package actors;

import game.Stage;

import java.util.Random;

public class Coffee extends Actor {
    private Random randy = new Random();
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
    }
}