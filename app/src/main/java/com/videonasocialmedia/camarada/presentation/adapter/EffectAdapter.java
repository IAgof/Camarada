/*
 * Copyright (c) 2015. Videona Socialmedia SL
 * http://www.videona.com
 * info@videona.com
 * All rights reserved
 */

package com.videonasocialmedia.camarada.presentation.adapter;


import android.content.Context;

import com.videonasocialmedia.camarada.model.entities.editor.effects.Effect;
import com.videonasocialmedia.camarada.presentation.listener.OnEffectSelectedListener;

import java.util.List;

/**
 * This class is used to show the camera effects gallery.
 */
public class EffectAdapter {

    private Context context;
    private List<Effect> effects;
    private OnEffectSelectedListener onEffectSelectedListener;
    private int selectedPosition = -1;
    private int previousSelectionPosition = -1;
    private boolean effectSelected = false;

    /**
     * Constructor.
     *
     * @param effects the list of the available effects
     */
    public EffectAdapter(List<Effect> effects, OnEffectSelectedListener listener) {
        this.effects = effects;
        this.onEffectSelectedListener = listener;
    }

    /**
     * This method returns the list of the available effects
     *
     * @return
     */
    public List<Effect> getElementList() {
        return effects;
    }

    public void setNextEffectPosition(int position){
        selectedPosition = position;

    }

    /**
     * Returns the effect for a given position
     *
     * @param position the position of the effect element
     * @return
     */
    public Effect getEffect(int position) {
        return effects.get(position);
    }

    /**
     * Sets the listener of the recycler view
     *
     * @param onEffectSelectedListener
     */
    public void setClickListener(OnEffectSelectedListener
                                         onEffectSelectedListener) {
        this.onEffectSelectedListener = onEffectSelectedListener;
    }


    /**
     * Checks if some effect has been selected
     *
     * @return
     */
    public boolean isEffectSelected(){
        return effectSelected;
    }

    public void resetSelectedEffect() {
        selectedPosition = -1;
        effectSelected = false;
    }

    public int getPreviousSelectionPosition() {
        return previousSelectionPosition;
    }

    public int getSelectionPosition() {
        if(selectedPosition == -1) {
            return 0;
        }
        return selectedPosition;
    }


}