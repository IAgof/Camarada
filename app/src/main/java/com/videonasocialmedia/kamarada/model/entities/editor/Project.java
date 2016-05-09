/*
 * Copyright (C) 2015 Videona Socialmedia SL
 * http://www.videona.com
 * info@videona.com
 * All rights reserved
 *
 * Authors:
 * Juan Javier Cabanas
 * Álvaro Martínez Marco
 * Danny R. Fonseca Arboleda
 */
package com.videonasocialmedia.kamarada.model.entities.editor;

import java.io.File;
import java.util.ArrayList;

/**
 * Project representation that contains reference to media, audio, transitions and effects used in
 * the current edition job.
 * A project can be created, opened, saved, deleted and shared with other users. Every time a user
 * opens a project all previous changes must be accessible to him or her. However there can be only
 * one
 */
public class Project {

    /**
     * There could be just one project open at a time. So this converts Project in a Singleton.
     */
    private static Project INSTANCE;

    private ArrayList<String> videoList;

    /**
     * Project name. Also it will be the name of the exported video
     */
    private String title;
    /**
     * The folder where de temp files of the project are stored
     */
    private String projectPath;


    public static String KAMARADA_PATH = "";

    /**
     * Constructor of minimum number of parameters. This is the Default constructor.
     *
     * @param title    - Project and final video name.
     * @param rootPath - Path to root folder for the current project.
     */
    private Project(String title, String rootPath) {
        this.title = title;
        this.projectPath = rootPath + "/projects/" + title; //todo probablemente necesitemos un slugify de ese title.
        this.checkPathSetup(rootPath);
        this.videoList = new ArrayList<>();


    }

    /**
     * @param rootPath
     */
    private void checkPathSetup(String rootPath) {

        Project.KAMARADA_PATH = rootPath;
        File projectPath = new File(this.projectPath);
        projectPath.mkdirs();


    }

    /**
     * Project factory.
     *
     * @return - Singleton instance of the current project.
     */
    public static Project getInstance(String title, String rootPath) {
        if (INSTANCE == null) {
            INSTANCE = new Project(title, rootPath);
        }
        return INSTANCE;
    }

    // getters & setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getProjectPath() {
        return projectPath;
    }

    public void setProjectPath(String projectPath) {
        this.projectPath = projectPath;
    }

    public ArrayList<String> getMediaVideoList() {
        return videoList;
    }

    public void addMediaVideoList(String videoPath){
        videoList.add(videoPath);
    }

    public void removeMediaVideoList(String videoPath){
        videoList.remove(videoPath);
    }

    public void clearMediaVideoList(){

        videoList.clear();
    }


}