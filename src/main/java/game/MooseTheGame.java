package game;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;


import javax.swing.JFrame;
import javax.swing.JPanel;

import actors.*;

public class MooseTheGame extends Stage implements KeyListener {

    private static final long serialVersionUID = 1L;

    private InputHandler keyPressedHandlerLeft;
    private InputHandler keyReleasedHandlerLeft;

    private InputHandler keyPressedHandlerRight;
    private InputHandler keyReleasedHandlerRight;

    public long usedTime;//time taken per game step
    public BufferStrategy strategy;     //double buffering strategy
    public int roadHorizontalOffset;

    //private TNT tnt;
    private Timbit timbit;
    private Coffee coffee;
    private Moose moose;
    private PotHole potHole;
    private int health = 100;


  //  private Splat splat;
    // private int splatFrames;


    private Car car;
    //private Paddle paddleRight;
    //rivate Ball ball;

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
        // ericsCar = new EricsCar(this, EricsCar.ePlayerNumber.PN_ONE);
        car = new Car(this);
        actors.add(car);

        // tnt = new TNT(this);
        //paddleRight = new Paddle(this, Paddle.ePlayerNumber.PN_TWO);
        //ball = new Ball(this);

        timbit = new Timbit(this);
        actors.add(timbit);
        coffee = new Coffee(this);
        moose = new Moose(this);
        actors.add(moose);
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

        // car.paint(g);

        //tnt.paint(g);
        // moose.paint(g);
        // timbit.paint(g);

        coffee.paint(g);
//        if (splat != null) {
//            splat.paint(g);
//        }

        //.paint(g);
        //ball.paint(g);
        paintFPS(g);
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

    public void paint(Graphics g) {
    }

    public void updateWorld() {

        roadHorizontalOffset += 10;
        roadHorizontalOffset %= Stage.WIDTH;
// TODO: 2019-08-08  for loop size of actors array  check each for isMarkedForRemoval then actors.remove(i) then i--
        car.update();
        moose.update();
        timbit.update();
        coffee.update();
        // tnt.update();
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

        // TODO: 2019-08-07  make timbit disapear once hit
        // TODO: 2019-08-07 make coffee disapear once hit
        if (car.getBounds().intersects(timbit.getBounds())) {
            health += 10;
            System.out.println("yumm!");
            timbit.setMarkedForRemoval(true);//dose not work :(

        }
        if (car.getBounds().intersects(moose.getBounds()) || health == 0) {
            gameOver = true;

            System.out.println("i hit the thing");
//            if( splat == null) {
//                splat = new Splat(this);
//                splat.setX(car.getX());
//                splat.setY(car.getY());
//
//                splatFrames = 0;
            // }
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

        while (isVisible()) {
            long startTime = System.currentTimeMillis();
            checkCollision();

            usedTime = System.currentTimeMillis() - startTime;

            //calculate sleep time
            if (usedTime == 0) usedTime = 1;
            if (super.gameOver) {
                paintGameOver();
                continue;
            }
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
            System.out.println(actors.toString());
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