package de.mirkosertic.gameengine.physic;

import de.mirkosertic.gameengine.core.Behavior;
import de.mirkosertic.gameengine.core.GameObjectInstance;
import de.mirkosertic.gameengine.event.GameEventManager;
import de.mirkosertic.gameengine.event.Property;
import de.mirkosertic.gameengine.type.Reflectable;

import java.util.HashMap;
import java.util.Map;

public class PhysicsBehavior implements Behavior, Physics, Reflectable<PhysicsClassInformation> {

    private static final PhysicsClassInformation CIINSTANCE = new PhysicsClassInformation();

    static final String TYPE = "Physics";

    private final GameObjectInstance objectInstance;

    private final Property<Boolean> active;
    private final Property<Boolean> fixedRotation;
    private final Property<Float> density;
    private final Property<Float> friction;
    private final Property<Float> restitution;
    private final Property<Float> gravityScale;

    PhysicsBehavior(GameObjectInstance aObjectInstance) {
        this(aObjectInstance, aObjectInstance.getOwnerGameObject().getBehaviorTemplate(PhysicsBehaviorTemplate.class));
    }

    PhysicsBehavior(GameObjectInstance aObjectInstance, PhysicsBehaviorTemplate aTemplate) {
        objectInstance = aObjectInstance;

        GameEventManager theEventManager = aObjectInstance.getOwnerGameObject().getGameScene().getRuntime().getEventManager();

        active = new Property<>(Boolean.class, this, ACTIVE_PROPERTY, aTemplate.activeProperty().get(), theEventManager);
        fixedRotation = new Property<>(Boolean.class, this, FIXED_ROTATION_PROPERTY, aTemplate.fixedRotationProperty().get(), theEventManager);
        density = new Property<>(Float.class, this, DENSITY_PROPERTY, aTemplate.densityProperty().get(), theEventManager);
        friction = new Property<>(Float.class, this, FRICTION_PROPERTY, aTemplate.frictionProperty().get(), theEventManager);
        restitution = new Property<>(Float.class, this, RESTITUTION_PROPERTY, aTemplate.restitutionProperty().get(), theEventManager);
        gravityScale = new Property<>(Float.class, this, GRAVITY_SCALE_PROPERTY, aTemplate.gravityScaleProperty().get(), theEventManager);
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public PhysicsClassInformation getClassInformation() {
        return CIINSTANCE;
    }

    @Override
    public Property<Boolean> activeProperty() {
        return active;
    }

    @Override
    public Property<Boolean> fixedRotationProperty() {
        return fixedRotation;
    }

    @Override
    public Property<Float> densityProperty() {
        return density;
    }

    @Override
    public Property<Float> frictionProperty() {
        return friction;
    }

    @Override
    public Property<Float> restitutionProperty() {
        return restitution;
    }

    @Override
    public Property<Float> gravityScaleProperty() {
        return gravityScale;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> theStructure = new HashMap<>();
        theStructure.put(TYPE_ATTRIBUTE, TYPE);
        theStructure.put(ACTIVE_PROPERTY, Boolean.toString(active.get()));
        return theStructure;
    }

    @Override
    public PhysicsBehaviorTemplate getTemplate() {
        return objectInstance.getOwnerGameObject().getBehaviorTemplate(PhysicsBehaviorTemplate.class);
    }

    @Override
    public void delete() {
        objectInstance.getOwnerGameObject().getGameScene().removeBehaviorFrom(objectInstance.getOwnerGameObject(), this);
    }

    @Override
    public GameObjectInstance getInstance() {
        return objectInstance;
    }

    @Override
    public void markAsRemoteObject() {
    }

    public static PhysicsBehavior deserialize(GameObjectInstance aObjectInstance, Map<String, Object> aSerializedData) {
        PhysicsBehavior theResult = new PhysicsBehavior(aObjectInstance);
        String theActiveValue = (String) aSerializedData.get(ACTIVE_PROPERTY);
        if (theActiveValue != null) {
            theResult.active.setQuietly(Boolean.parseBoolean(theActiveValue));
        }
        return theResult;
    }
}
