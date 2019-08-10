package game;

import java.awt.event.KeyEvent;
import java.io.IOException;

import actors.Player;

import actors.KeyboardControllable;
/**
 * creates a thread to process player input
 * @author ghast
 *
 */
public class InputHandler {
    public enum Action {
        PRESS,
        RELSEASE
    }
    private Stage stage = null;
    private KeyboardControllable player  = null;
    public Action action;

    public InputHandler(Stage stg, KeyboardControllable player) {
        this.stage = stg;
        this.player = player;
    }

    public void handleInput(KeyEvent event) throws IOException {
        if (action == Action.PRESS) {
            if (KeyEvent.VK_ENTER == event.getKeyCode()) {
                if (stage.gameOver) {
                    stage.game();
                    //stage.resetGame();
                   // stage.inGame();
                }

                else if (stage.game) {
                    stage.paintPauseMenu();
                }
            }

            else if (event.getKeyChar() == 'A' || event.getKeyChar() == 'a') {
                if (stage.optionsMenu) {
                    stage.paintAudioOptionsMenu();
                }
            }

            else if (event.getKeyChar() == 'B' || event.getKeyChar() == 'b') {
                if (stage.optionsMenu || stage.customizationMenu || stage.highscoreMenu) {
                    stage.paintMainMenu();
                }

                else if (stage.controlsOptionsMenu || stage.audioOptionsMenu || stage.videoOptionsMenu) {
                    stage.paintOptionsMenu();
                }
            } // End B

            else if (event.getKeyChar() == 'C' || event.getKeyChar() == 'c') {
                if (stage.mainMenu) {
                    stage.paintCustomizationMenu();
                }

                else if (stage.optionsMenu) {
                    stage.paintControlsOptionsMenu();
                }
            } // End C

            else if (event.getKeyChar() == 'H' || event.getKeyChar() == 'h') {
                if (stage.mainMenu) {
                    stage.paintHighscoreMenu();
                }
            } // End H

        
            else if (event.getKeyChar() == 'M' || event.getKeyChar() == 'm') {
                if (stage.gameOver) {
                    stage.paintMainMenu();
                }
            } // End M

            else if (event.getKeyChar() == 'O' || event.getKeyChar() == 'o') {
                if (stage.mainMenu) {
                    stage.paintOptionsMenu();
                }
            } // End O

            else if (event.getKeyChar() == 'P' || event.getKeyChar() == 'p') {
                // TODO: Game does not init properly when started this way ex. no controls, cant close window
                if (stage.mainMenu) {
                    stage.game();
                }
            } // End P

            else if (event.getKeyChar() == 'Q' || event.getKeyChar() == 'q') {
                if (stage.mainMenu || stage.pauseMenu || stage.gameOver) {
                    System.exit(0);
                }
            } // End Q

            else if (event.getKeyChar() == 'V' || event.getKeyChar() == 'v') {
                if (stage.optionsMenu) {
                    stage.paintVideoOptionsMenu();
                }
            }

            else
                player.triggerKeyPress(event);
        }
        else if (action == Action.RELSEASE)
            player.triggerKeyRelease(event);
    }
}