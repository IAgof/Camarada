package com.videonasocialmedia.Kamarada.domain;

import com.videonasocialmedia.Kamarada.utils.Constants;
import com.videonasocialmedia.Kamarada.utils.Utils;

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
