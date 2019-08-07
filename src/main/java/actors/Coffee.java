package actors;

import game.Stage;

public class Coffee extends Actor {
    public Coffee(Stage canvas) {
        super(canvas, 80, 80, 80, 80);

        vx = -1;

        sprites = new String[] {"coffee.png"};

        posX = 600;
    }


    public void update() {
        super.update();
        posX += vx;
        posY += vy;
    }
}
