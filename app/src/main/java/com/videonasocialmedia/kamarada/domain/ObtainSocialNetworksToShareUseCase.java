package com.videonasocialmedia.kamarada.domain;

import com.videonasocialmedia.kamarada.model.entities.SocialNetwork;
import com.videonasocialmedia.kamarada.model.repository.SocialNetworkRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jca on 18/1/16.
 */
public class ObtainSocialNetworksToShareUseCase {
    private SocialNetworkRepository repository;

    public ObtainSocialNetworksToShareUseCase() {
        repository = new SocialNetworkRepository();
    }

    public List<SocialNetwork> getAllSocialNetworks() {
        return repository.getSocialNetworks();
    }

    public List<SocialNetwork> getKnownSocialNetworks() {
        List<SocialNetwork> allNetworks = repository.getSocialNetworks();
        List<SocialNetwork> result = new ArrayList<>();
        for (SocialNetwork current :
                allNetworks) {
            if (!current.getName().equalsIgnoreCase("generic")) {
                result.add(current);
            }
        }
        return result;
    }
}
