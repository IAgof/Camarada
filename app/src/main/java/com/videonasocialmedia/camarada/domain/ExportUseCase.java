package com.videonasocialmedia.camarada.domain;

import android.os.Environment;

import com.googlecode.mp4parser.authoring.Movie;
import com.videonasocialmedia.camarada.domain.muxer.Muxer;
import com.videonasocialmedia.camarada.utils.Utils;

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
    private Muxer muxer;
    private String pathVideoExported;

    public ExportUseCase(OnExportFinishedListener onExportFinishedListener) {
        this.onExportFinishedListener = onExportFinishedListener;
        muxer = new Muxer();
    }

    public void export(List<String> videoPaths) {
        // TODO definir dónde se va a guardar el vídeo y con qué nombre
        pathVideoExported = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_MOVIES) + File.separator + "V_EDIT_" +
                new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".mp4";
        Movie mergedVideoWithoutAudio = appendVideos(videoPaths);
        if(mergedVideoWithoutAudio != null) {
            Movie result = addAudio(mergedVideoWithoutAudio, getAudioPath(),
                    getMovieDuration(videoPaths));
            if(result != null) {
                try {
                    Utils.createFile(result, pathVideoExported);
                    onExportFinishedListener.onExportSuccess(pathVideoExported);
                } catch (IOException e) {
                    onExportFinishedListener.onExportError(String.valueOf(e));
                }
            }
        }
    }

    private Movie appendVideos(List<String> videoPaths) {
        Movie result = null;
        try {
            result = muxer.appendVideos(videoPaths, false);
        } catch (IOException e) {
            onExportFinishedListener.onExportError(String.valueOf(e));
        }
        return result;
    }

    private String getAudioPath() {
        // TODO ver de dónde coger la música
        return Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_MOVIES) + File.separator + "audio.m4a";
    }

    private double getMovieDuration(List<String> videoPaths) {
        return muxer.getFileDuration(videoPaths);
    }

    private Movie addAudio(Movie movie, String audioPath, double durationMovie) {
        Movie result = null;
        try {
            result = muxer.addAudio(movie, audioPath, durationMovie);
        } catch (IOException e) {
            onExportFinishedListener.onExportError(String.valueOf(e));
        }
        return result;
    }

}
