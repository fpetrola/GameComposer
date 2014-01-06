package de.mirkosertic.gameengine.type;

import java.util.HashMap;
import java.util.Map;

public class Size {

    public final int width;
    public final int height;

    public Size() {
        this(0, 0);
    }

    public Size(int aWidth, int aHeight) {
        width = aWidth;
        height = aHeight;
    }

    public Size changeWidth(int aNewWidth) {
        return new Size(aNewWidth, height);
    }

    public Size changeHeight(int aNewHeight) {
        return new Size(width, aNewHeight);
    }

    public Map<String, Object> serialize() {
        Map<String, Object> theResult = new HashMap<String, Object>();
        theResult.put("width", "" + width);
        theResult.put("height", "" + height);
        return theResult;
    }

    public static Size deserialize(Map<String, Object> aSerializedStructure) {
        int theWidth = Integer.valueOf((String) aSerializedStructure.get("width"));
        int theHeight = Integer.valueOf((String) aSerializedStructure.get("height"));
        return new Size(theWidth, theHeight);
    }
}