package com.videonasocialmedia.camarada.domain;

import com.googlecode.mp4parser.authoring.Movie;
import com.videonasocialmedia.muxer.Appender;
import com.videonasocialmedia.muxer.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by Veronica Lago Fominaya on 18/01/2016.
 */
public class ExportUseCase {
    private OnExportFinishedListener onExportFinishedListener;
    private Appender appender;
    private String pathVideoEdited;

    public ExportUseCase(OnExportFinishedListener onExportFinishedListener) {
        this.onExportFinishedListener = onExportFinishedListener;
        appender = new Appender();
    }

    public void export(List<String> videoPaths) {
        pathVideoEdited = File.separator + "V_EDIT_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".mp4";
        Movie mergedVideoWithoutAudio = appendVideos(videoPaths);
        if(mergedVideoWithoutAudio != null) {
            Movie result = addAudio(mergedVideoWithoutAudio, getAudioPath(), getMovieDuration());
            if(result != null) {
                try {
                    Utils.createFile(result, pathVideoEdited);
                    onExportFinishedListener.onExportSuccess(pathVideoEdited);
                } catch (IOException e) {
                    onExportFinishedListener.onExportError(String.valueOf(e));
                }
            }
        }
    }

    private Movie appendVideos(List<String> videoPaths) {
        Movie result = null;
        try {
            result = appender.appendVideos(videoPaths, false);
        } catch (IOException e) {
            onExportFinishedListener.onExportError(String.valueOf(e));
        }
        return result;
    }

    private String getAudioPath() {
        // TODO ver cómo coger la música
        return "lhjafsh";
    }

    private double getMovieDuration() {
        // TODO ver cómo coger la duración del vídeo
        return 1500;
    }

    private Movie addAudio(Movie movie, String audioPath, double durationMovie) {
        Movie result = null;
        try {
            result = appender.addAudio(movie, audioPath, durationMovie);
        } catch (IOException e) {
            onExportFinishedListener.onExportError(String.valueOf(e));
        }
        return result;
    }

}
