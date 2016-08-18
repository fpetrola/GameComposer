package de.mirkosertic.gameengine.dragome;

import java.net.URL;
import java.util.Arrays;

import org.w3c.dom.events.KeyboardEvent;
import org.w3c.dom.events.MouseEvent;

import com.dragome.commons.DragomeConfiguratorImplementor;
import com.dragome.commons.compiler.CompilerMode;
import com.dragome.web.config.DomHandlerApplicationConfigurator;

@DragomeConfiguratorImplementor(priority= 100)
public class GameEngineDragomeConfigurator extends DomHandlerApplicationConfigurator {

    public GameEngineDragomeConfigurator() {
    	   super(Arrays.asList(MouseEvent.class, KeyboardEvent.class));
//        System.setProperty("dragome-compile-mode", CompilerMode.Production.toString());
    }

    @Override
    public boolean filterClassPath(String aClassPathEntry) { 
		boolean include= super.filterClassPath(aClassPathEntry);

		include|= aClassPathEntry.contains("gameengine-0.5");
		include|= aClassPathEntry.contains("gameengine-lua");
		include|= aClassPathEntry.contains("gameengine-jbox2d");
		include|= aClassPathEntry.contains("dragome-js-jre");
		include|= aClassPathEntry.contains("dragome-web");
		include|= aClassPathEntry.contains("dragome-w3c");
		include|= aClassPathEntry.contains("dragome-js-commons");
		include|= aClassPathEntry.contains("dragome-core");

		return include;
    }

    @Override
    public boolean isCheckingCast() {
        return false;
    }

    public boolean isRemoveUnusedCode()
    {
        return false;
    }
    
    public URL getAdditionalCodeKeepConfigFile() {
    	return getClass().getResource("/gameengine-proguard.conf");
    }
}