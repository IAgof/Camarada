package com.videonasocialmedia.Camarada.utils;

import android.os.Environment;

import java.io.File;

public class Constants {

    // Folders
    final public static String FOLDER_VIDEONA = "Kamarada";
    final public static String FOLDER_VIDEONA_TEMP = ".temporal";

    final public static String PATH_APP = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DCIM) + File.separator + FOLDER_VIDEONA;
    final public static String PATH_APP_TEMP = PATH_APP + File.separator + FOLDER_VIDEONA_TEMP;
    final public static String VIDEO_MUSIC_FOLDER = PATH_APP + File.separator +
            FOLDER_VIDEONA_TEMP + File.separator + "tempAV";
    final public static String VIDEO_MUSIC_FILE = VIDEO_MUSIC_FOLDER + File.separator + "audio.m4a";
    final public static String AUDIO_MUSIC_FILE_EXTENSION = ".m4a";

    final public static String FOLDER_VIDEONA_PRIVATE_MODEL = "data_camarada";
}
