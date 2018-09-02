package org.havenapp.main.ui;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import com.github.derlio.waveform.SimpleWaveformView;
import com.github.derlio.waveform.soundfile.SoundFile;

import org.havenapp.main.R;
import org.havenapp.main.model.EventTrigger;
import org.havenapp.main.resources.IResourceManager;

import java.io.File;
import java.util.List;

import nl.changer.audiowife.AudioWife;

/**
 * Created by n8fr8 on 4/16/17.
 */

public class EventTriggerAdapter extends RecyclerView.Adapter<EventTriggerAdapter.EventTriggerVH> {

    private Context context;
    private IResourceManager resourceManager;
    private List<EventTrigger> eventTriggers;

    private EventTriggerClickListener eventTriggerClickListener;

    private final static String AUTHORITY = "org.havenapp.main.fileprovider";

    public EventTriggerAdapter(Context context, List<EventTrigger> eventTriggers,
                               IResourceManager resourceManager, EventTriggerClickListener eventTriggerClickListener) {
        this.context = context;
        this.resourceManager = resourceManager;
        this.eventTriggers = eventTriggers;
        this.eventTriggerClickListener = eventTriggerClickListener;
    }


    @Override
    public EventTriggerVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event, parent, false);

        return new EventTriggerVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventTriggerVH holder, int position) {

        final EventTrigger eventTrigger = eventTriggers.get(position);

        String title = eventTrigger.getStringType(resourceManager);
        String desc = eventTrigger.getMTime().toLocaleString();

        holder.image.setVisibility(View.GONE);
        holder.video.setVisibility(View.GONE);
        holder.extra.setVisibility(View.GONE);
        holder.sound.setVisibility(View.GONE);


        if (eventTrigger.getMPath() != null)
        {
            switch (eventTrigger.getMType()) {
                case EventTrigger.CAMERA_VIDEO:
                    holder.video.setVisibility(View.VISIBLE);
                    BitmapDrawable bitmapD = new BitmapDrawable(context.getResources(),
                            ThumbnailUtils.createVideoThumbnail(eventTrigger.getMPath(),
                            MediaStore.Video.Thumbnails.FULL_SCREEN_KIND));
                    holder.video.setBackground(bitmapD);
                    holder.video.setOnClickListener(view -> {
                        if (eventTriggerClickListener != null) {
                            eventTriggerClickListener.onVideoClick(eventTrigger);
                        }
                    });

                    holder.video.setOnLongClickListener(view -> {
                        if (eventTriggerClickListener != null) {
                            eventTriggerClickListener.onVideoLongClick(eventTrigger);
                        }
                        return false;
                    });
                    break;
                case EventTrigger.CAMERA:
                    holder.image.setVisibility(View.VISIBLE);

                    Uri fileUri = FileProvider.getUriForFile(
                            context,
                            AUTHORITY,
                            new File(eventTrigger.getMPath()));
                    holder.image.setImageURI(fileUri);

                    holder.image.setOnClickListener(view -> {
                        if (eventTriggerClickListener != null)
                            eventTriggerClickListener.onImageClick(eventTrigger);
                    });

                    holder.image.setOnLongClickListener(view -> {
                        if (eventTriggerClickListener != null)
                            eventTriggerClickListener.onImageLongClick(eventTrigger);
                        return false;
                    });
                    break;
                case EventTrigger.MICROPHONE:
                    LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                    holder.sound.setVisibility(View.VISIBLE);
                    final File fileSound = new File(eventTrigger.getMPath());
                    try {
                        final SoundFile soundFile = SoundFile.create(fileSound.getPath(), new SoundFile.ProgressListener() {
                            int lastProgress = 0;

                            @Override
                            public boolean reportProgress(double fractionComplete) {
                                final int progress = (int) (fractionComplete * 100);
                                if (lastProgress == progress) {
                                    return true;
                                }
                                lastProgress = progress;

                                return true;
                            }
                        });
                        holder.sound.setAudioFile(soundFile);
                        holder.sound.invalidate();
                    } catch (Exception e) {
                    }

                    holder.extra.setVisibility(View.VISIBLE);
                    holder.extra.removeAllViews();

                    AudioWife audioWife = new AudioWife();
                    audioWife.init(context, Uri.fromFile(fileSound))
                            .useDefaultUi(holder.extra, inflater);


                    break;
                case EventTrigger.ACCELEROMETER:
                    desc += "\n" + resourceManager.getString(R.string.data_speed) + ": " + eventTrigger.getMPath();

                    break;
                case EventTrigger.LIGHT:
                    desc += "\n" + resourceManager.getString(R.string.data_light) + ": " + eventTrigger.getMPath();

                    break;
                case EventTrigger.PRESSURE:
                    desc += "\n" + resourceManager.getString(R.string.data_pressure) + ": " + eventTrigger.getMPath();
                    break;
                case EventTrigger.POWER:
                    desc += "\n" + resourceManager.getString(R.string.data_power) + ": " + eventTrigger.getMPath();
                    break;
            }

        }

        holder.title.setText(title);
        holder.note.setText(desc);


    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);

        AudioWife.getInstance().release();
    }

    @Override
    public int getItemCount() {
        return eventTriggers.size();
    }

    class EventTriggerVH extends RecyclerView.ViewHolder {
        TextView title, note;
        ImageView image;
        VideoView video;
        ViewGroup extra;
        SimpleWaveformView sound;
        public EventTriggerVH(View itemView) {
            super(itemView);

           title = itemView.findViewById(R.id.event_item_title);
            note = itemView.findViewById(R.id.event_item_desc);
            image = itemView.findViewById(R.id.event_item_image);
            video = itemView.findViewById(R.id.event_item_video);
            extra = itemView.findViewById(R.id.event_item_extra);
            sound = itemView.findViewById(R.id.event_item_sound);
        }
    }

    public interface EventTriggerClickListener {
        void onVideoClick(EventTrigger eventTrigger);

        void onVideoLongClick(EventTrigger eventTrigger);

        void onImageClick(EventTrigger eventTrigger);

        void onImageLongClick(EventTrigger eventTrigger);
    }

}
