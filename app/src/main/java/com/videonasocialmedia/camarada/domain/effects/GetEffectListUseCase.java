package com.videonasocialmedia.camarada.domain.effects;

import com.videonasocialmedia.camarada.model.entities.editor.effects.Effect;
import com.videonasocialmedia.camarada.model.entities.sources.EffectProvider;

import java.util.List;

public class GetEffectListUseCase {


    public static List<Effect> getShaderEffectsList() {
        return EffectProvider.getShaderEffectList();
    }

}
