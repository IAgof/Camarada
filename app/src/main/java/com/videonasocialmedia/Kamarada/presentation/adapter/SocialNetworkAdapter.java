package com.videonasocialmedia.Kamarada.presentation.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.videonasocialmedia.Kamarada.R;
import com.videonasocialmedia.Kamarada.model.entities.SocialNetwork;
import com.videonasocialmedia.Kamarada.presentation.listener.OnSocialNetworkClickedListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by jca on 19/1/16.
 */
public class SocialNetworkAdapter extends RecyclerView.Adapter<SocialNetworkAdapter.SocialNetworkViewHolder> {


    private List<SocialNetwork> socialNetworks;
    private OnSocialNetworkClickedListener listener;


    public SocialNetworkAdapter(List<SocialNetwork> socialNetworks,
                                OnSocialNetworkClickedListener listener) {
        this.socialNetworks = socialNetworks;
        this.listener = listener;
        notifyDataSetChanged();
    }

    public SocialNetworkAdapter(OnSocialNetworkClickedListener listener) {
        this(new ArrayList<SocialNetwork>(), listener);
    }

    public void setSocialNetworks(List<SocialNetwork> socialNetworks) {
        this.socialNetworks = socialNetworks;
        notifyDataSetChanged();
    }

    @Override
    public SocialNetworkViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.socialnetwork_viewholder, parent, false);
        return new SocialNetworkViewHolder(rowView);
    }

    @Override
    public void onBindViewHolder(SocialNetworkViewHolder holder, int position) {
        SocialNetwork current = socialNetworks.get(position);
        holder.setup(current);
    }

    @Override
    public int getItemCount() {
        return socialNetworks.size();
    }

    class SocialNetworkViewHolder extends RecyclerView.ViewHolder {


        @Bind(R.id.icon)
        ImageView icon;
        @Bind(R.id.name)
        TextView name;

        public SocialNetworkViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    listener.onSocialNetworkClicked(socialNetworks.get(position));
                }
            });
        }

        void setup(SocialNetwork socialNetwork) {
            icon.setImageDrawable(socialNetwork.getIcon());
            name.setText(socialNetwork.getName());
        }
    }
}
