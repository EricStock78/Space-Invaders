package game;

import java.awt.event.KeyEvent;

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

    public void handleInput(KeyEvent event) {
        //ES.. don't use this for painting stuff or changing game states

        if (action == Action.PRESS) {
            player.triggerKeyPress(event);
        }
        else if (action == Action.RELSEASE)
            player.triggerKeyRelease(event);
    }
}