package com.videonasocialmedia.muxer;

import com.googlecode.mp4parser.authoring.Movie;

import java.io.IOException;

/**
 * Created by Veronica Lago Fominaya on 18/01/2016.
 */
public class VideoTrimmer extends Trimmer {

    @Override
    public Movie trim(String videoPath, double startTime, double endTime) throws IOException {
        return super.trim(videoPath, startTime, endTime);
    }
}
