package com.videonasocialmedia.kamarada.domain;

import com.videonasocialmedia.kamarada.model.entities.editor.Project;

import java.util.List;

/**
 * Created by Veronica Lago Fominaya on 22/01/2016.
 */
public class GetVideosFromTempProjectUseCase {


    public List<String> getVideosFromTempProject() {

        return Project.getInstance(null,null).getMediaVideoList();
    }
}
