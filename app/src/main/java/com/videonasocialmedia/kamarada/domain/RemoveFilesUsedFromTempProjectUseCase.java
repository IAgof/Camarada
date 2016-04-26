package com.videonasocialmedia.kamarada.domain;

import com.videonasocialmedia.kamarada.model.entities.editor.Project;
import com.videonasocialmedia.kamarada.utils.Utils;

import java.util.ArrayList;

/**
 * Created by Veronica Lago Fominaya on 22/01/2016.
 */
public class RemoveFilesUsedFromTempProjectUseCase {

    public RemoveFilesUsedFromTempProjectUseCase() {
    }

    public void removeFilesUsedInExport() {

        ArrayList<String> videoListPath = Project.getInstance(null,null).getMediaVideoList();

        for (String videoPath : videoListPath) {
            Utils.removeVideo(videoPath);
        }

    }

    public void clearVideosFromProject(){

        Project.getInstance(null,null).clearMediaVideoList();
    }
}
