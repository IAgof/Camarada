package com.videonasocialmedia.camarada.domain;

import com.videonasocialmedia.camarada.utils.Constants;
import com.videonasocialmedia.camarada.utils.Utils;

import java.io.File;

/**
 * Created by Veronica Lago Fominaya on 22/01/2016.
 */
public class RemoveFilesInTempFolderUseCase {

    public RemoveFilesInTempFolderUseCase() {}

    public void removeFilesInTempFolder() {
        Utils.cleanDirectory(new File(Constants.PATH_APP_TEMP));
    }
}
