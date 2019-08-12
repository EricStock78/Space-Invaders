package actors;

import game.Stage;

import java.util.Random;

public class EnemyCar extends Actor {
    private Random randy = new Random();

    public EnemyCar(Stage canvas, int enemyCarType, int lane) {
        super(canvas, 145, 73, 145, 73);

        vx = -20;

        switch (enemyCarType) {
            case 0:sprites = new String[]{"enemy_car1.png"};
            break;
            case 1:sprites = new String[]{"enemy_car2.png"};
            break;
            case 2:sprites = new String[]{"enemy_car3.png"};
            break;
            case 3:sprites = new String[]{"enemy_car4.png"};
            break;
            case 4:sprites = new String[]{"enemy_car5.png"};
        }

        posX = 1000;
        if (lane == 0) {
            posY = 40;
        } else {
            posY = 175;
        }
    }

    public void update() {
        super.update();
        posX += vx;
        posY += vy;
    }
}