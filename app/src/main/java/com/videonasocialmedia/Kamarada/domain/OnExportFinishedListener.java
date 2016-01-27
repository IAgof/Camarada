package com.videonasocialmedia.Kamarada.domain;

/**
 * Created by Veronica Lago Fominaya on 18/01/2016.
 */
public interface OnExportFinishedListener {
    void onExportError(String error);
    void onExportSuccess(String path);
}
