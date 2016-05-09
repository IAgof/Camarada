package com.videonasocialmedia.kamarada.utils;

import android.os.Environment;

import java.io.File;

public class Constants {

    // Folders
    final public static String FOLDER_KAMARADA = "Kamarada";
    final public static String FOLDER_KAMARADA_TEMP = ".temporal";

    final public static String PATH_APP = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DCIM) + File.separator + FOLDER_KAMARADA;
    final public static String PATH_APP_TEMP = PATH_APP + File.separator + FOLDER_KAMARADA_TEMP;
    final public static String VIDEO_MUSIC_FOLDER = PATH_APP + File.separator +
            FOLDER_KAMARADA_TEMP + File.separator + "tempAV";
    final public static String VIDEO_MUSIC_FILE = VIDEO_MUSIC_FOLDER + File.separator + "audio.m4a";
    final public static String AUDIO_MUSIC_FILE_EXTENSION = ".m4a";

    // TODO defined in recorder module, but necessary because recorder creates this file when init it
    final public static String TEMP_VIDEO_PATH = PATH_APP_TEMP + File.separator + "VID_temp.mp4";

    final public static String FOLDER_VIDEONA_PRIVATE_MODEL = "data_camarada";

    public static String PROJECT_TITLE = "project";
}
