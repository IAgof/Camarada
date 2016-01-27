package com.videonasocialmedia.Camarada.presentation.mvp.views;

/**
 * Created by Veronica Lago Fominaya on 19/01/2016.
 */
public interface RecordView {

    void showRecordButton();

    void showStopButton();

    void showFlashOn(boolean on);

    void showFlashSupported(boolean state);

    void showFrontCameraSelected();

    void showBackCameraSelected();

    void showError(String errorMessage); //videonaView

    void showError(int stringResourceId); //videonaView

    void goToShare(String videoToSharePath);

    void showProgressDialog();

    void hideProgressDialog();

    void showMessage(int stringToast);

    void enableShareButton();

    void disableShareButton();

    void changeSkin(int backgroundId);

    void showSkinWoodButton();

    void showSkinLeatherButton();

}
