package de.mirkosertic.gamecomposer.contentarea.gamescene;

import de.mirkosertic.gamecomposer.FlushResourceCacheEvent;
import de.mirkosertic.gamecomposer.GameObjectClipboardContent;
import de.mirkosertic.gamecomposer.ObjectSelectedEvent;
import de.mirkosertic.gamecomposer.PersistenceManager;
import de.mirkosertic.gamecomposer.ShutdownEvent;
import de.mirkosertic.gamecomposer.contentarea.ContentController;
import de.mirkosertic.gameengine.Callback;
import de.mirkosertic.gameengine.camera.CameraBehavior;
import de.mirkosertic.gameengine.camera.SetScreenResolution;
import de.mirkosertic.gameengine.core.GameLoop;
import de.mirkosertic.gameengine.core.GameLoopFactory;
import de.mirkosertic.gameengine.core.GameObject;
import de.mirkosertic.gameengine.core.GameObjectInstance;
import de.mirkosertic.gameengine.core.GameObjectInstanceAddedToScene;
import de.mirkosertic.gameengine.core.GameRuntime;
import de.mirkosertic.gameengine.core.GameScene;
import de.mirkosertic.gameengine.core.SceneShutdown;
import de.mirkosertic.gameengine.event.GameEventListener;
import de.mirkosertic.gameengine.event.GameEventManager;
import de.mirkosertic.gameengine.event.PropertyChanged;
import de.mirkosertic.gameengine.event.SystemException;
import de.mirkosertic.gameengine.input.DefaultGestureDetector;
import de.mirkosertic.gameengine.javafx.JavaFXGameView;
import de.mirkosertic.gameengine.network.DefaultEventInterpreter;
import de.mirkosertic.gameengine.network.NetworkGameView;
import de.mirkosertic.gameengine.network.NetworkGameViewFactory;
import de.mirkosertic.gameengine.network.NewGameInstance;
import de.mirkosertic.gameengine.physic.DisableDynamicPhysics;
import de.mirkosertic.gameengine.physic.EnableDynamicPhysics;
import de.mirkosertic.gameengine.type.GameKeyCode;
import de.mirkosertic.gameengine.type.Position;
import de.mirkosertic.gameengine.type.Size;
import de.mirkosertic.gameengine.type.UUID;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.util.List;
import javax.enterprise.event.Event;
import javax.inject.Inject;

public class GameSceneEditorController implements ContentController<GameScene> {

    @FXML
    BorderPane centerBorderPane;

    @FXML
    CheckBox snapToGrid;

    @FXML
    CheckBox renderPhysics;

    @FXML
    TextField gridsizeWidth;

    @FXML
    TextField gridsizeHeight;

    @Inject
    PersistenceManager persistenceManager;

    private GameScene gameScene;
    private Node view;
    private EditorFXGameView gameView;
    private GameLoop gameLoop;
    private CameraBehavior cameraComponent;

    private Event<ObjectSelectedEvent> objectSelectedEventEvent;

    private GameObjectInstance dndCreateInstance;
    private GameObjectInstance draggingInstance;
    private Position draggingMouseWorldPosition;
    private GameRuntime gameRuntime;

