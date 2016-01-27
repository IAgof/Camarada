package com.videonasocialmedia.kamarada.domain;

import com.videonasocialmedia.kamarada.utils.Constants;
import com.videonasocialmedia.kamarada.utils.Utils;

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
