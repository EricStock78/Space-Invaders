package game;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;
import java.io.*;
import java.util.*;
import java.util.Timer;

import javax.swing.*;

import actors.*;

/**
 * Moose: The Game
 * Moose: The Game is designed to be a fun way to raise awareness about driving safety in Newfoundland. The objective of
 * the game is to steer a car along a Newfoundland highway to get power-ups and avoid hazards on the road to get the
 * highest score possible, which increases by 10 every 2 seconds the player is alive. The game also includes an
 * educational component to raise road safety awareness in Newfoundland. After unsafe driving causes the player to lose,
 * they are presented with a driving safety tip on the game over screen.
 * Co-developed by
 * Emma Troke, Gabe Walsh, Greg Tracey
 */


public class MooseTheGame extends Stage implements KeyListener {
    /**
     * Enum Method used for changing the Game States
     */
    public enum eGameState { // Game states
        GS_Playing,
        GS_Paused,
        GS_MainMenu,
        GS_GameOver,
        GS_Customizations,
        GS_HighScore,
    }

    private static final long serialVersionUID = 1L;

    private InputHandler keyPressedHandlerLeft;
    private InputHandler keyReleasedHandlerLeft;
    private ArrayList<String> factList;
    private boolean sound;

    public long usedTime; //time taken per game step
    public BufferStrategy strategy;     //double buffering strategy
    public int roadHorizontalOffset;

    private JFrame frame;
    private Car car;
    private int carType;


    private int score;
    private boolean hitBlood = false;
    private boolean hitTire = false;
    private boolean isPaused = false;

    private Timer tigerTimer = new Timer();

    private eGameState gameState;

    /**
     * Moose The Game constructor:
     * Builds the game itself
     */
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
        ResourceLoader.createFont(this);

