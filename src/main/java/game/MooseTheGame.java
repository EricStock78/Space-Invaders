package game;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;
import java.util.Timer;
import java.util.TimerTask;


import javax.swing.JFrame;
import javax.swing.JPanel;

import actors.*;

public class MooseTheGame extends Stage implements KeyListener {

    private static final long serialVersionUID = 1L;

    private InputHandler keyPressedHandlerLeft;
    private InputHandler keyReleasedHandlerLeft;

    private InputHandler keyPressedHandlerRight;
    private InputHandler keyReleasedHandlerRight;

    public long usedTime; //time taken per game step
    public long timeElapsed; // total time game has been running
    public BufferStrategy strategy;     //double buffering strategy
    public int roadHorizontalOffset;

    private Car car;
    private Timbit timbit;
    private Coffee coffee;
    private Moose moose;
    private TigerBlood tigerBlood;
    private int health = 100;
    private int score;

    private Splat splat;
    private int splatFrames;

    public MooseTheGame() {
        //init the UI
        setBounds(0, 0, Stage.WIDTH, Stage.HEIGHT);
        setBackground(Color.black);

        JPanel panel = new JPanel();
        panel.setPreferredSize(new Dimension(Stage.WIDTH, Stage.HEIGHT));
        panel.setLayout(null);

        panel.add(this);

        JFrame frame = new JFrame("Moose The Game");
        frame.add(panel);

        frame.setBounds(0, 0, Stage.WIDTH, Stage.HEIGHT);
        frame.setResizable(false);
        frame.setVisible(true);

        //cleanup resources on exit
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                ResourceLoader.getInstance().cleanup();
                System.exit(0);
            }
        });


        addKeyListener(this);

        //create a double buffer
        createBufferStrategy(2);
        strategy = getBufferStrategy();
        requestFocus();
        initWorld();

        keyPressedHandlerLeft = new InputHandler(this, car);
        keyPressedHandlerLeft.action = InputHandler.Action.PRESS;
        keyReleasedHandlerLeft = new InputHandler(this, car);
        keyReleasedHandlerLeft.action = InputHandler.Action.RELSEASE;

        //keyPressedHandlerRight = new InputHandler(this, paddleRight);
        //keyPressedHandlerRight.action = InputHandler.Action.PRESS;
        //keyReleasedHandlerRight = new InputHandler(this, paddleRight);
        //keyReleasedHandlerRight.action = InputHandler.Action.RELSEASE;
        roadHorizontalOffset = 0;
    }


    public void initWorld() {
        car = new Car(this);
        actors.add(car);

        timbit = new Timbit(this);
        actors.add(timbit);
        coffee = new Coffee(this);
        actors.add(coffee);
        moose = new Moose(this);
        actors.add(moose);
        tigerBlood = new TigerBlood(this);
        actors.add(tigerBlood);

    }

    public void paintWorld() {

        //get the graphics from the buffer
        Graphics g = strategy.getDrawGraphics();

        //init image to background
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());

        g.drawImage(ResourceLoader.getInstance().getSprite("road.png"), -roadHorizontalOffset, 0, this);

        g.drawImage(ResourceLoader.getInstance().getSprite("road.png"), -roadHorizontalOffset + Stage.WIDTH, 0, this);

        //load subimage from the background

        //paint the actors
        for (int i = 0; i < actors.size(); i++) {
            Actor actor = actors.get(i);
            actor.paint(g);
        }

        paintScore(g, score);

        //swap buffer
        strategy.show();

    }

    public void paintFPS(Graphics g) {
        g.setColor(Color.RED);
        if (usedTime > 0)
            g.drawString(String.valueOf(1000 / usedTime) + " fps", 0, Stage.HEIGHT - 50);
        else
            g.drawString("--- fps", 0, Stage.HEIGHT - 50);
    }

    public void paintScore(Graphics g, int score) {
            g.setColor(Color.RED);
            g.drawString(String.valueOf(score), Stage.WIDTH - 700, Stage.HEIGHT - 50);
    }

    public void trackScore() {
        score = 0;
        Timer timer = new Timer();

        try {
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    score += 10;
                }
            }, 0, 2000);
        }
        catch (Exception e)  {
            timer.cancel();
        }
    }

    public void paint(Graphics g) {
    }

    public void updateWorld() {

        roadHorizontalOffset += 10;
        roadHorizontalOffset %= Stage.WIDTH;

        for(int i = 0; i < actors.size(); i++){
            if (actors.get(i).isMarkedForRemoval()){
                actors.remove(i);
                i--;
            }
            actors.get(i).update();
        }
//
//        if (splat != null) {
//            splat.update();
//            splatFrames++;
//
//            if (splatFrames > 60) {
//                splat = null;
//            }

        //paddleRight.update();
        //ball.update();
    }

    private void checkCollision() {

        // TODO: 2019-08-07 fix the removal of timbit and coffee
        if (car.getBounds().intersects(timbit.getBounds())){
            health+=10;
            //System.out.println("yumm!");
            timbit.setMarkedForRemoval(true);
        }

        if (car.getBounds().intersects(coffee.getBounds())){
            health+=10;
            //System.out.println("yumm!");
            coffee.setMarkedForRemoval(true);
        }

        if (car.getBounds().intersects(tigerBlood.getBounds())) {
            //isTigerBlood = true;
            tigerBlood.setMarkedForRemoval(true);
        }

        if (car.getBounds().intersects(moose.getBounds())||health==0) {
            gameOver=true;

            //System.out.println("i hit the thing");
        }


        //if( ball.getBounds().intersects(ericsCar.getBounds())) {
        //    ball.collision(ericsCar);
        //} //else if( ball.getBounds().intersects(paddleRight.getBounds())) {
        //  ball.collision(paddleRight);
        //}

    }

    public void loopSound(final String name) {
        new Thread(new Runnable() {
            public void run() {
                ResourceLoader.getInstance().getSound(name).loop();
            }
        }).start();
    }


    public void game() {
        //loopSound("music.wav");
        /*************************************************************************************************************
         *                                                      GAME LOOP
         **************************************************************************************************************/
        usedTime = 0;
        trackScore();

        while (isVisible()) {
            long startTime = System.currentTimeMillis();
            checkCollision();

            usedTime = System.currentTimeMillis() - startTime;

            //calculate sleep time
            if (usedTime == 0) usedTime = 1;
            if (super.gameOver) {
                paintGameOver();
                continue;}
            int timeDiff = 1000 / DESIRED_FPS - (int) (usedTime);
            if (timeDiff > 0) {
                try {
                    Thread.sleep(timeDiff);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            int random = (int) (Math.random() * 1000);
            if (random == 700) {
//                Actor ufo = new Ufo(this);
//                ufo.setX(0);
//                ufo.setY(20);
//                ufo.setVx(1);
                actors.add(moose);
            }

            updateWorld();
            paintWorld();
            //System.out.println(actors.toString());
            usedTime = System.currentTimeMillis() - startTime;
        }
    }

    public void keyPressed(KeyEvent e) {
        keyPressedHandlerLeft.handleInput(e);

        if (e.getKeyCode() == KeyEvent.VK_K) {
            Actor.debugCollision = !Actor.debugCollision;
        }

        //keyPressedHandlerRight.handleInput(e);
    }

    public void keyReleased(KeyEvent e) {
        keyReleasedHandlerLeft.handleInput(e);
        //keyReleasedHandlerRight.handleInput(e);
    }

    public void keyTyped(KeyEvent e) {
    }


    public void paintGameOver() {
        Graphics g = strategy.getDrawGraphics();
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());

        // paintScore(g);

        //about 310 pixels wide
        g.setFont(new Font("Arial", Font.BOLD, 50));
        g.setColor(Color.RED);
        int xPos = getWidth() / 2 - 155;
        g.drawString("GAME OVER", (xPos < 0 ? 0 : xPos), getHeight() / 2);

        xPos += 30;
        g.setFont(new Font("Arial", Font.BOLD, 30));
        g.drawString("ENTER: try again", (xPos < 0 ? 0 : xPos), getHeight() / 2 + 50);

        strategy.show();
    }

    public void paintMainMenu() {

    }

    public void painOptionsMenu() {

    }

    public void paintCustomizationMenu() {

    }

    public void paintPauseMenu() {

    }
}