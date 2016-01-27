package com.videonasocialmedia.Camarada.domain.effects;

import com.videonasocialmedia.Camarada.model.entities.ShaderEffect;
import com.videonasocialmedia.Camarada.model.repository.EffectProvider;

import java.util.List;

public class GetEffectListUseCase {


    public static List<ShaderEffect> getShaderEffectsList() {
        return EffectProvider.getShaderEffectList();
    }

}
