package com.cxromos.castbox.ui.main;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.cxromos.castbox.R;
import com.cxromos.castbox.data.model.Cast;
import com.cxromos.castbox.ui.track.TrackActivity;
import com.cxromos.castbox.util.LocalUtils;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CastAdapter extends RecyclerView.Adapter<CastAdapter.CastHolder> {
    private List<Cast> mCasts;

    @Inject
    public CastAdapter() { this.mCasts = new ArrayList<>(); }

    @NonNull
    @Override
    public CastHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cast, parent, false);
        return new CastHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CastHolder holder, int position) {
        final Context context = holder.itemView.getContext();

        final Cast cast = mCasts.get(position);
        holder.castTitleText.setText(cast.title);
        holder.castDescriptionText.setText(LocalUtils.stripHtml(cast.description));
        holder.castAuthorText.setText(cast.author);
        Glide.with(context)
                .load(cast.coverMedium)
                .into(holder.castImage);

        holder.castContainer.setOnClickListener(v -> context.startActivity(TrackActivity.getStartIntent(context, cast)));
    }

    @Override
    public int getItemCount() {
        return mCasts.size();
    }

    void setCasts(List<Cast> casts) {
        mCasts = casts;
        notifyDataSetChanged();
    }

    class CastHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.container_cast) View castContainer;
        @BindView(R.id.cast_title) TextView castTitleText;
        @BindView(R.id.cast_description) TextView castDescriptionText;
        @BindView(R.id.cast_author) TextView castAuthorText;
        @BindView(R.id.cast_image) ImageView castImage;

        CastHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
