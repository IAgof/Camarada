package com.videonasocialmedia.kamarada.domain.effects;

import com.videonasocialmedia.kamarada.model.entities.ShaderEffect;
import com.videonasocialmedia.kamarada.model.repository.EffectProvider;

import java.util.List;

public class GetEffectListUseCase {


    public static List<ShaderEffect> getShaderEffectsList() {
        return EffectProvider.getShaderEffectList();
    }

}
