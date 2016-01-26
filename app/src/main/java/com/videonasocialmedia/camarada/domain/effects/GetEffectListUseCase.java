package com.videonasocialmedia.camarada.domain.effects;

import com.videonasocialmedia.camarada.model.entities.Effect;
import com.videonasocialmedia.camarada.model.entities.ShaderEffect;
import com.videonasocialmedia.camarada.model.repository.EffectProvider;

import java.util.List;

public class GetEffectListUseCase {


    public static List<ShaderEffect> getShaderEffectsList() {
        return EffectProvider.getShaderEffectList();
    }

}
