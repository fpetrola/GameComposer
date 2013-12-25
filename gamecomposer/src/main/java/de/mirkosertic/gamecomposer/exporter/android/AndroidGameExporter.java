package de.mirkosertic.gamecomposer.exporter.android;

import de.mirkosertic.gamecomposer.ExportGameAndroidEvent;
import de.mirkosertic.gamecomposer.PersistenceManager;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.SystemUtils;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.*;
import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@Singleton
public class AndroidGameExporter {

    @Inject
    PersistenceManager persistenceManager;

    public void onExport(@Observes ExportGameAndroidEvent aEvent) throws IOException {

        // Create a new temp directory
        File theTempDirectory = new File(SystemUtils.getJavaIoTmpDir(), "game" + System.currentTimeMillis());

        File theAssetsDirectory = new File(theTempDirectory, "assets");
        theAssetsDirectory.mkdirs();

        // And copy the game to the assets directory
        persistenceManager.copyGameTo(theAssetsDirectory);

        // We need to extract the apk file
        InputStream theStream = AndroidGameExporter.class.getResourceAsStream("/gameengine-androidrenderer.apk");
        try (ZipInputStream theZipStream = new ZipInputStream(theStream)) {
            ZipEntry theEntry;
            while ((theEntry = theZipStream.getNextEntry()) != null) {

                File theTargetFile = new File(theTempDirectory, theEntry.getName().replace('/', File.separatorChar));
                theTargetFile.getParentFile().mkdirs();
                try (FileOutputStream theFos = new FileOutputStream(theTargetFile)) {
                    IOUtils.copy(theZipStream, theFos);
                }
            }
        }

        String theBaseName = theTempDirectory.toString();
        File theGameFile = new File(aEvent.getGameDirectory(), "game.apk");
        try (ZipOutputStream theZipStream = new ZipOutputStream(new FileOutputStream(theGameFile))) {
            for (File theFile : FileUtils.listFilesAndDirs(theTempDirectory, TrueFileFilter.INSTANCE, DirectoryFileFilter.DIRECTORY)) {
                String theRelativeName = theFile.toString().substring(theBaseName.length()).replace(File.separatorChar, '/');
                if (theRelativeName.length() > 0) {
                    theRelativeName = theRelativeName.substring(1);
                    if (theFile.isDirectory()) {
                        ZipEntry theEntry = new ZipEntry(theRelativeName + "/");
                        theEntry.setTime(theFile.lastModified());
                        theZipStream.putNextEntry(theEntry);
                        theZipStream.closeEntry();
                    } else {
                        ZipEntry theEntry = new ZipEntry(theRelativeName);
                        theEntry.setTime(theFile.lastModified());
                        theZipStream.putNextEntry(theEntry);
                        try (FileInputStream theFis = new FileInputStream(theFile)) {
                            IOUtils.copy(theFis, theZipStream);
                        }
                        theZipStream.closeEntry();
                    }
                }
            }
        }

        FileUtils.deleteDirectory(theTempDirectory);

        Desktop.getDesktop().browse(aEvent.getGameDirectory().toURI());
    }
}