package de.mirkosertic.gamecomposer.objectinspector;

import de.mirkosertic.gameengine.event.Property;

import java.util.Optional;
import org.controlsfx.control.PropertySheet;
import org.controlsfx.property.editor.PropertyEditor;

public class PropertyEditorItem<T> implements PropertySheet.Item {

    private final Property<T> property;
    private final String category;
    private final String name;
    private final String description;
    private final Optional<Class<? extends PropertyEditor<?>>> propertyEditor;

    public PropertyEditorItem(String aCategory, Property<T> aProperty, String aName, String aDescription, Optional<Class<? extends PropertyEditor<?>>> aPropertyEditor) {
        property = aProperty;
        category = aCategory;
        name = aName;
        description = aDescription;
        propertyEditor = aPropertyEditor;
    }

    public PropertyEditorItem(String aCategory, Property<T> aProperty, String aName, String aDescription) {
        this(aCategory, aProperty, aName, aDescription, Optional.empty());
    }

    @Override
    public Class<?> getType() {
        return property.getType();
    }

    @Override
    public String getCategory() {
        return category;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public Object getValue() {
        return property.get();
    }

    @Override
    public void setValue(Object aValue) {
        property.set((T) aValue);
    }

    @Override
    public Optional<Class<? extends PropertyEditor<?>>> getPropertyEditorClass() {
        return propertyEditor;
    }

    public Object getOwner() {
        return property.getOwner();
    }
}