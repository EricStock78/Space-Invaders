package game;

import java.awt.Canvas;
import java.awt.Image;
import java.awt.image.ImageObserver;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import actors.Actor;

public class Stage extends Canvas implements ImageObserver {

    private static final long serialVersionUID = 1L;
    public static final int WIDTH = 1000;
    public static final int HEIGHT = 600;
    public static final int DESIRED_FPS = 60;

    protected boolean audioOptionsMenu = false;
    protected boolean controlsOptionsMenu = false;
    protected boolean customizationMenu = false;
    protected boolean game = false;
    protected boolean gameWon = false;
    protected boolean gameOver = false;
    protected boolean highscoreMenu = false;
    protected boolean mainMenu = false;
    protected boolean optionsMenu = false;
    protected boolean pauseMenu = false;
    protected boolean videoOptionsMenu = false;


    public List<Actor> actors = new ArrayList<Actor>();

    public Stage() {
    }

    public void endGame() {
        gameOver = true;
        mainMenu = true;
        pauseMenu = false;
        optionsMenu = false;
        game = false;
        customizationMenu = false;
        audioOptionsMenu = false;
        controlsOptionsMenu = false;
        videoOptionsMenu = false;
        highscoreMenu = false;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public void onMainMenu() {
        mainMenu = true;
        gameOver = false;
        pauseMenu = false;
        optionsMenu = false;
        game = false;
        customizationMenu = false;
        audioOptionsMenu = false;
        controlsOptionsMenu = false;
        videoOptionsMenu = false;
        highscoreMenu = false;
    }

    public void onOptionsMenu() {
        optionsMenu = true;
        gameOver = false;
        mainMenu = false;
        pauseMenu = false;
        game = false;
        customizationMenu = false;
        audioOptionsMenu = false;
        controlsOptionsMenu = false;
        videoOptionsMenu = false;
        highscoreMenu = false;
    }

    public void inGame() {
        game = true;
        optionsMenu = false;
        gameOver = false;
        mainMenu = false;
        pauseMenu = false;
        customizationMenu = false;
        audioOptionsMenu = false;
        controlsOptionsMenu = false;
        videoOptionsMenu = false;
        highscoreMenu = false;
    }

    public void onPauseMenu() {
        pauseMenu = true;
        optionsMenu = false;
        gameOver = false;
        mainMenu = false;
        game = false;
        customizationMenu = false;
        audioOptionsMenu = false;
        controlsOptionsMenu = false;
        videoOptionsMenu = false;
        highscoreMenu = false;
    }

    public void onCustomizationMenu() {
        customizationMenu = true;
        pauseMenu = false;
        optionsMenu = false;
        gameOver = false;
        mainMenu = false;
        game = false;
        audioOptionsMenu = false;
        controlsOptionsMenu = false;
        videoOptionsMenu = false;
        highscoreMenu = false;
    }

    public void onAudioOptionsMenu() {
        audioOptionsMenu = true;
        customizationMenu = false;
        pauseMenu = false;
        optionsMenu = false;
        gameOver = false;
        mainMenu = false;
        game = false;
        controlsOptionsMenu = false;
        videoOptionsMenu = false;
        highscoreMenu = false;
    }

    public void onControlsOptionsMenu() {
        controlsOptionsMenu = true;
        audioOptionsMenu = false;
        customizationMenu = false;
        pauseMenu = false;
        optionsMenu = false;
        gameOver = false;
        mainMenu = false;
        game = false;
        videoOptionsMenu = false;
        highscoreMenu = false;
    }

    public void onVideoOptionsMenu() {
        videoOptionsMenu = true;
        controlsOptionsMenu = false;
        audioOptionsMenu = false;
        customizationMenu = false;
        pauseMenu = false;
        optionsMenu = false;
        gameOver = false;
        mainMenu = false;
        game = false;
        highscoreMenu = false;
    }

    public void onHighscoreMenu() {
        highscoreMenu = true;
        videoOptionsMenu = false;
        controlsOptionsMenu = false;
        audioOptionsMenu = false;
        customizationMenu = false;
        pauseMenu = false;
        optionsMenu = false;
        gameOver = false;
        mainMenu = false;
        game = false;
        videoOptionsMenu = false;
    }

    public boolean imageUpdate(Image img, int infoflags, int x, int y,
                               int width, int height) {
        return false;
    }

    public void initWorld() {

    }

    public void game() throws IOException {

    }

    public void paintOptionsMenu() {

    }

    public void paintPauseMenu() {

    }

    public void paintMainMenu() {

    }

    public void paintCustomizationMenu() {

    }

    public void paintAudioOptionsMenu() {

    }

    public void paintVideoOptionsMenu() {

    }

    public void paintControlsOptionsMenu() {

    }

    public void paintHighscoreMenu() throws IOException {

    }

    public void resetGame() {

    }


}
