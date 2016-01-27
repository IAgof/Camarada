package com.videonasocialmedia.Kamarada.domain.effects;

import com.videonasocialmedia.Kamarada.model.entities.ShaderEffect;
import com.videonasocialmedia.Kamarada.model.repository.EffectProvider;

import java.util.List;

public class GetEffectListUseCase {


    public static List<ShaderEffect> getShaderEffectsList() {
        return EffectProvider.getShaderEffectList();
    }

}
