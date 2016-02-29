package com.videonasocialmedia.kamarada.domain;

import com.videonasocialmedia.kamarada.utils.Constants;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Veronica Lago Fominaya on 22/01/2016.
 */
public class GetVideosFromTempFolderUseCase {

    public GetVideosFromTempFolderUseCase() {
    }

    public List<String> getVideosFromTempFolder() {
        File directory = new File(Constants.PATH_APP_TEMP);
        List<String> paths = new ArrayList();
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null) { //some JVMs return null for empty dirs
                for (File f : files) {
                    if (!f.isDirectory() && !(f.getAbsolutePath()).equals(Constants.TEMP_VIDEO_PATH)) {
                        paths.add(f.getAbsolutePath());
                    }
                }
            }
        }
        return paths;
    }
}
