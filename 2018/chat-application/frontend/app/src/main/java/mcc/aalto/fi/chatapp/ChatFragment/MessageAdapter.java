package mcc.aalto.fi.chatapp.ChatFragment;

import android.content.Context;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.storage.StorageReference;
import com.leocardz.link.preview.library.LinkPreviewCallback;
import com.leocardz.link.preview.library.SourceContent;
import com.leocardz.link.preview.library.TextCrawler;

import java.util.List;

import mcc.aalto.fi.chatapp.GlideApp;
import mcc.aalto.fi.chatapp.Models.Message;
import mcc.aalto.fi.chatapp.R;


public class MessageAdapter extends BaseAdapter {
    private Context context;
    private List<Message> messages;
    private String currentUserId;
    private String downloadResolution;
    private StorageReference chatStorageRef;
    private OnImageClickListener imageClickListener;

    private final int URL_TITLE_TEXT_LIMIT = 15;
    private final int URL_DESCRIPTION_TEXT_LIMIT = 50;

    public MessageAdapter(Context context, List<Message> messages, String currentUserId, String downloadResolution, StorageReference chatStorageRef, OnImageClickListener imageClickListener) {
        this.context = context;
        this.messages = messages;
        this.currentUserId = currentUserId;
        this.downloadResolution = downloadResolution;
        this.chatStorageRef = chatStorageRef;
        this.imageClickListener = imageClickListener;
    }

    @Override
    public int getCount() {
        return messages.size();
    }

    @Override
    public Object getItem(int index) {
        return messages.get(index);
    }

    @Override
    public long getItemId(int index) {
        return index;
    }

    @Override
    public View getView(int index, View convertView, ViewGroup viewGroup) {
        // We can't use convertView here in the way it is traditionally used because the views
        // used are different for messages sent by self and others
        MessageViewHolder msgViewHolder;
        Message currentMsg = (Message) getItem(index);
        Integer messageLayoutId;

        if (currentMsg.sender.equals(currentUserId)) {
            messageLayoutId = R.layout.chat_message_self;
        } else {
            messageLayoutId = R.layout.chat_message_others;
        }

        convertView = LayoutInflater.from(context).
                inflate(messageLayoutId, viewGroup, false);
        msgViewHolder = new MessageViewHolder(convertView);
        convertView.setTag(msgViewHolder);

        renderContent(msgViewHolder, currentMsg);

        return convertView;
    }

    private void renderContent(MessageViewHolder msgViewHolder, Message message) {
        msgViewHolder.senderName.setText(getSenderName(message));
        renderSenderImage(msgViewHolder, message);

        // A message either has an image, or contains only text
        if (message.image != null) {
            msgViewHolder.text.setVisibility(View.GONE);
            msgViewHolder.urlTitle.setVisibility(View.GONE);
            msgViewHolder.urlDescription.setVisibility(View.GONE);
            msgViewHolder.urlImage.setVisibility(View.GONE);

            String imageName = message.image.getImageName(downloadResolution);
            StorageReference imageRef = chatStorageRef.child(imageName);

            GlideApp.with(context)
                    .load(imageRef)
                    .placeholder(R.drawable.chat_app_logo)
                    .into(msgViewHolder.image);

            msgViewHolder.image.setOnClickListener(view -> {
                imageClickListener.onClick(view, message.image);
            });
        } else if (message.text.contains("https") || message.text.contains("http")) {
            loadLinkPreview(msgViewHolder, message);
        } else {
            msgViewHolder.image.setVisibility(View.GONE);
            msgViewHolder.urlTitle.setVisibility(View.GONE);
            msgViewHolder.urlDescription.setVisibility(View.GONE);
            msgViewHolder.urlImage.setVisibility(View.GONE);

            msgViewHolder.text.setText(message.text);
        }
    }


    private void renderSenderImage(MessageViewHolder msgViewHolder, Message message) {
        if (message.senderData.photoUrl != null && !message.senderData.photoUrl.isEmpty()) {
            StorageReference senderImageRef = chatStorageRef.child(message.senderData.photoUrl);

            GlideApp.with(context)
                    .load(senderImageRef)
                    .placeholder(R.drawable.ic_profile)
                    .circleCrop()
                    .into(msgViewHolder.senderImage);
        }
    }

