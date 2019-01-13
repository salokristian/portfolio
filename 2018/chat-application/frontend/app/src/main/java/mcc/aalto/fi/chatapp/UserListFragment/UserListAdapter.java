package mcc.aalto.fi.chatapp.UserListFragment;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import mcc.aalto.fi.chatapp.ChatListFragment.ChatViewHolder;
import mcc.aalto.fi.chatapp.Models.User;
import mcc.aalto.fi.chatapp.R;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UserListAdapter extends BaseAdapter {

    private List<User> userList;
    private Context context;
    private Set<User> selected;

    public UserListAdapter(Context parent, List<User> userList) {
        this.userList = userList;
        this.context = parent;
        this.selected = new HashSet<>();
    }

    public Set<User> getSelectedUsers() {
        return selected;
    }

    /**
     * Preserve selection when replacing adapter.
     */
    public UserListAdapter createFrom(List<User> users) {
        UserListAdapter newAdaper = new UserListAdapter(context, users);
        newAdaper.selected = selected;
        return newAdaper;
    }

    @Override
    public int getCount() {
        return userList.size();
    }

    @Override
    public Object getItem(int i) {
        return userList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ChatViewHolder viewHolder;
        User user = (User) getItem(i);

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

        Log.d("SELECTION", selected.toString());
        viewHolder.icon.setOnClickListener(view1 -> { ;
            if (selected.contains(user)) {
                selected.remove(user);
            } else {
                selected.add(user);
            }
            setIcon(viewHolder.icon, user);
        });

        viewHolder.title.setText(user.displayName);
        setIcon(viewHolder.icon, user);


        return view;
    }

    private void setIcon(ImageView imageView, User user) {
        // TODO: use proper images instead of placeholders
        if (selected.contains(user)) {
            imageView.setImageResource(R.drawable.ic_checkmark_checked);
        } else {
            imageView.setImageResource(R.drawable.ic_checkmark);
        }
    }
}

