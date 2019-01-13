package mcc.aalto.fi.chatapp.UserListFragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.jakewharton.rxbinding.widget.RxTextView;
import mcc.aalto.fi.chatapp.ChatFragment.ChatFragment;
import mcc.aalto.fi.chatapp.MainActivity;
import mcc.aalto.fi.chatapp.Modals;
import mcc.aalto.fi.chatapp.Models.Chat;
import mcc.aalto.fi.chatapp.Models.User;
import mcc.aalto.fi.chatapp.R;
import mcc.aalto.fi.chatapp.ViewModels.ChatViewModel;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class UserListFragment extends Fragment {
    ListView userList;
    FloatingActionButton addUsersButton;
    String userId;
    String query = "";
    UserListAdapter userListAdapter;

    Set<User> users = new HashSet<>();

    String chatId;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getActivity().setTitle("Create a new chat");

        return inflater.inflate(R.layout.fragment_user_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getArguments() != null) {
            this.chatId = (String) getArguments().get("chatId");
        }

        userId = ((MainActivity) getActivity()).getUserId();

        EditText searchText = view.findViewById(R.id.userSearchText);
        userList = view.findViewById(R.id.userResultList);
        addUsersButton = view.findViewById(R.id.startNewChatButton);

        addUsersButton.setOnClickListener(view1 -> {
            if (userListAdapter.getSelectedUsers().size() <= 0) {
                Toast.makeText(getActivity(), getString(R.string.userlist_user_not_selected), Toast.LENGTH_SHORT).show();
            } else if (this.chatId == null) {
                createChat((chatId, name) -> addUsers(userListAdapter.getSelectedUsers(), chatId, name));
            } else {
                addUsers(userListAdapter.getSelectedUsers(), this.chatId, null);
            }
        });

        // only run search query after user has stopped typing for a set time (400 ms currently)
        RxTextView.textChanges(searchText)
                .debounce(getResources().getInteger(R.integer.userlist_search_delay_ms), TimeUnit.MILLISECONDS)
                .subscribe(query -> {
                    this.query = query.toString();
                    getActivity().runOnUiThread(this::updateSearch);
                });

        ChatViewModel chatViewModel = ((MainActivity) getActivity()).getChatViewModel();
        chatViewModel.getUsers().observe(this, users -> {
            this.users = new HashSet<>(users);
            updateSearch();
        });
        chatViewModel.loadUsers();

    }


    private void createChat(BiConsumer<String, String> callback) {
        Modals.showPrompt(getActivity(),
                getContext().getResources().getText(R.string.userlist_prompt_chat_name).toString(),
                getContext().getResources().getText(R.string.userlist_prompt_create).toString(),
                (name) -> createChat(name, callback));
    }

    private void createChat(String name, BiConsumer<String, String> callback) {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();

        Long currentTimestamp = System.currentTimeMillis();

        // add self to chat immediately to ensure the creator can add users
        HashMap userMap = new HashMap() {{
            put(userId, currentTimestamp);
        }};
        Chat chat = new Chat(currentTimestamp, name, userMap);

        String chatId = database.child("chats").push().getKey();
        database.child("users").child("private").child(userId).child("chats").child(chatId).setValue(true);

        database.child("chats").child(chatId).setValue(chat)
                .addOnSuccessListener(e -> callback.accept(chatId, name));
    }

    private void updateSearch() {
        if (query.length() > 1) {
            setAdapter(users.stream().filter(el -> {
                if (el.displayName == null) {
                    return false;
                }

                return el.displayName.toLowerCase().contains(query.toLowerCase());
            })
                    .limit(getResources().getInteger(R.integer.userlist_max_search_results)).collect(Collectors.toList()));
        } else {
            setAdapter(new ArrayList<>());
        }

    }

    private void setAdapter(List<User> users) {
        if (userListAdapter == null) {
            userListAdapter = new UserListAdapter(this.getActivity(), users);
        } else {
            userListAdapter = userListAdapter.createFrom(users);
        }
        userList.setAdapter(userListAdapter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

         // Remove the action bar back arrow when navigating back to all chats fragment
        if(getFragmentManager().getBackStackEntryCount() <= 0) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getActivity().setTitle("ChatApp");
        }
    }


    private void addUsers(Set<User> userSet, String chatId, String name) {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();

        DatabaseReference membersRef = database.child("chats").child(chatId).child("members");
        DatabaseReference userRef = database.child("users").child("private");
        for (User user : userSet) {
            Long currentTimestamp = System.currentTimeMillis();
            String message = getResources().getString(R.string.userlist_new_user_added, user.displayName);
            membersRef.child(user.id).setValue(currentTimestamp, (databaseError, databaseReference) -> {
                if (databaseError != null) {
                    Log.d("ERROR", databaseError.getMessage());
                }

                userRef.child(user.id).child("chats").child(chatId).setValue(true);

                if (!user.isSelf()) {
                    ChatFragment.sendMessage(chatId, userId, message);
                }
            });
        }

        if (userSet.size() == 1) {
            name = userSet.iterator().next().displayName;
        }

        FragmentActivity act = getActivity();
        act.getSupportFragmentManager().popBackStack();
        Log.d("CHATNAME", name);
        ChatFragment.openChat(act, chatId, name);
    }

}