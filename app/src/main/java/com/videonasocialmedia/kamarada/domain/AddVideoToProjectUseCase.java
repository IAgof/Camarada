/*
 * Copyright (c) 2015. Videona Socialmedia SL
 * http://www.videona.com
 * info@videona.com
 * All rights reserved
 *
 * Authors:
 * Veronica Lago Fominaya
 */

package com.videonasocialmedia.kamarada.domain;

import com.videonasocialmedia.kamarada.model.entities.editor.Project;

/**
 * This class is used to add a new videos to the project.
 */
public class AddVideoToProjectUseCase {

    /**
     * Constructor.
     */
    public AddVideoToProjectUseCase() {
    }

    /**
     * @param videoPath
     */
    public void addVideoToProfile(String videoPath) {

        Project.getInstance(null,null).addMediaVideoList(videoPath);


    }

}
