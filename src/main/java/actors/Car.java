package actors;

import game.Stage;

import java.awt.event.KeyEvent;

public class Car extends Actor implements KeyboardControllable {
    private boolean up,down,left,right;
    public enum ePlayerNumber {
        PN_ONE,}


    public Car(Stage stage) {
        super(stage, 256, 256, 256, 256);
        sprites = new String[]{"Car.png"};
        frame = 0;
        frameSpeed = 35;
        actorSpeed = 10;
        posX = Stage.WIDTH/2 - 128;
        posY = Stage.HEIGHT/2 - 128;
    }

    public  void update() {
        super.update();
        updateSpeed();
    }

    protected void updateSpeed() {
        vx = 0;
        vy = 0;
        if (down)
            vy = actorSpeed;
        if (up)
            vy = -actorSpeed;
        if (left)
            vx = -actorSpeed;
        if (right)
            vx = actorSpeed;

        //don't allow scrolling off the edge of the screen
        if (posX - getWidth()/2 > 0 && vx < 1)
            posX += vx;
        else if (posX + getWidth()  + (getWidth()/2)< Stage.WIDTH )//&& vx > 1)
            posX += vx;

        if (posY - getHeight()/2 > 0 && vy < 0)
            posY += vy;
        else if (posY + getHeight() + (getHeight()/2) < Stage.HEIGHT && vy > 0)
            posY += vy;
    }

    public void triggerKeyRelease(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_DOWN:
                down = false;
                break;
            case KeyEvent.VK_UP:
                up = false;
                break;
            case KeyEvent.VK_LEFT:
                left = false;
                break;
            case KeyEvent.VK_RIGHT:
                right = false;
                break;
        }
    }

    public void triggerKeyPress(KeyEvent e) {
        switch (e.getKeyCode()) {
            ///*
            case KeyEvent.VK_UP:
                up = true;
                break;
            //*/
            case KeyEvent.VK_LEFT:
                left = true;
                break;
            case KeyEvent.VK_RIGHT:
                right = true;
                break;
            ///*
            case KeyEvent.VK_DOWN:
                down = true;
                break;
        }
    }
}
