package com.videonasocialmedia.kamarada.domain.muxer;

import android.util.Log;

import com.coremedia.iso.IsoFile;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Veronica Lago Fominaya on 18/01/2016.
 */
public class Muxer {

    private Trimmer trimmer;

    public Movie appendVideos(List<String> videoPaths, boolean addOriginalAudio) throws IOException {
        List<Movie> movieList = getMovieList(videoPaths);
        List<Track> videoTracks = new LinkedList<>();
        List<Track> audioTracks = new LinkedList<>();

        for (Movie m : movieList) {
            for (Track t : m.getTracks()) {
                if (addOriginalAudio) {
                    if (t.getHandler().equals("soun")) {
                        audioTracks.add(t);
                    }
                }
                if (t.getHandler().equals("vide")) {
                    videoTracks.add(t);
                }
            }
        }
        return createMovie(audioTracks, videoTracks);
    }

    private List<Movie> getMovieList(List<String> videoPaths) throws IOException {
        List<Movie> movieList = new ArrayList<>();

        for (String videoPath : videoPaths) {
            long start = System.currentTimeMillis();
            Movie movie = MovieCreator.build(videoPath);
            long spent = System.currentTimeMillis() - start;
            movieList.add(movie);
        }
        return movieList;
    }

    private Movie createMovie(List<Track> audioTracks, List<Track> videoTracks) throws IOException {
        Movie result = new Movie();

        if (audioTracks.size() > 0) {
            result.addTrack(new AppendTrack(audioTracks.toArray(new Track[audioTracks.size()])));
        }
        if (videoTracks.size() > 0) {
            result.addTrack(new AppendTrack(videoTracks.toArray(new Track[videoTracks.size()])));
        }
        return result;
    }

    public Movie addAudio(Movie movie, String audioPath, double movieDuration) throws IOException {

        double audioDuration = getFileDuration(audioPath);
        trimmer = new AudioTrimmer();
        List<Movie> audioList = new ArrayList<>();
        List<Track> audioTracks = new LinkedList<>();

        if (audioDuration < movieDuration) {
            repeatAudio(audioList, audioPath, audioDuration, movieDuration);
        } else if (audioDuration > movieDuration) {
            trimAudio(audioList, audioPath, movieDuration);
        } else
            audioList.add(MovieCreator.build(audioPath));

        for (Movie m : audioList) {
            for (Track t : m.getTracks()) {
                if (t.getHandler().equals("soun")) {
                    audioTracks.add(t);
                }
            }
        }
        if (audioTracks.size() > 0) {
            movie.addTrack(new AppendTrack(audioTracks.toArray(new Track[audioTracks.size()])));
        }
        return movie;
    }

    private double getFileDuration(String filePath) throws IOException {
        IsoFile isoFile = new IsoFile(filePath);
        double lengthInMSeconds = (double)
                (isoFile.getMovieBox().getMovieHeaderBox().getDuration() /
                        isoFile.getMovieBox().getMovieHeaderBox().getTimescale()) * 1000;
        return lengthInMSeconds;
    }

    private void repeatAudio(List<Movie> audioList, String audioPath, double audioDuration,
                             double movieDuration) throws IOException {
        int numCompleteAudioFilesInMovieDuration = (int) (movieDuration / audioDuration);

        double durationOfLastAudioFile = movieDuration -
                (numCompleteAudioFilesInMovieDuration * audioDuration);

        for (int i = 0; i < numCompleteAudioFilesInMovieDuration; i++)
            audioList.add(MovieCreator.build(audioPath));

        audioList.add(trimmer.trim(audioPath, 0, durationOfLastAudioFile));
    }

    private void trimAudio(List<Movie> audioList, String audioPath, double movieDuration)
            throws IOException {
        audioList.add(trimmer.trim(audioPath, 0, movieDuration));
    }

    public double getFileDuration(List<String> videoPaths) {
        double duration = 0;
        for (String path : videoPaths) {
            try {
                duration += getFileDuration(path);
            } catch (IOException e) {
                Log.e("IOException", "error", e);
            }
        }
        return duration;
    }
}
