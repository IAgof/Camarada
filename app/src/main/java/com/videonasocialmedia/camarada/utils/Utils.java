package com.videonasocialmedia.camarada.utils;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.Arrays;

/**
 * Created by Veronica Lago Fominaya on 26/06/2015.
 */
public class Utils {

    public static void createFile(Movie movie, String outPath) throws IOException {
        Container out = new DefaultMp4Builder().build(movie);
        FileChannel fc = new RandomAccessFile(outPath, "rw").getChannel();
        out.writeContainer(fc);
        fc.close();
    }

    public static double correctTimeToSyncSample(Track track, double cutHere, boolean next) {
        double[] timeOfSyncSamples = new double[track.getSyncSamples().length];
        long currentSample = 0;
        double currentTime = 0;
        for (int i = 0; i < track.getSampleDurations().length; i++) {
            long delta = track.getSampleDurations()[i];

            if (Arrays.binarySearch(track.getSyncSamples(), currentSample + 1) >= 0) {
                // samples always start with 1 but we start with zero therefore +1
                timeOfSyncSamples[Arrays.binarySearch(track.getSyncSamples(), currentSample + 1)] = currentTime;
            }
            currentTime += (double) delta / (double) track.getTrackMetaData().getTimescale();
            currentSample++;
        }
        double previous = 0;
        for (double timeOfSyncSample : timeOfSyncSamples) {
            if (timeOfSyncSample > cutHere) {
                if (next) {
                    return timeOfSyncSample;
                } else {
                    return previous;
                }
            }
            previous = timeOfSyncSample;
        }
        return timeOfSyncSamples[timeOfSyncSamples.length - 1];
    }

    public static long[] getStartAndStopSamples(Track track, double startTime, double endTime) {
        long currentSample = 0;
        double currentTime = 0;
        double lastTime = -1;
        long startSample = -1;
        long endSample = -1;

        for (int i = 0; i < track.getSampleDurations().length; i++) {
            long delta = track.getSampleDurations()[i];

            if (currentTime > lastTime && currentTime <= startTime) {
                // current sample is still before the new starttime
                startSample = currentSample;
            }
            if (currentTime > lastTime && currentTime <= endTime) {
                // current sample is after the new start time and still before the new endtime
                endSample = currentSample;
            }
            lastTime = currentTime;
            currentTime += (double) delta / (double) track.getTrackMetaData().getTimescale();
            currentSample++;
        }
        return new long[]{startSample, endSample};
    }

    public static Uri obtainUriToShare(Context context, String videoPath) {
        Uri uri;
        if (videoPath != null) {
            ContentResolver resolver = context.getContentResolver();
            uri = getUriFromContentProvider(resolver, videoPath);
            if (uri == null) {
                uri = createUriToShare(resolver, videoPath);
            }
        } else {
            uri = null;
        }
        return uri;
    }

    private static Uri getUriFromContentProvider(ContentResolver resolver, String videoPath) {
        Uri uri = null;
        String[] retCol = {MediaStore.Audio.Media._ID};
        Cursor cursor = resolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                retCol,
                MediaStore.MediaColumns.DATA + "='" + videoPath + "'", null, null);

        if (cursor.getCount() != 0) {
            cursor.moveToFirst();
            int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
            uri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    id);
            cursor.close();
        }
        return uri;
    }

    private static Uri createUriToShare(ContentResolver resolver, String videoPath) {
        ContentValues content = new ContentValues(4);
        content.put(MediaStore.Video.VideoColumns.TITLE, videoPath);
        content.put(MediaStore.Video.VideoColumns.DATE_ADDED,
                System.currentTimeMillis());
        content.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
        content.put(MediaStore.Video.Media.DATA, videoPath);
        return resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                content);
    }

    /**
     * Returns whether the current device is running Android 4.4, KitKat, or newer
     */
    public static boolean isKitKat() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    }
}