    private String getSenderName(Message message) {
        if (message.senderData != null && message.senderData.displayName != null) {
            return message.senderData.displayName;
        } else {
            return "";
        }
    }

    private boolean isNullOrBlank(String s) {
        return (s == null || s.trim().equals(""));
    }

    /**
     * Loads the given links which start with http or https
     *
     * @param msgViewHolder
     * @param message
     */
    private void loadLinkPreview(MessageViewHolder msgViewHolder, Message message) {
        TextCrawler textCrawler = new TextCrawler();
        // Get the given link
        int firstIndex = message.text.indexOf("http");
        int lastIndex = message.text.substring(firstIndex).indexOf(" ") > 0 ? message.text.substring(firstIndex).indexOf(" ") : message.text.length();
        String url = message.text.substring(firstIndex, lastIndex);
        // Set link and make it clickable
        msgViewHolder.text.setText(url);
        Linkify.addLinks(msgViewHolder.text, Linkify.WEB_URLS);
        // Hide other views
        msgViewHolder.image.setVisibility(View.GONE);
        msgViewHolder.urlTitle.setVisibility(View.INVISIBLE);
        msgViewHolder.urlDescription.setVisibility(View.INVISIBLE);
        msgViewHolder.urlImage.setVisibility(View.INVISIBLE);

        // A listener for loading link previews
        LinkPreviewCallback linkPreviewCallback = new LinkPreviewCallback() {
            @Override
            public void onPre() {
                // Any work that needs to be done before generating the preview.
            }

            @Override
            public void onPos(SourceContent sourceContent, boolean b) {
                if (!b && !isNullOrBlank(sourceContent.getTitle()) && !isNullOrBlank(sourceContent.getDescription())) {
                    msgViewHolder.urlTitle.setVisibility(View.VISIBLE);
                    msgViewHolder.urlDescription.setVisibility(View.VISIBLE);
                    msgViewHolder.urlImage.setVisibility(View.VISIBLE);

                    int titleLimit = sourceContent.getTitle().length() > URL_TITLE_TEXT_LIMIT ? URL_TITLE_TEXT_LIMIT : sourceContent.getTitle().length();
                    int descriptionLimit = sourceContent.getTitle().length() > URL_DESCRIPTION_TEXT_LIMIT ? URL_DESCRIPTION_TEXT_LIMIT : sourceContent.getDescription().length();

                    msgViewHolder.urlTitle.setText(sourceContent.getTitle().substring(0, titleLimit) + "...");
                    msgViewHolder.urlDescription.setText(sourceContent.getDescription().substring(0, descriptionLimit) + "...");

                    if (sourceContent.getImages().size() > 0) {
                        GlideApp.with(context.getApplicationContext())
                                .load(sourceContent.getImages().get(0))
                                .centerCrop()
                                .into(msgViewHolder.urlImage);
                    }
                } else {
                    msgViewHolder.urlTitle.setVisibility(View.GONE);
                    msgViewHolder.urlDescription.setVisibility(View.GONE);
                    msgViewHolder.urlImage.setVisibility(View.GONE);
                }
            }
        };
        // Start loading link preview
        textCrawler.makePreview(linkPreviewCallback, url);
    }
}

class MessageViewHolder {
    TextView text;
    ImageView image;
    TextView senderName;
    ImageView senderImage;
    TextView urlTitle;
    TextView urlDescription;
    ImageView urlImage;

    MessageViewHolder(View view) {
        this.text = view.findViewById(R.id.message_text);
        this.image = view.findViewById(R.id.message_image);
        this.senderName = view.findViewById(R.id.message_sender_name);
        this.senderImage = view.findViewById(R.id.message_sender_image);
        this.urlTitle = view.findViewById(R.id.url_title);
        this.urlDescription = view.findViewById(R.id.url_description);
        this.urlImage = view.findViewById(R.id.url_image);
    }
}
