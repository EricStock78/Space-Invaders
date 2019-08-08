package game;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;


import javax.swing.*;

import actors.*;

public class MooseTheGame extends Stage implements KeyListener {

    private static final long serialVersionUID = 1L;

    private InputHandler keyPressedHandlerLeft;
    private InputHandler keyReleasedHandlerLeft;

    private InputHandler keyPressedHandlerRight;
    private InputHandler keyReleasedHandlerRight;
    private BufferedImage playBtn, playTile; //playBtn cache
    private int backgroundY; //playBtn cache position

    public long usedTime; //time taken per game step
    public BufferStrategy strategy;     //double buffering strategy
    public int roadHorizontalOffset;

    private JFrame frame;
    private Car car;
    private int health = 100;
    private int score;
    private boolean hitBlood = false;


    private Splat splat;
    private int splatFrames;

    private JButton start;
    private JButton options;
    private JButton customize;
    private JButton exit;

    public MooseTheGame() {
        //init the UI
        setBounds(0, 0, Stage.WIDTH, Stage.HEIGHT);
        setBackground(Color.black);

        JPanel panel = new JPanel();
        panel.setPreferredSize(new Dimension(Stage.WIDTH, Stage.HEIGHT));
        panel.setLayout(null);

        panel.add(this);

        frame = new JFrame("Moose The Game");
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
        //paintMainMenu();

        keyPressedHandlerLeft = new InputHandler(this, car);
        keyPressedHandlerLeft.action = InputHandler.Action.PRESS;
        keyReleasedHandlerLeft = new InputHandler(this, car);
        keyReleasedHandlerLeft.action = InputHandler.Action.RELSEASE;


    }

    public void paintMainMenu() {
        start = new JButton();
        start.setIcon(new ImageIcon("playButton.png"));
        frame.add(start);
    }

    public void initWorld() {
        car = new Car(this);

        actors.add(car);


    }

    public void paintWorld() {

        //get the graphics from the buffer
        Graphics g = strategy.getDrawGraphics();

        //init image to playBtn
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());

        g.drawImage(ResourceLoader.getInstance().getSprite("road.png"), -roadHorizontalOffset, 0, this);

        g.drawImage(ResourceLoader.getInstance().getSprite("road.png"), -roadHorizontalOffset + Stage.WIDTH, 0, this);

        //load subimage from the playBtn

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
                    if (hitBlood) {
                        score += 20;
                    } else {
                        score += 10;
                    }
                }
            }, 2000, 2000);
        } catch (Exception e) {
            timer.cancel();
        }
    }

    public void setTigerBlood() {
        final Timer tigerTimer = new Timer();

        try {
            hitBlood = true;

            tigerTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    hitBlood = false;
                    tigerTimer.cancel();
                }
            }, 10000, 1);
        } catch (Exception e) {
            tigerTimer.cancel();
        }
    }


    public void paint(Graphics g) {
    }

    public void updateWorld() {

        roadHorizontalOffset += 10;
        roadHorizontalOffset %= Stage.WIDTH;

        for (int i = 0; i < actors.size(); i++) {
            System.out.println(actors.get(i).toString());
            if (actors.get(i).isMarkedForRemoval()) {
                actors.remove(i);
                i--;
            }
            actors.get(i).update();
        }

    }

    private void checkCollision() {

// TODO: 2019-08-08 fix removal of coffee
        for (int i = 0; i < actors.size(); i++) {
            if (actors.get(i) instanceof Moose) {
                if (car.getBounds().intersects(actors.get(i).getBounds())) {
                    gameOver = true;

                }
            }

            if (actors.get(i) instanceof Timbit) {
                if (car.getBounds().intersects(actors.get(i).getBounds())) {
                    health += 10;
                    actors.get(i).setMarkedForRemoval(true);
                }

            }
            if (actors.get(i) instanceof TigerBlood) {
                if (car.getBounds().intersects(actors.get(i).getBounds())) {
                    setTigerBlood();
                    actors.get(i).setMarkedForRemoval(true);
                }

            }
            if (actors.get(i) instanceof PotHole) {
                if (car.getBounds().intersects(actors.get(i).getBounds())) {
                    health-=5;
                    actors.get(i).setMarkedForRemoval(true);


                }
                if (actors.get(i) instanceof Coffee) {
                    if (car.getBounds().intersects(actors.get(i).getBounds())) {
                        actors.get(i).setMarkedForRemoval(true);
                        System.out.println("hit a coffee");
                    }

                }

            }
        }

    }

    public void loopSound(final String name) {
        new Thread(new Runnable() {
            public void run() {
                ResourceLoader.getInstance().getSound(name).loop();
            }
        }).start();
    }


    public void game() {

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


            actorGenerator();
            updateWorld();
            paintWorld();

            usedTime = System.currentTimeMillis() - startTime;
        }
    }

    public void keyPressed(KeyEvent e) {
        keyPressedHandlerLeft.handleInput(e);

        if (e.getKeyCode() == KeyEvent.VK_K) {
            Actor.debugCollision = !Actor.debugCollision;
        }


    }

    public void keyReleased(KeyEvent e) {
        keyReleasedHandlerLeft.handleInput(e);

    }

    public void keyTyped(KeyEvent e) {
    }


    public void paintGameOver() {
        Graphics g = strategy.getDrawGraphics();
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());


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


    public void painOptionsMenu() {

    }

    public void paintCustomizationMenu() {

    }

    public void paintPauseMenu() {

    }

    private void actorGenerator() {
        // TODO: 2019-08-08 adjust for better spawn rates--> if randy.nextInt() between x and y picker = a

        Random randy = new Random();
        int picker = randy.nextInt(1000);

        switch (picker) {
            case 1:
                Moose moose = new Moose(this);
                actors.add(moose);

                break;
            case 2:
                Timbit timbit = new Timbit(this);
                actors.add(timbit);
                break;

            case 3:
                Coffee coffee = new Coffee(this);
                actors.add(coffee);
                break;

            case 4:
                TigerBlood tigerBlood = new TigerBlood(this);
                actors.add(tigerBlood);
                break;
            case 5:
                PotHole potHole = new PotHole(this);
                actors.add(potHole);
                break;


        }
    }


}