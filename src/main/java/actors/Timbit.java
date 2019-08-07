package actors;

import game.Stage;

public class Timbit extends Actor{
    public Timbit(Stage canvas) {
        super(canvas, 80, 80, 80, 80);

        vx = -1;

        sprites = new String[] {"timbit.png"};

        posX = 800;
    }


    public void update() {
        super.update();
        posX += vx;
        posY += vy;
    }


}