    GameSceneEditorController initialize(GameRuntime aGameRuntime, GameScene aScene, Node aView, EditorFXGameView aGameView, GameLoop aGameLoop, CameraBehavior aCameraComponent, Event<ObjectSelectedEvent> aSelectedEvent) {

        gameRuntime = aGameRuntime;

        Canvas theCanvasNode = aGameView.getCanvasNode();

        centerBorderPane.setCenter(theCanvasNode);

        gameScene = aScene;
        view = aView;
        gameView = aGameView;
        gameLoop = aGameLoop;
        cameraComponent = aCameraComponent;
        objectSelectedEventEvent = aSelectedEvent;

        theCanvasNode.setOnDragOver(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent aEvent) {
                onDragOver(aEvent);
            }
        });
        theCanvasNode.setOnDragEntered(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent aEvent) {
                onDragEntered(aEvent);
            }
        });
        theCanvasNode.setOnDragExited(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent aEvent) {
                onDragExited(aEvent);
            }
        });
        theCanvasNode.setOnDragDropped(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent aEvent) {
                setOnDragDropped(aEvent);
            }
        });
        theCanvasNode.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent aEvent) {
                onMouseDragged(aEvent);
            }
        });
        theCanvasNode.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent aEvent) {
                onMousePressed(aEvent);
            }
        });
        theCanvasNode.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent aEvent) {
                onMouseReleased(aEvent);
            }
        });
        theCanvasNode.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent aEvent) {
                onMouseClicked(aEvent);
            }
        });

        gridsizeWidth.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String aOldValue, String aNewValue) {
                gameView.gridsizeWidthProperty().set(Integer.valueOf(aNewValue));
            }
        });
        gridsizeHeight.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String aOldValue, String aNewValue) {
                gameView.gridsizeHeightProperty().set(Integer.valueOf(aNewValue));
            }
        });
        gridsizeWidth.setText(Integer.toString(gameView.gridsizeWidthProperty().get()));
        gridsizeHeight.setText(Integer.toString(gameView.gridsizeHeightProperty().get()));

        gameView.renderPhysicsDebugProperty().bind(renderPhysics.selectedProperty());
        gameView.snapToGridProperty().bind(snapToGrid.selectedProperty());
        return this;
    }

    private void onDragOver(DragEvent aEvent) {
        if (aEvent.getGestureSource() != this && aEvent.getDragboard().hasContent(GameObjectClipboardContent.FORMAT)) {
            aEvent.acceptTransferModes(TransferMode.LINK);
        }

        if (dndCreateInstance != null) {
            Position theNewPosition = cameraComponent.transformFromScreen(new Position(aEvent.getX(), aEvent.getY()));
            dndCreateInstance.positionProperty().set(theNewPosition);
        }

        aEvent.consume();
    }

    private void onDragEntered(DragEvent aEvent) {
        Dragboard theDragBoard = aEvent.getDragboard();
        if (theDragBoard.hasContent(GameObjectClipboardContent.FORMAT)) {
            GameObjectClipboardContent theContent = (GameObjectClipboardContent) theDragBoard.getContent(GameObjectClipboardContent.FORMAT);
            GameObject theGameObject = gameScene.findObjectByID(theContent.getGameObjectId());

            GameObjectInstance theInstance = gameScene.createFrom(theGameObject);
            theInstance.positionProperty().set(cameraComponent.transformFromScreen(new Position(aEvent.getX(), aEvent.getY())));

            gameScene.addInstance(theInstance);

            gameScene.getRuntime().getEventManager().fire(new DisableDynamicPhysics(theInstance));

            dndCreateInstance = theInstance;

            aEvent.consume();
        }
    }

    private void onDragExited(DragEvent aEvent) {
        if (dndCreateInstance != null) {
            gameScene.removeGameObjectInstance(dndCreateInstance);
            dndCreateInstance = null;
            aEvent.consume();
        }
    }

    private void setOnDragDropped(DragEvent aEvent) {
        if (dndCreateInstance != null) {
            aEvent.setDropCompleted(true);
            aEvent.consume();
            if (snapToGrid.isSelected()) {
                dndCreateInstance.positionProperty().set(gameView.snapToGrid(dndCreateInstance.positionProperty().get()));
            }

            gameScene.getRuntime().getEventManager().fire(new EnableDynamicPhysics(dndCreateInstance));

            dndCreateInstance = null;
        }
    }

    private void onMousePressed(MouseEvent aEvent) {
        Position theScreenPosition = new Position(aEvent.getX(), aEvent.getY());
        Position theWorldPosition = cameraComponent.transformFromScreen(theScreenPosition);
        List<GameObjectInstance> theFoundInstances = gameScene.findAllAt(theScreenPosition, theWorldPosition);
        if (theFoundInstances.size() == 1) {
            draggingInstance = theFoundInstances.get(0);
            draggingMouseWorldPosition = theWorldPosition;

            gameScene.getRuntime().getEventManager().fire(new DisableDynamicPhysics(draggingInstance));

            objectSelectedEventEvent.fire(new ObjectSelectedEvent(draggingInstance));
        } else {
            draggingInstance = null;
            draggingMouseWorldPosition = theScreenPosition;
        }
    }

    private void onMouseDragged(MouseEvent aEvent) {
        if (draggingMouseWorldPosition != null) {
            if (draggingInstance != null) {
                // Move object instance

                Position theScreenPosition = new Position(aEvent.getX(), aEvent.getY());
                Position theWorldPosition = cameraComponent.transformFromScreen(theScreenPosition);
                float theDX = theWorldPosition.x - draggingMouseWorldPosition.x;
                float theDY = theWorldPosition.y - draggingMouseWorldPosition.y;

                Position theObjectPosition = draggingInstance.positionProperty().get();
                draggingInstance.positionProperty().set(new Position(theObjectPosition.x + theDX, theObjectPosition.y + theDY));

                draggingMouseWorldPosition = theWorldPosition;
            } else {

                Position theScreenPosition = new Position(aEvent.getX(), aEvent.getY());

                float theDX = theScreenPosition.x - draggingMouseWorldPosition.x;
                float theDY = theScreenPosition.y - draggingMouseWorldPosition.y;

                // Move camera
                Position theObjectPosition = cameraComponent.getObjectInstance().positionProperty().get();
                cameraComponent.getObjectInstance().positionProperty().set(new Position(theObjectPosition.x - theDX, theObjectPosition.y - theDY));

                draggingMouseWorldPosition = theScreenPosition;
            }
        }
    }

    private void onMouseReleased(MouseEvent aEvent) {
        if (draggingInstance != null) {
            if (snapToGrid.isSelected()) {
                draggingInstance.positionProperty().set(gameView.snapToGrid(draggingInstance.positionProperty().get()));
            }
            gameScene.getRuntime().getEventManager().fire(new EnableDynamicPhysics(draggingInstance));
        }
        draggingMouseWorldPosition = null;
        draggingInstance = null;
    }

    @Override
    public GameScene getEditingObject() {
        return gameScene;
    }

    @Override
    public void processKeyPressedEvent(KeyEvent aKeyEvent) {
        gameView.getGestureDetector().keyPressed(GameKeyCode.valueOf(aKeyEvent.getCode().name()));
    }

    @Override
    public void processKeyReleasedEvent(KeyEvent aKeyEvent) {
        gameView.getGestureDetector().keyReleased(GameKeyCode.valueOf(aKeyEvent.getCode().name()));
    }

    @Override
    public Node getView() {
        return view;
    }

    @Override
    public void addedAsTab() {
        gameView.startTimer(gameLoop, true);
    }

    @Override
    public void removed() {
        gameScene.getRuntime().getEventManager().fire(new SceneShutdown());
        gameView.stopTimer();
    }

    @Override
    public void onObjectSelected(ObjectSelectedEvent aEvent) {
        gameView.onObjectSelected(aEvent);
    }

    @Override
    public void onShutdown(ShutdownEvent aEvent) {
        gameView.stopTimer();
        gameScene.getRuntime().getEventManager().fire(new SceneShutdown());
    }

    void onMouseClicked(MouseEvent aEvent) {
        Position theClickPosition = new Position(aEvent.getX(), aEvent.getY());
        cameraComponent.processVisibleInstances(new Callback<GameObjectInstance>() {
            @Override
            public void process(GameObjectInstance aValue) {
                if (aValue.contains(theClickPosition)) {
                    objectSelectedEventEvent.fire(new ObjectSelectedEvent(aValue));
                }
            }
        });
    }

    @Override
    public void onObjectUpdated(Tab aTab, PropertyChanged aEvent) {
        if (aEvent.getOwner() == gameScene) {
            aTab.setText(gameScene.nameProperty().get());
        }
    }

    @Override
    public void onFlushResourceCache(FlushResourceCacheEvent aEvent) {
        gameRuntime.getResourceCache().flush();
    }

    @FXML
    public void onPreview() {

        final ListView<String> theLoggerView = new ListView<>();
        theLoggerView.getItems().clear();
        theLoggerView.setMinHeight(50);
        theLoggerView.setPrefHeight(50);
        theLoggerView.setFocusTraversable(false);

        GameScene thePreviewScene = persistenceManager.cloneSceneForPreview(gameScene);

        GameRuntime theRuntime = thePreviewScene.getRuntime();
        final GameEventManager theEventManager = theRuntime.getEventManager();

        GameObject theDefaultCamera = thePreviewScene.cameraObjectProperty().get();
        if (theDefaultCamera == null) {
            throw new IllegalArgumentException("No camera set");
        }

        // Detect and create a camera
        GameObjectInstance theCameraInstance = thePreviewScene.createFrom(theDefaultCamera);
        final CameraBehavior theCameraInstanceBehavior = theCameraInstance.getBehavior(CameraBehavior.class);
        if (theCameraInstanceBehavior == null) {
            throw new IllegalArgumentException("No camera component in camera object");
        }

        GameObjectInstance thePlayerInstance = null;
        for (GameObjectInstance theInstance : thePreviewScene.getInstances()) {
            if (theInstance.getOwnerGameObject() == thePreviewScene.defaultPlayerProperty().get()) {
                thePlayerInstance = theInstance;
            }
        }

        // If there is a networked game
        // we need unique player instance ids
        // After loading they are the same on every instance
        if (thePlayerInstance != null) {
            thePlayerInstance.uuidProperty().set(UUID.randomUID());
        }

        final JavaFXGameView thePreviewGameView = new JavaFXGameView(theRuntime, theCameraInstanceBehavior, new DefaultGestureDetector(theRuntime.getEventManager(), theCameraInstanceBehavior));

        GameLoopFactory theGameLoopFactory = new GameLoopFactory();
        GameLoop theMainLoop = theGameLoopFactory.create(thePreviewScene, thePreviewGameView, theRuntime);

        // Register a local Network Game View
        LocalNetworkConnector theNetworkConnector = new LocalNetworkConnector();
        NetworkGameViewFactory theFactory = new NetworkGameViewFactory(theNetworkConnector, new DefaultEventInterpreter());
        NetworkGameView theNetworkGameView = theFactory.createNetworkViewFor(theEventManager);
        theMainLoop.addGameView(theNetworkGameView);

        // Finally notify the other game instances that there is a new player on the field
        // This event will we sent to the other game instances
        // And will trigger there a creation of the new remote player
        theNetworkGameView.handleGameEvent(new NewGameInstance(thePlayerInstance));
        if (thePlayerInstance != null) {

            final GameObjectInstance theFinalPlayer = thePlayerInstance;

            theEventManager.register(null, NewGameInstance.class, new GameEventListener<NewGameInstance>() {
                @Override
                public void handleGameEvent(NewGameInstance aEvent) {
                    // Inform the other instances about the current player
                    theNetworkGameView.handleGameEvent(new GameObjectInstanceAddedToScene(theFinalPlayer));
                }
            });
        }

        // Set defaults, this will be overridden
        Size theInitialSize = new Size(200,200);

        theEventManager.register(null, SetScreenResolution.class, new GameEventListener<SetScreenResolution>() {
            @Override
            public void handleGameEvent(SetScreenResolution aEvent) {
                thePreviewGameView.setCurrentScreenSize(aEvent.screenSize);
            }
        });

        theEventManager.fire(new SetScreenResolution(theInitialSize));

        theEventManager.register(null, SystemException.class, new GameEventListener<SystemException>() {
            @Override
            public void handleGameEvent(SystemException aEvent) {
                List<String> theExceptions = theLoggerView.getItems();
                theExceptions.add(0, aEvent.toString());
                while (theExceptions.size() > 20) {
                    theExceptions.remove(19);
                }
            }
        });

        Canvas thePreviewNode = thePreviewGameView.getCanvasNode();

        BorderPane thePaneForCanvas = new BorderPane();
        thePaneForCanvas.setCenter(thePreviewNode);

        BorderPane theBorderPane = new BorderPane();
        theBorderPane.setCenter(thePaneForCanvas);
        theBorderPane.setBottom(theLoggerView);
        theBorderPane.setMinWidth(BorderPane.USE_PREF_SIZE);
        theBorderPane.setMinHeight(BorderPane.USE_PREF_SIZE);
        theBorderPane.setPrefWidth(800);
        theBorderPane.setPrefHeight(600);

        thePaneForCanvas.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
                theEventManager.fire(new SetScreenResolution(new Size((int) ((double) number2), theCameraInstanceBehavior.getScreenSize().height)));
            }
        });
        thePaneForCanvas.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
                theEventManager.fire(new SetScreenResolution(new Size(theCameraInstanceBehavior.getScreenSize().width, (int) (((double) number2)))));
            }
        });
        thePreviewGameView.getCanvasNode().widthProperty().bind(thePaneForCanvas.widthProperty());
        thePreviewGameView.getCanvasNode().heightProperty().bind(thePaneForCanvas.heightProperty());

        Stage theStage = new Stage();
        theStage.setTitle("Game Preview");

        Scene theScene = new Scene(theBorderPane);
        //theStage.initStyle(StageStyle.UTILITY);
        theStage.setScene(theScene);
        theStage.initOwner(view.getScene().getWindow());

        theStage.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent aKeyEvent) {
                thePreviewGameView.getGestureDetector().keyPressed(GameKeyCode.valueOf(aKeyEvent.getCode().name()));
            }
        });
        theStage.addEventHandler(KeyEvent.KEY_RELEASED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent aKeyEvent) {
                thePreviewGameView.getGestureDetector().keyReleased(GameKeyCode.valueOf(aKeyEvent.getCode().name()));
            }
        });
        theStage.addEventFilter(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent aEvent) {
                thePreviewGameView.getGestureDetector().mousePressed(new Position(aEvent.getX(), aEvent.getY()));
            }
        });
        theStage.addEventFilter(MouseEvent.MOUSE_RELEASED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent aEvent) {
                thePreviewGameView.getGestureDetector().mouseReleased(new Position(aEvent.getX(), aEvent.getY()));
            }
        });

        theStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent windowEvent) {
                thePreviewGameView.stopTimer();
                theNetworkConnector.shutdown();
            }
        });
        theStage.show();
        theStage.requestFocus();

        theCameraInstanceBehavior.initializeFor(thePreviewScene, thePlayerInstance);

        thePreviewGameView.startTimer(theMainLoop, false);
    }
}