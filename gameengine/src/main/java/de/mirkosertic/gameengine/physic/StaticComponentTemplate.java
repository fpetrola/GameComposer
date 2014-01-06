package de.mirkosertic.gameengine.physic;

import java.util.HashMap;
import java.util.Map;

import de.mirkosertic.gameengine.core.GameComponentTemplate;
import de.mirkosertic.gameengine.core.GameObject;
import de.mirkosertic.gameengine.core.GameObjectInstance;
import de.mirkosertic.gameengine.core.GameRuntime;

public class StaticComponentTemplate extends GameComponentTemplate<StaticComponent> implements Static {

    private final GameObject owner;

    public StaticComponentTemplate(GameObject aOwner) {
        owner = aOwner;
    }

    @Override
    public GameObject getOwner() {
        return owner;
    }

    public StaticComponent create(GameObjectInstance aInstance, GameRuntime aGameRuntime) {
        return new StaticComponent(aInstance, this);
    }

    @Override
    public String getTypeKey() {
        return StaticComponent.TYPE;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> theResult = new HashMap<String, Object>();
        theResult.put(StaticComponent.TYPE_ATTRIBUTE, StaticComponent.TYPE);
        return theResult;
    }

    public static StaticComponentTemplate deserialize(GameObject aOwner) {
        return new StaticComponentTemplate(aOwner);
    }
}