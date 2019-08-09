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

    public long usedTime; //time taken per game step
    public BufferStrategy strategy;     //double buffering strategy
    public int roadHorizontalOffset;

    private JFrame frame;
    private Car car;
    private int score;
    private boolean hitBlood = false;

    public MooseTheGame() {
        //init the UI
        setBounds(0, 0, Stage.WIDTH, Stage.HEIGHT);
        setBackground(new Color(73, 62, 92));

        JPanel panel = new JPanel();
        panel.setPreferredSize(new Dimension(Stage.WIDTH, Stage.HEIGHT));
        panel.setLayout(null);

        panel.add(this);

        frame = new JFrame("MOOSE: The Game");
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
        ResourceLoader.createFont();

        keyPressedHandlerLeft = new InputHandler(this, car);
        keyPressedHandlerLeft.action = InputHandler.Action.PRESS;
        keyReleasedHandlerLeft = new InputHandler(this, car);
        keyReleasedHandlerLeft.action = InputHandler.Action.RELSEASE;
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
        drawHealthBar(car.getCurrentHealth());


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
        g.setFont(new Font("BitPotionExt", 0, 48));
        g.drawString(String.valueOf(score), 310, 545);
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

    public void drawHealthBar(int health) {
        Graphics g = strategy.getDrawGraphics();
        int fillAmount = (int) (car.getCurrentHealth() * 2.5);

        g.setColor(new Color(81, 217, 61));
        g.fillRect(38, 523, 250, 27);
        g.setColor(Color.BLACK);
        g.fillRect(40, 525, 246, 23);
        g.setColor(new Color(81, 217, 61));
        g.fillRect(38, 523, fillAmount, 27);

        g.drawImage(ResourceLoader.getInstance().getSprite("healthPlus.png"), 10, 521, this);
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
            //System.out.println(actors.get(i).toString());
            if (actors.get(i).isMarkedForRemoval()) {
                actors.remove(i);
                i--;
            }

            actors.get(i).update();
        }
    }

    private void checkCollision() {

// TODO:  fix removal of coffee
// TODO:  potholes do not disapear when you hit them and nether should ours
        for (int i = 0; i < actors.size(); i++) {

            if (actors.get(i) instanceof Moose) {
                if (car.getBounds().intersects(actors.get(i).getBounds())) {
                    paintGameOver();
                }
            }

            if (actors.get(i) instanceof Timbit) {
                if (car.getBounds().intersects(actors.get(i).getBounds())) {
                    car.gainHealth(15);
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
                    car.loseHealth(25);
                    System.out.println("");
                    actors.get(i).setMarkedForRemoval(true);

                    if (car.getCurrentHealth() == 0) {
                        paintGameOver();
                    }
                    //actors.get(i).setMarkedForRemoval(true);
                }

                if (actors.get(i) instanceof Coffee) {
                    if (car.getBounds().intersects(actors.get(i).getBounds())) {
                        score += 5;
                        actors.get(i).setMarkedForRemoval(true);
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
        inGame();
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
                //continue;
                break; //TODO: This lets game over screen go to main menu, but when play is pressed it goes back to the game over screen
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

    public void paintGameOver() {
        endGame();
        Graphics g = strategy.getDrawGraphics();
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());

        g.setColor(new Color(48, 48, 48));
        g.fillRect(230, 190, 520, 270);

        g.setColor(Color.WHITE);
        g.fillRect(240, 200, 500, 250);

        g.drawImage(ResourceLoader.getInstance().getSprite("goTitle.png"), 190, 30, this);
        g.drawImage(ResourceLoader.getInstance().getSprite("retryButton.png"), 18, 475, this);
        g.drawImage(ResourceLoader.getInstance().getSprite("mainButton.png"), 343, 475, this);
        g.drawImage(ResourceLoader.getInstance().getSprite("quitButton.png"), 667, 475, this);

        writeFact();

        strategy.show();
    }

    public void paintMainMenu() {
        onMainMenu();
        Graphics g = strategy.getDrawGraphics();
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());

        g.drawImage(ResourceLoader.getInstance().getSprite("title.png"), 190, 30, this);
        g.drawImage(ResourceLoader.getInstance().getSprite("playButton.png"), 18, 250, this);
        g.drawImage(ResourceLoader.getInstance().getSprite("highscoreButton.png"), 343, 250, this);
        g.drawImage(ResourceLoader.getInstance().getSprite("customizeButton.png"), 667, 250, this);
        g.drawImage(ResourceLoader.getInstance().getSprite("optionsButton.png"), 165, 380, this);
        g.drawImage(ResourceLoader.getInstance().getSprite("quitButton.png"), 515, 380, this);

        strategy.show();
    }

    public void paintCustomizationMenu() {
        onCustomizationMenu();
        Graphics g = strategy.getDrawGraphics();
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());

        g.drawImage(ResourceLoader.getInstance().getSprite("customizeTitle.png"), 190, 30, this);
        g.drawImage(ResourceLoader.getInstance().getSprite("backButton.png"), 715, 470, this);
        strategy.show();
    }

    public void paintPauseMenu() {
        onPauseMenu();
        Graphics g = strategy.getDrawGraphics();
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());

        g.drawImage(ResourceLoader.getInstance().getSprite("pausedTitle.png"), 190, 30, this);
        g.drawImage(ResourceLoader.getInstance().getSprite("resumeButton.png"), 18, 300, this);
        g.drawImage(ResourceLoader.getInstance().getSprite("mainButton.png"), 343, 300, this);
        g.drawImage(ResourceLoader.getInstance().getSprite("quitButton.png"), 667, 300, this);

        strategy.show();
    }

    public void paintOptionsMenu() {
        onOptionsMenu();
        Graphics g = strategy.getDrawGraphics();
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());

        g.drawImage(ResourceLoader.getInstance().getSprite("optionsTitle.png"), 190, 30, this);
        g.drawImage(ResourceLoader.getInstance().getSprite("controlsButton.png"), 18, 300, this);
        g.drawImage(ResourceLoader.getInstance().getSprite("videoButton.png"), 343, 300, this);
        g.drawImage(ResourceLoader.getInstance().getSprite("audioButton.png"), 667, 300, this);
        g.drawImage(ResourceLoader.getInstance().getSprite("backButton.png"), 715, 470, this);

        strategy.show();
    }

    public void paintAudioOptionsMenu() {
        onAudioOptionsMenu();
        Graphics g = strategy.getDrawGraphics();
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());

        g.drawImage(ResourceLoader.getInstance().getSprite("audioTitle.png"), 190, 30, this);
        g.drawImage(ResourceLoader.getInstance().getSprite("backButton.png"), 715, 470, this);

        strategy.show();
    }

    public void paintVideoOptionsMenu() {
        onVideoOptionsMenu();
        Graphics g = strategy.getDrawGraphics();
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());

        g.drawImage(ResourceLoader.getInstance().getSprite("videoTitle.png"), 190, 30, this);
        g.drawImage(ResourceLoader.getInstance().getSprite("backButton.png"), 715, 470, this);

        strategy.show();
    }

    public void paintControlsOptionsMenu() {
        onControlsOptionsMenu();
        Graphics g = strategy.getDrawGraphics();
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());

        g.drawImage(ResourceLoader.getInstance().getSprite("controlsTitle.png"), 190, 30, this);
        g.drawImage(ResourceLoader.getInstance().getSprite("backButton.png"), 715, 470, this);

        strategy.show();
    }

    public void paintHighscoreMenu() {
        onHighscoreMenu();
        Graphics g = strategy.getDrawGraphics();
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());

        g.drawImage(ResourceLoader.getInstance().getSprite("highscoreTitle.png"), 190, 30, this);
        g.drawImage(ResourceLoader.getInstance().getSprite("backButton.png"), 715, 470, this);

        strategy.show();
    }

    private void actorGenerator() {


        Random randy = new Random();
        int randNum = randy.nextInt(10000);
        int picker = 0;
        if (randNum < 50) {
            picker = 1;
        } else if (randNum > 100 && randNum < 300) {
            picker = 5;
        } else if (randNum > 300 && randNum < 350) {
            picker = 2;
        } else if (randNum > 350 && randNum < 400) {
            picker = 3;
        } else if (randNum > 400 && randNum < 415) {
            picker = 4;
        }
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

    public void writeFact() {
        Random randy = new Random();
        Graphics g = strategy.getDrawGraphics();

        g.setColor(new Color(11, 33, 64));
        g.setFont(new Font("BitPotionExt", 0, 60));
        g.drawString("Did You Know?", 376, 240);
        g.setFont(new Font("BitPotionExt", 0, 32));

        int i = randy.nextInt(6);

        if (i == 0) {
            g.drawString("Even in areas with very low moose density,", 282, 290);
            g.drawString("moose are still attracted to roadways and", 282, 320);
            g.drawString("can pose a hazard to drivers.", 282, 350);
        } else if (i == 1) {
            g.drawString("Most accidents occur on clear nights and on", 282, 290);
            g.drawString("straight road sections. ", 282, 320);
            g.drawString("Don't let yourself be distracted.", 282, 350);
        } else if (i == 2) {
            g.drawString("More than 70% of accidents occur between", 282, 290);
            g.drawString("May and October. The most critical", 282, 320);
            g.drawString("months are June, July, and August.", 282, 350);
            g.drawString("However, moose accidents can occur all year.", 282, 380);
        } else if (i == 3) {
            g.drawString("More accidents occur on certain sections of", 282, 290);
            g.drawString("the highway. These areas are marked with", 282, 320);
            g.drawString("moose crossing warning signs.", 282, 350);
        } else if (i == 4) {
            g.drawString("Moose accidents are estimated to cost more", 282, 290);
            g.drawString("than $1 million annually.", 282, 320);
        } else if (i == 5) {
            g.drawString("Care and attention when driving is your", 282, 290);
            g.drawString("best defense against moose-vehicle accidents.", 282, 320);
        }

        g.setFont(new Font("BitPotionExt", 0, 23));
        g.drawString("https://www.flr.gov.nl.ca/wildlife/moose_vehicle_awareness.html", 282, 440);
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

    public static void main(String[] args) {
        MooseTheGame mooseGame = new MooseTheGame();
        //mooseGame.paintMainMenu();
        mooseGame.game();
    }

}