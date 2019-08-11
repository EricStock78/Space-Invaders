package actors;

import game.Stage;

import java.util.Random;

public class Timbit extends Actor{
    private Random randy = new Random();
    public Timbit(Stage canvas) {
        super(canvas, 80, 80, 80, 80);

        vx = -2;

        sprites = new String[] {"timbit.png"};

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