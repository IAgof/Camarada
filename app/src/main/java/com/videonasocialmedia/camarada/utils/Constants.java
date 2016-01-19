package com.videonasocialmedia.camarada.utils;

import android.os.Environment;

import java.io.File;

public class Constants {

    //TODO cambiar el endpoint a la dirección de producción
    public static final String API_ENDPOINT = "http://192.168.0.22/Videona/web/app_dev.php/api";
    //OAuth
    public static final String OAUTH_CLIENT_ID = "4_6c1bbez44j0okk8sckcssk4wocsgks044wsw0sogkw4gwc8gg0";
    public static final String OAUTH_CLIENT_SECRET = "64a2br3oixwk0kkw4wwscoocssss0cwg0og8g0ssggcs80owww";

    // Folders
    final public static String FOLDER_VIDEONA_MASTERS = "Videona_Masters";
    final public static String FOLDER_VIDEONA_EDITED = "Videona_Edited";
    final public static String FOLDER_VIDEONA = "Videona";
    final public static String FOLDER_VIDEONA_TEMP = ".temporal";
    final public static String FOLDER_VIDEONA_TEMP_DEPRECATED = ".temp";

    final public static String PATH_APP = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_MOVIES) + File.separator + FOLDER_VIDEONA;

    final public static String PATH_APP_EDITED = PATH_APP;

    final public static String PATH_APP_MASTERS = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_MOVIES) + File.separator + FOLDER_VIDEONA_MASTERS;

    final public static String PATH_APP_TEMP = PATH_APP + File.separator + FOLDER_VIDEONA_TEMP;
    final public static String PATH_APP_TEMP_DEPRECATED = PATH_APP + File.separator + FOLDER_VIDEONA_TEMP_DEPRECATED;
    final public static String VIDEO_MUSIC_TEMP_FILE = PATH_APP + File.separator + FOLDER_VIDEONA_TEMP + File.separator + "tempAV.mp4";
    final public static String VIDEO_CUT_AUX_NAME = "/videona_trim.mp4";

    final public static String AUDIO_MUSIC_FILE_EXTENSION = ".m4a";

    // Project
    //TODO define this values
    final public static String PROJECT_TITLE = "model";
    final public static String FOLDER_VIDEONA_PRIVATE_MODEL = "model";
}