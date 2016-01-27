package com.videonasocialmedia.camarada.model.repository;

import com.videonasocialmedia.avrecorder.Filters;
import com.videonasocialmedia.camarada.R;
import com.videonasocialmedia.camarada.model.entities.ShaderEffect;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Veronica Lago Fominaya on 25/11/2015.
 */
public class EffectProvider {

    public static List<ShaderEffect> getShaderEffectList() {

        List<ShaderEffect> camaradaEffects = new ArrayList<>();

        camaradaEffects.add(
                new ShaderEffect("AD4", "B&W", R.mipmap.activity_record_filter_bw_icon_normal,
                        Filters.FILTER_MONO));
        camaradaEffects.add(
                new ShaderEffect("AD8", "Sepia", R.mipmap.activity_record_filter_sepia_icon_normal,
                        Filters.FILTER_SEPIA));
        camaradaEffects.add(
                new ShaderEffect("AD1", "Blue", R.mipmap.activity_record_filter_blue_icon_normal,
                        Filters.FILTER_AQUA));

        return camaradaEffects;
    }


}