        keyPressedHandlerLeft = new InputHandler(this, car);
        keyPressedHandlerLeft.action = InputHandler.Action.PRESS;
        keyReleasedHandlerLeft = new InputHandler(this, car);
        keyReleasedHandlerLeft.action = InputHandler.Action.RELSEASE;
    }

    /**
     * Game
     * Launches the game.
     * Creates a basic game loop for the game to run around
     *
     * @throws IOException
     */
    public void game() throws IOException {
        /*************************************************************************************************************
         *                                                      GAME LOOP
         **************************************************************************************************************/
        usedTime = 0;
        score = 0;
        trackScore();
        gameState = eGameState.GS_MainMenu;

        ResourceLoader.getInstance().loopSound("hmm.wav");

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


            } else if (gameState == eGameState.GS_Paused) {
                paintPauseMenu();
            } else if (gameState == eGameState.GS_Customizations) {
                paintCustomizationMenu();

            } else if (gameState == eGameState.GS_HighScore) {
                paintHighscoreMenu();
            }

            usedTime = System.currentTimeMillis() - startTime;
        }
    }

    /**
     * InitWorld
     * Creates the game world.
     * Builds the objects to be used by the game on start
     */

    public void initWorld() {
        if (carType == 0) {
            carType = 1;
        }

        car = new Car(this, carType);
        actors.add(car);

        keyPressedHandlerLeft = new InputHandler(this, car);
        keyPressedHandlerLeft.action = InputHandler.Action.PRESS;
        keyReleasedHandlerLeft = new InputHandler(this, car);
        keyReleasedHandlerLeft.action = InputHandler.Action.RELSEASE;
    }

    /**
     * UpdateWorld
     * Updates the object values for each loop of the game
     */

    public void updateWorld() {

        roadHorizontalOffset += 10;
        roadHorizontalOffset %= Stage.WIDTH;

        for (int i = 0; i < actors.size(); i++) {
            if (actors.get(i).isMarkedForRemoval()) {
                actors.remove(i);
                i--;
            }
            actors.get(i).update();
        }
    }

    /**
     * Checks if the car collides with any other objects and takes appropriate action
     *
     * @throws IOException
     */
    private void checkCollision() throws IOException {

        for (int i = 0; i < actors.size(); i++) {

            if (actors.get(i) instanceof Moose) {
                if (car.getBounds().intersects(actors.get(i).getBounds())) {
                    actors.get(i).playSound("explosion.wav");

                    factList = getFact();
                    saveScore(score);
                    gameState = eGameState.GS_GameOver;
                }
            }

            if (actors.get(i) instanceof Timbit) {
                if (car.getBounds().intersects(actors.get(i).getBounds())) {
                    car.gainHealth(15);
                    actors.get(i).playSound("bite.wav");
                    actors.get(i).setMarkedForRemoval(true);
                }
            }

            if (actors.get(i) instanceof TigerBlood) {
                if (car.getBounds().intersects(actors.get(i).getBounds())) {
                    actors.get(i).playSound("tigger.wav");
                    setTigerBlood();
                    actors.get(i).setMarkedForRemoval(true);
                }
            }

            if (actors.get(i) instanceof PotHole) {
                if (car.getBounds().intersects(actors.get(i).getBounds())) {
                    actors.get(i).playSound("rocky.wav");
                    if (hitTire) {
                        continue;
                    } else {
                        car.loseHealth(1);
                    }

                    if (car.getCurrentHealth() == 0) {
                        factList = getFact();
                        saveScore(score);
                        gameState = eGameState.GS_GameOver;
                    }
                }
            }

            if (actors.get(i) instanceof Tire) {
                if (car.getBounds().intersects(actors.get(i).getBounds())) {
                    actors.get(i).playSound("impact.wav");
                    setTire();
                    actors.get(i).setMarkedForRemoval(true);
                }
            }

            if (actors.get(i) instanceof Coffee) {
                if (car.getBounds().intersects(actors.get(i).getBounds())) {
                    actors.get(i).playSound("slurp.wav");
                    if (hitBlood) {
                        score += 10;
                    } else {
                        score += 5;
                    }

                    actors.get(i).setMarkedForRemoval(true);
                }
            }
            if (actors.get(i) instanceof EnemyCar) {
                if (car.getBounds().intersects(actors.get(i).getBounds())) {
                    actors.get(i).playSound("explosion.wav");
                    factList = getFact();
                    saveScore(score);
                    gameState = eGameState.GS_GameOver;
                }
            }
        }
    }
    /**
     * Paint the Gameplay Screen
     */
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
        car.paint(g);

        // Paint the graphics
        paintScore(g, score);
        paintHealthBar(g, car.getCurrentHealth());
        paintPowerUps(g);

        //swap buffer
        strategy.show();
    }

    /**
     * Randomly generate a hazard or powerup to appear on gameplay screen
     */
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
        } else if (randNum > 415 && randNum < 430) {
            picker = 6;
        } else if (randNum > 430 && randNum < 445) {
            picker = 7;
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
            case 6:
                Tire tire = new Tire(this);
                actors.add(tire);
                break;
            case 7:
                int enemyCarType = randy.nextInt(5);
                int lane = randy.nextInt(2);
                EnemyCar enemyCar = new EnemyCar(this, enemyCarType, lane);
                actors.add(enemyCar);
                break;

        }
    }

    /**
     * Track the player's score through the game
     */
    public void trackScore() {
        Timer timer = new Timer();

        try {
            // Create a new task for the timer to run
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (isPaused) {
                    } // Do nothing is game is paused
                    else if (hitBlood) { // Inc by 20 if player hit tiger blood
                        score += 20;
                    } else { // Default inc by 10
                        score += 10;
                    }
                }
            }, 2000, 2000); // Occurs every 2 sec after 2 sec
        } catch (Exception e) {
            timer.cancel(); // Cancel the timer if something goes wrong
        }
    }

    /**
     * Sets behaviour for when car collides with tiger blood
     */
    public void setTigerBlood() {
        final Timer tigerTimer = new Timer();

        try {
            hitBlood = true; // When car collides set to true

            tigerTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    // When timer runs set to false and turn off timer
                    hitBlood = false;
                    tigerTimer.cancel();
                }
            }, 10000, 1); // Shuts off after a 10 sec delay
        } catch (Exception e) {
            tigerTimer.cancel(); // Turn off timer if something goes wrong
        }
    }

    /**
     * Sets behaviour for when car collides with a tire
     */
    public void setTire() {
        final Timer tireTimer = new Timer();

        try {
            hitTire = true; // When car collides set to true

            tireTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    // When timer runs set to false and turn off timer
                    hitTire = false;
                    tireTimer.cancel();
                }
            }, 6000, 1); // Shuts off after 6 sec
        } catch (Exception e) {
            tireTimer.cancel(); // Turn off timer if something goes wrong
        }
    }


    /**
     * Reset current game
     */
    public void resetGame() {
        gameState = eGameState.GS_Playing;
        score = 0;
        hitBlood = false;
        hitTire = false;
        tigerTimer.cancel();
        actors.clear();
        initWorld();
    }

    /**
     * Paint the player's current score onto the gameplay screen
     *
     * @param g     Graphics
     * @param score Player's current score
     */
    public void paintScore(Graphics g, int score) {
        g.setColor(Color.RED);
        g.setFont(new Font("BitPotionExt", 0, 48));
        g.drawString(String.valueOf(score), 310, 545);
    }

    /**
     * Paint the Game Over screen
     */
    public void paintGameOver() {
        endGame();
        Graphics g = strategy.getDrawGraphics();
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());
        drawBox(g);

        paintHeader(g, "goTitle");
        g.drawImage(ResourceLoader.getInstance().getSprite("retryButton.png"), 18, 475, this);
        g.drawImage(ResourceLoader.getInstance().getSprite("mainButton.png"), 343, 475, this);
        g.drawImage(ResourceLoader.getInstance().getSprite("quitButton.png"), 667, 475, this);

        writeFact(); // Write a safety fact to thr screen

        strategy.show();
    }

    /**
     * Paint the Main Menu
     */
    public void paintMainMenu() {
        onMainMenu();
        Graphics g = strategy.getDrawGraphics();
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());

        paintHeader(g, "title");
        g.drawImage(ResourceLoader.getInstance().getSprite("playButton.png"), 18, 250, this);
        g.drawImage(ResourceLoader.getInstance().getSprite("highscoreButton.png"), 343, 250, this);
        g.drawImage(ResourceLoader.getInstance().getSprite("customizeButton.png"), 667, 250, this);
        g.drawImage(ResourceLoader.getInstance().getSprite("quitButton.png"), 343, 380, this);

        strategy.show();
    }

    /**
     * Paint the customization menu
     */
    public void paintCustomizationMenu() {
        onCustomizationMenu();
        Graphics g = strategy.getDrawGraphics();
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());
        drawBox(g);
        g.setColor(new Color(0, 0, 0));
        int stringx=300;
        g.setFont(new Font("BitPotionExt", 0, 50));
        g.drawString("press 1 for:", stringx, 225);
        g.drawString("press 2 for:", stringx, 275);
        g.drawString("press 3 for:", stringx, 325);
        g.drawString("press 4 for:", stringx, 375);
        g.drawString("press 5 for:", stringx, 425);
        paintHeader(g, "customizeTitle");
        paintBackButton(g);
        int carx=500;

        g.drawImage(ResourceLoader.getInstance().getSprite("car1.png"), carx, 200, this);
        g.drawImage(ResourceLoader.getInstance().getSprite("car2.png"), carx, 250, this);
        g.drawImage(ResourceLoader.getInstance().getSprite("car3.png"), carx, 300, this);
        g.drawImage(ResourceLoader.getInstance().getSprite("car4.png"), carx, 350, this);
        g.drawImage(ResourceLoader.getInstance().getSprite("car5.png"), carx, 400, this);


        strategy.show();
    }

    /**
     * Paint the Pause Menu
     */
    public void paintPauseMenu() {
        onPauseMenu();
        isPaused = true;

        Graphics g = strategy.getDrawGraphics();
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());

        paintHeader(g, "pausedTitle");
        g.drawImage(ResourceLoader.getInstance().getSprite("resumeButton.png"), 18, 300, this);
        g.drawImage(ResourceLoader.getInstance().getSprite("mainButton.png"), 343, 300, this);
        g.drawImage(ResourceLoader.getInstance().getSprite("quitButton.png"), 667, 300, this);

        strategy.show();
    }

    /**
     * Paint the Highscore Menu
     */
    public void paintHighscoreMenu() throws IOException {
        onHighscoreMenu();
        Graphics g = strategy.getDrawGraphics();
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());
        drawBox(g);

        paintHeader(g, "highscoreTitle");
        paintBackButton(g);
        g.drawImage(ResourceLoader.getInstance().getSprite("goldCrown.png"), 300, 210, this);
        g.drawImage(ResourceLoader.getInstance().getSprite("goldCrown.png"), 620, 210, this);
        g.drawImage(ResourceLoader.getInstance().getSprite("silverCrown.png"), 300, 270, this);
        g.drawImage(ResourceLoader.getInstance().getSprite("silverCrown.png"), 620, 270, this);
        g.drawImage(ResourceLoader.getInstance().getSprite("bronzeCrown.png"), 300, 330, this);
        g.drawImage(ResourceLoader.getInstance().getSprite("bronzeCrown.png"), 620, 330, this);



        String frist = "1st: " + Integer.toString(getScores().get(3));
        String second = "2nd: " + Integer.toString(getScores().get(2));
        String third = "3rd: " + Integer.toString(getScores().get(1));
        String fourth = "4th: " + Integer.toString(getScores().get(0));

        g.setColor(new Color(242, 124, 143));
        g.setFont(new Font("BitPotionExt", 0, 50));
        g.drawString(frist, 425, 240);

        g.setColor(new Color(41, 59, 61));
        g.setFont(new Font("BitPotionExt", 0, 50));
        g.drawString(second, 425, 300);
        g.drawString(third, 425, 360);
        g.drawString(fourth, 425, 420);
        strategy.show();
    }

    /**
     * Paints a health bar onto the game screen. Dynamically changes with player's health
     *
     * @param g      Graphics
     * @param health Player's current health
     */
    public void paintHealthBar(Graphics g, int health) {
        int fillAmount = (int) (health * 2.5); // Multiply player health by 2.5 to fill correct amount of bar

        g.setColor(new Color(81, 217, 61));
        g.fillRect(38, 523, 250, 27);
        g.setColor(Color.BLACK);
        g.fillRect(40, 525, 246, 23);
        g.setColor(new Color(81, 217, 61));
        g.fillRect(38, 523, fillAmount, 27);

        g.drawImage(ResourceLoader.getInstance().getSprite("healthPlus.png"), 10, 521, this);
    }

    /**
     * Paint currently active powerups onto the game screen
     *
     * @param g Graphics
     */
    public void paintPowerUps(Graphics g) {
        if (hitBlood) {
            g.drawImage(ResourceLoader.getInstance().getSprite("tigerBloodsm.png"), 35, 497, this);
        }

        if (hitTire) {
            g.drawImage(ResourceLoader.getInstance().getSprite("tiresm.png"), 65, 497, this);
        }
    }

    public void paintBackButton(Graphics g) {
        g.drawImage(ResourceLoader.getInstance().getSprite("backButton.png"), 715, 490, this);
    }

    public void paintHeader(Graphics g, String name) {
        g.drawImage(ResourceLoader.getInstance().getSprite(name + ".png"), 190, 20, this);
    }

    /**
     * Draw a box to write text in
     *
     * @param g Graphics
     */
    public void drawBox(Graphics g) {
        g.setColor(Color.WHITE);
        g.fillRect(227, 187, 526, 276);

        g.setColor(new Color(48, 48, 48));
        g.fillRect(230, 190, 520, 270);

        g.setColor(Color.WHITE);
        g.fillRect(240, 200, 500, 250);
    }

    /**
     * Write a fact to the Game Over screen
     */
    public void writeFact() {
        Graphics g = strategy.getDrawGraphics();

        g.setColor(new Color(41, 59, 61));
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


    /**
     * Get a random fact
     *
     * @return ArrayList, a String fact
     */
    public ArrayList<String> getFact() {
        ArrayList<String> factBuilder = new ArrayList<>();
        Random randy = new Random();
        int i = randy.nextInt(6);

        if (i == 0) {
            factBuilder.add("Even in areas with a very low moose density");
            factBuilder.add("moose are still attracted to roadways and");
            factBuilder.add("can pose a hazard to drivers.");
        } else if (i == 1) {
            factBuilder.add("Most moose accidents occur on clear nights");
            factBuilder.add("and on straight road sections. ");
            factBuilder.add("Don't let yourself be distracted.");
        } else if (i == 2) {
            factBuilder.add("More than 70% of accidents occur between");
            factBuilder.add("May and October. However, moose accidents");
            factBuilder.add("can occur any time of year.");
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

    public void keyPressed(KeyEvent e) {
        keyPressedHandlerLeft.handleInput(e);

        if (e.getKeyCode() == KeyEvent.VK_K) {
            Actor.debugCollision = !Actor.debugCollision;
        }

        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            if (gameState == eGameState.GS_Playing) {
                gameState = eGameState.GS_Paused;
            } else if (gameState == eGameState.GS_Paused) {
                isPaused = false;
                gameState = eGameState.GS_Playing;
            }
        }  // End A

        else if (e.getKeyChar() == 'B' || e.getKeyChar() == 'b') {
            if ( gameState == eGameState.GS_Customizations || gameState == eGameState.GS_HighScore) {
                gameState = eGameState.GS_MainMenu;
            }
        } // End B

        else if (e.getKeyChar() == 'C' || e.getKeyChar() == 'c') {
            if (gameState == eGameState.GS_MainMenu) {
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
            } else if (gameState == eGameState.GS_Paused) {
                gameState = eGameState.GS_MainMenu;
            }
        } // End M



        else if (e.getKeyChar() == 'P' || e.getKeyChar() == 'p') {
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

        else if (e.getKeyChar() == 'R' || e.getKeyChar() == 'r') {
            if (gameState == eGameState.GS_GameOver) {
                resetGame();
            } else if (gameState == eGameState.GS_Paused) {
                isPaused = false;
                gameState = eGameState.GS_Playing;
            }
        }
        else if (e.getKeyChar() == '1') {

            if (gameState == eGameState.GS_Customizations) {
                carType = 1;
            }
        } else if (e.getKeyChar() == '2') {

            if (gameState == eGameState.GS_Customizations) {
                carType = 2;
            }
        } else if (e.getKeyChar() == '3') {

            if (gameState == eGameState.GS_Customizations) {
                carType = 3;
            }
        } else if (e.getKeyChar() == '4') {

            if (gameState == eGameState.GS_Customizations) {
                carType = 4;
            }
        } else if (e.getKeyChar() == '5') {

            if (gameState == eGameState.GS_Customizations) {
                carType = 5;
            }
        }
    }


    public void keyReleased(KeyEvent e) {
        keyReleasedHandlerLeft.handleInput(e);
    }

    public void keyTyped(KeyEvent e) {
    }

    /**
     * Save the game score if it is bigger than the 4 highest scores
     *
     * @param newScore Final game score
     * @throws IOException
     */
    public void saveScore(int newScore) throws IOException {

        String file = "highscore.dat";
        ArrayList<Integer> scoreBored = getScores();

        for (int i = 0; i < 3; i++) {
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
            for (int i = 0; i < 4; i++) {
                output.append(String.valueOf(scoreBored.get(i)));
                output.newLine();
            }
            output.close();
        } catch (IOException ex1) {
            System.err.printf("ERROR writing score to file: %s\n", ex1);
        }
    }

    /**
     * Get the saved scores
     *
     * @return ArrayList, int scores
     * @throws IOException
     */
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
        mooseGame.game();
    }

}