package mcc.aalto.fi.chatapp.ChatListFragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;

import mcc.aalto.fi.chatapp.ChatFragment.ChatFragment;
import mcc.aalto.fi.chatapp.MainActivity;
import mcc.aalto.fi.chatapp.Models.Chat;
import mcc.aalto.fi.chatapp.R;
import mcc.aalto.fi.chatapp.UserListFragment.UserListFragment;
import mcc.aalto.fi.chatapp.ViewModels.ChatViewModel;

public class ChatListFragment extends Fragment {
	ListView chatListView;
	FloatingActionButton newChatButton;
	String userId;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		// Maintain this fragment across orientation changes
		setRetainInstance(true);

		return inflater.inflate(R.layout.fragment_chat_list, container, false);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		chatListView = view.findViewById(R.id.chatListView);
		newChatButton = view.findViewById(R.id.newChatButton);
		userId = ((MainActivity) getActivity()).getUserId();

		ChatViewModel chatViewModel = ((MainActivity) getActivity()).getChatViewModel();
		chatViewModel.getChats().observe(this, chats -> {
			ChatAdapter adapter = new ChatAdapter(this.getContext(), chats, userId);
			chatListView.setAdapter(adapter);
		});


		chatListView.setOnItemClickListener((adapterView, view1, i, l) -> {
			Chat chat = ((Chat) chatListView.getAdapter().getItem(i));
			ChatFragment.openChat(getActivity(), chat.id, ChatFragment.getTitle(chat, userId));
		});

		newChatButton.setOnClickListener(v -> {
			Fragment userListFragment = new UserListFragment();

			android.support.v4.app.FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
			fragmentTransaction.add(R.id.fragmentContainer, userListFragment).addToBackStack(null).commit();
		});
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		// Remove the action bar back arrow when navigating back to all chats fragment
		((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
		getActivity().setTitle("ChatApp");
	}

}