package com.videonasocialmedia.camarada.domain;

import com.videonasocialmedia.camarada.model.SocialNetwork;
import com.videonasocialmedia.camarada.model.repository.SocialNetworkRepository;

import java.util.List;

/**
 * Created by jca on 18/1/16.
 */
public class ObtainSocialNetworksToShareUseCase {
    private SocialNetworkRepository repository;
    public ObtainSocialNetworksToShareUseCase() {
        repository= new SocialNetworkRepository();
    }

    public List<SocialNetwork> getSocialNetworks(){
        return repository.getSocialNetworks();
    }
}
