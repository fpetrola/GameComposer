package de.mirkosertic.gameengine.input;

import de.mirkosertic.gameengine.core.GameKeyCode;
import de.mirkosertic.gameengine.core.GestureDetector;
import de.mirkosertic.gameengine.event.GameEventManager;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DefaultGestureDetector implements GestureDetector {

    private static final Logger LOGGER = Logger.getLogger(DefaultGestureDetector.class.getSimpleName());

    private static final int THRESHOLD = 40;

    private final GameEventManager eventManager;
    private final Map<TouchIdentifier, TouchPosition> currentTouchPositions;

    private boolean rightKeyDown;
    private boolean leftKeyDown;
    private boolean upKeyDown;

    public DefaultGestureDetector(GameEventManager aEventManager) {
        eventManager = aEventManager;
        currentTouchPositions = new HashMap<TouchIdentifier, TouchPosition>();
    }

    public void keyPressed(GameKeyCode aKeyCode) {
        eventManager.fire(new KeyPressed(aKeyCode));
    }

    public void keyReleased(GameKeyCode aKeyCode) {
        eventManager.fire(new KeyPressed(aKeyCode));
    }

    @Override
    public void touchStarted(TouchPosition[] aTouchPositions) {

        LOGGER.info("touchStarted with " + aTouchPositions.length);

        currentTouchPositions.clear();
        clearLeftKeyDown();
        clearRightKeyDown();
        clearUpKeyDown();
        for (TouchPosition thePosition : aTouchPositions) {
            currentTouchPositions.put(thePosition.identifier, thePosition);
        }
    }

    @Override
    public void touchEnded(TouchPosition[] aTouchPositions) {

        LOGGER.info("touchEnded with " + aTouchPositions.length);

        for (TouchPosition thePosition : aTouchPositions) {
            currentTouchPositions.remove(thePosition.identifier);
        }

        clearUpKeyDown();
        clearLeftKeyDown();
        clearRightKeyDown();
    }

    @Override
    public void touchMoved(TouchPosition[] aTouchPositions) {

        LOGGER.info("touchMoved with " + aTouchPositions.length);

        if (aTouchPositions.length == 1) {
            TouchPosition theFirstPosition = aTouchPositions[0];
            TouchPosition theOriginTouchPosition = currentTouchPositions.get(theFirstPosition.identifier);

            int theDX = theFirstPosition.x - theOriginTouchPosition.x;
            int theDY = theFirstPosition.y - theOriginTouchPosition.y;

            LOGGER.log(Level.INFO, "dx = " + theDX, " dy = " + theDY);
            LOGGER.log(Level.INFO, "moveLeft = " + leftKeyDown + " moveRight = " + rightKeyDown);

            if (theDY < -THRESHOLD) {
                if (!upKeyDown) {
                    LOGGER.info("Emulating up key pressed");
                    eventManager.fire(new KeyPressed(GameKeyCode.UP));
                    upKeyDown = true;
                }
            }

            if (theDX > THRESHOLD) {
                if (!rightKeyDown) {
                    LOGGER.info("Emulating right key pressed");
                    clearLeftKeyDown();
                    eventManager.fire(new KeyPressed(GameKeyCode.RIGHT));
                    rightKeyDown = true;
                }
                return;
            }
            if (theDX > 0) {
                clearRightKeyDown();
                return;
            }
            if (theDX < -THRESHOLD) {
                if (!leftKeyDown) {
                    LOGGER.info("Emulating left key pressed");
                    clearRightKeyDown();
                    eventManager.fire(new KeyPressed(GameKeyCode.LEFT));
                    leftKeyDown = true;
                }
                return;
            }
            if (theDX < 0) {
                clearLeftKeyDown();
                return;
            }
        }
    }

    private void clearLeftKeyDown() {
        if (leftKeyDown) {
            leftKeyDown = false;
            eventManager.fire(new KeyReleased(GameKeyCode.LEFT));
        }
    }

    private void clearRightKeyDown() {
        if (rightKeyDown) {
            rightKeyDown = false;
            eventManager.fire(new KeyReleased(GameKeyCode.RIGHT));
        }
    }

    private void clearUpKeyDown() {
        if (upKeyDown) {
            upKeyDown = false;
            eventManager.fire(new KeyReleased(GameKeyCode.UP));
        }
    }


    @Override
    public void touchCanceled(TouchPosition[] aTouchPositions) {
        clearLeftKeyDown();
        clearRightKeyDown();
        clearUpKeyDown();
    }
}
