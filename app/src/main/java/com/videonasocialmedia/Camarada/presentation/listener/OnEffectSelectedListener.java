/*
 * Copyright (c) 2015. Videona Socialmedia SL
 * http://www.videona.com
 * info@videona.com
 * All rights reserved
 */

package com.videonasocialmedia.Camarada.presentation.listener;

import com.videonasocialmedia.Camarada.model.entities.Effect;

/**
 * Created by Veronica Lago Fominaya on 03/11/2015.
 */
public interface OnEffectSelectedListener {
    /**
     * @param effect
     */
    void onEffectSelected(Effect effect);

    void onEffectSelectionCancel(Effect effect);

}
