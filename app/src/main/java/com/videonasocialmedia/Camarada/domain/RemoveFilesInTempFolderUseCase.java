package com.videonasocialmedia.Camarada.domain;

import com.videonasocialmedia.Camarada.utils.Constants;
import com.videonasocialmedia.Camarada.utils.Utils;

import java.io.File;

/**
 * Created by Veronica Lago Fominaya on 22/01/2016.
 */
public class RemoveFilesInTempFolderUseCase {

    public RemoveFilesInTempFolderUseCase() {
    }

    public void removeFilesInTempFolder() {
        Utils.cleanFilesInDirectory(new File(Constants.PATH_APP_TEMP));
    }
}
