package game;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.Timer;


import javax.swing.*;

import actors.*;

public class MooseTheGame extends Stage implements KeyListener {

    public enum eGameState {
        GS_Playing,
        GS_Paused,
        GS_MainMenu,
        GS_GameOver,
        GS_Options,
        GS_AudioOptions,
        GS_Customizations,
        GS_HighScore,
        GS_Controls,
        GS_VideoOptions
    }


    private static final long serialVersionUID = 1L;

    private InputHandler keyPressedHandlerLeft;
    private InputHandler keyReleasedHandlerLeft;
    private ArrayList<String> factList;

    public long usedTime; //time taken per game step
    public BufferStrategy strategy;     //double buffering strategy
    public int roadHorizontalOffset;

    private JFrame frame;
    private Car car;
    private int score;
    private boolean hitBlood = false;

    private eGameState gameState;

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

        keyPressedHandlerLeft = new InputHandler(this, car);
        keyPressedHandlerLeft.action = InputHandler.Action.PRESS;
        keyReleasedHandlerLeft = new InputHandler(this, car);
        keyReleasedHandlerLeft.action = InputHandler.Action.RELSEASE;
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

    private void checkCollision() throws IOException {


        for (int i = 0; i < actors.size(); i++) {

            if (actors.get(i) instanceof Moose) {
                if (car.getBounds().intersects(actors.get(i).getBounds())) {
                    factList = getFact();
                    saveScore(score);
                    gameState = eGameState.GS_GameOver;
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
                    car.loseHealth(1);
                    // System.out.println("");


                    if (car.getCurrentHealth() == 0) {
                        factList = getFact();
                        saveScore(score);
                        gameState = eGameState.GS_GameOver;

                    }

                }
            }

            if (actors.get(i) instanceof Coffee) {
                if (car.getBounds().intersects(actors.get(i).getBounds())) {
                    score += 5;
                    actors.get(i).setMarkedForRemoval(true);
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


    public void game() throws IOException {

        /*************************************************************************************************************
         *                                                      GAME LOOP
         **************************************************************************************************************/
        inGame();
        usedTime = 0;
        trackScore();
        gameState = eGameState.GS_MainMenu;

        while (isVisible()) {
            long startTime = System.currentTimeMillis();

            usedTime = System.currentTimeMillis() - startTime;

            //calculate sleep time
            if (usedTime == 0) usedTime = 1;

            int timeDiff = 1000 / DESIRED_FPS - (int) (usedTime);
            if (timeDiff > 0) {
                try {
                    Thread.sleep(timeDiff);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if (gameState == eGameState.GS_Playing) {

                checkCollision();
                actorGenerator();
                updateWorld();
                paintWorld();

            } else if (gameState == eGameState.GS_MainMenu) {
                paintMainMenu();
            } else if (gameState == eGameState.GS_GameOver) {

                paintGameOver();

            } else if (gameState == eGameState.GS_Options) {
                paintOptionsMenu();
            } else if (gameState == eGameState.GS_VideoOptions) {
                paintVideoOptionsMenu();
            } else if (gameState == eGameState.GS_Paused) {
                paintPauseMenu();
            } else if (gameState == eGameState.GS_Customizations) {
                paintCustomizationMenu();
            } else if (gameState == eGameState.GS_AudioOptions) {
                paintAudioOptionsMenu();
            } else if (gameState == eGameState.GS_Controls) {
                paintControlsOptionsMenu();
            } else if (gameState == eGameState.GS_HighScore) {
                paintHighscoreMenu();
            }

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

    public void paintHighscoreMenu() throws IOException {
        onHighscoreMenu();
        Graphics g = strategy.getDrawGraphics();
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());

        g.drawImage(ResourceLoader.getInstance().getSprite("highscoreTitle.png"), 190, 30, this);
        g.drawImage(ResourceLoader.getInstance().getSprite("backButton.png"), 715, 470, this);


        // TODO: 2019-08-10 find a better way to make the strings for the score
        String frist = "1st: "+ Integer.toString(getScores().get(2));
        String second = "2nd: "+ Integer.toString(getScores().get(1));
        String third = "3rd: "+ Integer.toString(getScores().get(0));

        g.setColor(new Color(10, 255, 14));
        g.setFont(new Font("BitPotionExt", 0, 50));
        g.drawString(frist, 200, 250);
        g.drawString(second, 200, 300);
        g.drawString(third, 200, 350);
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
        g.drawString("Did You Know?", 350, 240);
        g.setFont(new Font("BitPotionExt", 0, 32));
        int y = 290;

        for (String fact : factList) {
            g.drawString(fact, 250, y);
            y += 30;

        }

        g.setFont(new Font("BitPotionExt", 0, 23));
        g.drawString("https://www.flr.gov.nl.ca/wildlife/moose_vehicle_awareness.html", 250, 440);
    }


    public ArrayList<String> getFact() {
        ArrayList<String> factBuilder = new ArrayList<>();
        Random randy = new Random();
        int i = randy.nextInt(6);

        if (i == 0) {
            factBuilder.add("Even in areas with very low moose density,");
            factBuilder.add("moose are still attracted to roadways and");
            factBuilder.add("can pose a hazard to drivers.");
        } else if (i == 1) {
            factBuilder.add("Most accidents occur on clear nights and on");
            factBuilder.add("straight road sections. ");
            factBuilder.add("Don't let yourself be distracted.");
        } else if (i == 2) {
            factBuilder.add("More than 70% of accidents occur between");
            factBuilder.add("May and October. The most critical");
            factBuilder.add("months are June, July, and August.");
            factBuilder.add("However, moose accidents can occur all year.");
        } else if (i == 3) {
            factBuilder.add("More accidents occur on certain sections of");
            factBuilder.add("the highway. These areas are marked with");
            factBuilder.add("moose crossing warning signs.");
        } else if (i == 4) {
            factBuilder.add("Moose accidents are estimated to cost more");
            factBuilder.add("than $1 million annually.");
        } else if (i == 5) {
            factBuilder.add("Care and attention when driving is your");
            factBuilder.add("best defense against moose-vehicle accidents.");
        }
        return factBuilder;

    }


    public void resetGame() {
        gameState = eGameState.GS_Playing;
        score = 0;
        actors.clear();
        initWorld();
    }

    public void keyPressed(KeyEvent e) {
        try {
            keyPressedHandlerLeft.handleInput(e);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        if (e.getKeyCode() == KeyEvent.VK_K) {
            Actor.debugCollision = !Actor.debugCollision;
        }

        if (e.getKeyChar() == 'R' || e.getKeyChar() == 'r') {
            if (gameState == eGameState.GS_GameOver) {
                resetGame();
            }

            //else if (stage.game) {
            //   stage.paintPauseMenu();
            //}
        } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            if (gameState == eGameState.GS_Playing) {
                gameState = eGameState.GS_Paused;
            } else if (gameState == eGameState.GS_Paused) {
                gameState = eGameState.GS_Playing;
            }
        } else if (e.getKeyChar() == 'A' || e.getKeyChar() == 'a') {
            if (gameState == eGameState.GS_Options) {
                gameState = eGameState.GS_AudioOptions;
            }
        } else if (e.getKeyChar() == 'B' || e.getKeyChar() == 'b') {
            if (gameState == eGameState.GS_Options || gameState == eGameState.GS_Customizations || gameState == eGameState.GS_HighScore) {
                gameState = eGameState.GS_MainMenu;
            } else if (gameState == eGameState.GS_Controls || gameState == eGameState.GS_AudioOptions
                    || gameState == eGameState.GS_VideoOptions) {
                gameState = eGameState.GS_Options;
            }
        } // End B

        else if (e.getKeyChar() == 'C' || e.getKeyChar() == 'c') {
            if (gameState == eGameState.GS_MainMenu) {
                gameState = eGameState.GS_Customizations;
            } else if (gameState == eGameState.GS_Options) {
                gameState = eGameState.GS_Customizations;
            }
        } // End C

        else if (e.getKeyChar() == 'H' || e.getKeyChar() == 'h') {
            if (gameState == eGameState.GS_MainMenu) {
                gameState = eGameState.GS_HighScore;
            }
        } // End H


        else if (e.getKeyChar() == 'M' || e.getKeyChar() == 'm') {
            if (gameState == eGameState.GS_GameOver) {
                gameState = eGameState.GS_MainMenu;
            }
        } // End M

        else if (e.getKeyChar() == 'O' || e.getKeyChar() == 'o') {
            if (gameState == eGameState.GS_MainMenu) {
                gameState = eGameState.GS_Options;
            }
        } // End O

        else if (e.getKeyChar() == 'P' || e.getKeyChar() == 'p') {
            // TODO: Game does not init properly when started this way ex. no controls, cant close window
            if (gameState == eGameState.GS_MainMenu) {
                gameState = eGameState.GS_Playing;
                resetGame();
            }
        } // End P

        else if (e.getKeyChar() == 'Q' || e.getKeyChar() == 'q') {
            if (gameState == eGameState.GS_MainMenu || gameState == eGameState.GS_Paused
                    || gameState == eGameState.GS_GameOver) {
                System.exit(0);
            }
        } // End Q

        else if (e.getKeyChar() == 'V' || e.getKeyChar() == 'v') {
            if (gameState == eGameState.GS_Options) {
                gameState = eGameState.GS_VideoOptions;
            }
        }
    }

    public void keyReleased(KeyEvent e) {
        try {
            keyReleasedHandlerLeft.handleInput(e);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void keyTyped(KeyEvent e) {
    }

    public void saveScore(int newScore) throws IOException {

        String file = "highscore.dat";
        ArrayList<Integer> scoreBored = getScores();

        for (int i = 0; i < 2; i++) {
            if (newScore > scoreBored.get(i)) {
                scoreBored.remove(0);
                scoreBored.add(newScore);
                Collections.sort(scoreBored);
                break;
            }
        }
        try {

            BufferedWriter output = new BufferedWriter(new FileWriter(file, true));
            clearTheFile();
            output.flush();
            for (int i = 0; i < 3; i++) {
                output.append(String.valueOf(scoreBored.get(i)));
                output.newLine();
            }
            output.close();
        } catch (IOException ex1) {
            System.out.printf("ERROR writing score to file: %s\n", ex1);
        }
    }

    public ArrayList<Integer> getScores() throws IOException {
        ArrayList<Integer> scoreBored = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader("highscore.dat"));
        String line = reader.readLine();
        while (line != null) {
            scoreBored.add(Integer.parseInt(line.trim()));
            line = reader.readLine();
        }
        reader.close();
        return scoreBored;

    }

    public static void clearTheFile() throws IOException {
        FileWriter fwOb = new FileWriter("highscore.dat", false);
        PrintWriter pwOb = new PrintWriter(fwOb, false);
        pwOb.flush();
        pwOb.close();
        fwOb.close();
    }

    public static void main(String[] args) throws IOException {
        MooseTheGame mooseGame = new MooseTheGame();
        //mooseGame.paintMainMenu();
        mooseGame.game();
    }

}