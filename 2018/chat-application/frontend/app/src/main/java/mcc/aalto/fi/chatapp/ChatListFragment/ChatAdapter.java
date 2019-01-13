package mcc.aalto.fi.chatapp.ChatListFragment;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import mcc.aalto.fi.chatapp.ChatFragment.ChatFragment;
import mcc.aalto.fi.chatapp.GlideApp;
import mcc.aalto.fi.chatapp.Models.Chat;
import mcc.aalto.fi.chatapp.Models.User;
import mcc.aalto.fi.chatapp.R;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ChatAdapter extends BaseAdapter {

    private List<Chat> chatList;
    private Context context;
    private String userId;

    public ChatAdapter(Context parent, List<Chat> chatList, String userId) {
        this.chatList = chatList;
        this.context = parent;
        this.userId = userId;
    }

    @Override
    public int getCount() {
        return chatList.size();
    }

    @Override
    public Object getItem(int i) {
        return chatList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ChatViewHolder viewHolder;

        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.chat_item, viewGroup, false);

            viewHolder = new ChatViewHolder();

            viewHolder.title = view.findViewById(R.id.chatTitle);
            viewHolder.subtitle = view.findViewById(R.id.chatSubtitle);
            viewHolder.icon = view.findViewById(R.id.chatIcon);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ChatViewHolder) view.getTag();
        }

        Chat chat = (Chat) getItem(i);

        viewHolder.title.setText(ChatFragment.getTitle(chat, userId));
        viewHolder.subtitle.setText(context.getResources().getString(R.string.chatlist_subtitle_members, chat.members.size()));

        if (chat.members.size() == 2) {
            getFirstPhotoUrl(chat.members.keySet(), (ref) -> {
                GlideApp.with(context)
                        .load(ref)
                        .placeholder(R.drawable.ic_chat)
                        .circleCrop()
                        .into(viewHolder.icon);
            });
        } else {
            viewHolder.icon.setImageResource(R.drawable.ic_chat);
        }

        return view;
    }

    private void getFirstPhotoUrl(Set<String> userIds, Consumer<StorageReference> callback) {
        for (String id : userIds) {
            if (id.equals(userId)) { continue; }

            FirebaseDatabase.getInstance().getReference().child("users").child("public").child(id).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    User u = dataSnapshot.getValue(User.class);
                    callback.accept(FirebaseStorage.getInstance().getReference().child(u.photoUrl));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) { }
            });
            return;
        }

    }
}

