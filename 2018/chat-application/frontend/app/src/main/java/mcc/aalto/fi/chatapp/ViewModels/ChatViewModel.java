package mcc.aalto.fi.chatapp.ViewModels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import mcc.aalto.fi.chatapp.Models.Chat;
import mcc.aalto.fi.chatapp.Models.Message;
import mcc.aalto.fi.chatapp.Models.User;


/*
A ViewModel that fetching all of a user's chats along with the user profiles of the users in those
chats, and all messages of those chats. The data is fetched using event listeners, and should
therefore be up-to-date because Firebase notifies us of updates in the data.

The basic flow of fetching data is:

1. Get all chatIds of chats the user is currently in.
2. Get data of all user's chats
3. Get messages for user's chats
4. Fetch user profiles for users in user's chats
 */
public class ChatViewModel extends ViewModel {
	static final public Integer MESSAGE_PAGE_SIZE = 50;

	private String userId;
	private DatabaseReference database;
	private HashMap<String, MutableLiveData<List<Message>>> messages;
	private HashMap<String, Integer> newMessageCount;
	private HashMap<String, Boolean> hasMoreMessages;
	private HashMap<String, Boolean> fetchingOldMessages;
	private MutableLiveData<List<Chat>> chats;
	private MutableLiveData<List<User>> users;

	public ChatViewModel(String userId) {
		database = FirebaseDatabase.getInstance().getReference();

		this.userId = userId;

		messages = new HashMap<>();
		hasMoreMessages = new HashMap<>();
		fetchingOldMessages = new HashMap<>();
		newMessageCount = new HashMap<>();
		chats = new MutableLiveData<>();
		users = new MutableLiveData<>();

		chats.setValue(new ArrayList<>());
		users.setValue(new ArrayList<>());

		loadChatIds();
	}


	public LiveData<List<Message>> getMessages(String chatId) {
		if (messages.containsKey(chatId)) {
			return messages.get(chatId);
		} else {
			initializeMessageList(chatId);

			return messages.get(chatId);
		}
	}

	public LiveData<List<Chat>> getChats() {
		return chats;
	}
	public LiveData<List<User>> getUsers() {
		return users;
	}

	public Chat getChat(String chatId) {
		List<Chat> chats = this.chats.getValue();

		return chats.stream()
				.filter(chat -> chat.id.equals(chatId))
				.findFirst()
				.orElse(new Chat());
	}

	public Long getUserJoinedTimestamp(String chatId) {
		List<Chat> allChats = chats.getValue();

		Chat matchingChat = allChats.stream()
				.filter(chat -> chat.id.equals(chatId))
				.findFirst()
				.orElse(null);

		if (matchingChat == null) {
			return 0L;
		} else {
			return matchingChat.members.get(userId);
		}
	}

	public int getNewMessageCount(String chatId) {
		return newMessageCount.get(chatId);
	}

	public void fetchMoreMessages(String chatId) {

		if (hasMoreMessages.get(chatId) && !fetchingOldMessages.get(chatId)) {

			ValueEventListener moreMessagesListener = new ValueEventListener() {
				@Override
				public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
					List<Message> allMessages = messages.get(chatId).getValue();
					Integer messageCount = 0;

					for (DataSnapshot messageSnapshot: dataSnapshot.getChildren()) {

						Message message = messageSnapshot.getValue(Message.class);
						message.senderData = new User("Unknown user");
						getChatUserData(chatId, message.sender, (user) -> message.senderData = user);
						message.id = messageSnapshot.getKey();

						if (messageSentBeforeUserJoinedChat(message, chatId)) {
							hasMoreMessages.put(chatId, false);

						} else if (!messageExists(allMessages, message.id)) {
							allMessages.add(message);
							Collections.sort(allMessages);
							messageCount++;
						}
					}

					if (dataSnapshot.getChildrenCount() <= MESSAGE_PAGE_SIZE) {
						hasMoreMessages.put(chatId, false);
					}

					fetchingOldMessages.put(chatId, false);
					newMessageCount.put(chatId, messageCount);
					messages.get(chatId).setValue(allMessages);
				}

				@Override
				public void onCancelled(@NonNull DatabaseError databaseError) {
					Log.d("CHAT_VIEW_MODEL", "Fetch more items error: ", databaseError.toException());
					fetchingOldMessages.put(chatId, false);
				}
			};

			// Disable further fetching until all messages in this page are fetched
			fetchingOldMessages.put(chatId, true);

			// MESSAGE_PAGE_SIZE + 1 is used here because endAt includes the message with the given id
			database.child("messages").child(chatId).orderByKey().limitToLast(MESSAGE_PAGE_SIZE + 1)
				.endAt(getOldestFetchedMessageId(chatId))
				.addListenerForSingleValueEvent(moreMessagesListener);
		}
	}

	private void loadMessages(String chatId) {
		// If this is the first time loading chat messages, initialize the message data structures
		if (!messages.containsKey(chatId)) {
			initializeMessageList(chatId);
		}
		if (!hasMoreMessages.containsKey(chatId)) {
			hasMoreMessages.put(chatId, true);
		}
		if (!fetchingOldMessages.containsKey(chatId)) {
			fetchingOldMessages.put(chatId, false);
		}

		ChildEventListener messageEventListener = new ChildEventListener() {
			@Override
			public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
				addMessage(chatId, dataSnapshot);
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
				Log.w("CHAT_VIEW_MODEL","CANCEL", databaseError.toException());
			}

			@Override
			public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
				Log.d("CHAT_VIEW_MODEL", "CHANGE " + dataSnapshot.getKey());
			}

			@Override
			public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
				Log.d("CHAT_VIEW_MODEL", "REMOVE " + dataSnapshot.getKey());
			}

			@Override
			public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
				Log.d("CHAT_VIEW_MODEL", "MOVE " + dataSnapshot.getKey());
			}
		};

		database.child("messages").child(chatId).orderByKey().limitToLast(MESSAGE_PAGE_SIZE)
				.addChildEventListener(messageEventListener);
	}

	public void loadUsers() {
		ChildEventListener usersEventListener = new ChildEventListener() {
			@Override
			public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
				List<User> allUsers = users.getValue();

				Log.d("DATA", dataSnapshot.toString());
				User newUser = dataSnapshot.getValue(User.class);
				newUser.id = dataSnapshot.getKey();

				allUsers.add(newUser);
				users.setValue(allUsers);
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
				Log.w("CHAT_VIEW_MODEL", "loadChatIds error: ", databaseError.toException());
			}

			@Override
			public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
				Log.d("CHAT_VIEW_MODEL", "loadChatIds child changed: " + dataSnapshot.getKey());
			}

			@Override
			public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
				Log.d("CHAT_VIEW_MODEL", "loadChatIds child removed: " + dataSnapshot.getKey());
			}

			@Override
			public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
				Log.d("CHAT_VIEW_MODEL", "loadChatIds child moved: " + dataSnapshot.getKey());
			}
		};

		database.child("users").child("public")
				.addChildEventListener(usersEventListener);
	}

	private void loadChatIds() {
		ChildEventListener chatIdsEventListener = new ChildEventListener() {
			@Override
			public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
				String newChatId = dataSnapshot.getKey();

				loadChat(newChatId);
				loadMessages(newChatId);
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
				Log.e("CHAT_VIEW_MODEL", "loadChatIds error: ", databaseError.toException());
			}

			@Override
			public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
				chats.setValue(chats.getValue().stream().map(el -> {
					if (el.id.equals(dataSnapshot.getKey())) {
						return getChat(dataSnapshot.getKey());
					}
					return el;
				}).collect(Collectors.toList()));
			}

			@Override
			public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
				chats.setValue(chats.getValue().stream().filter(el -> !el.id.equals(dataSnapshot.getKey())).collect(Collectors.toList()));
			}

			@Override
			public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }
		};

		database.child("users").child("private").child(userId).child("chats")
				.addChildEventListener(chatIdsEventListener);
	}

	private void loadChat(String chatId) {
		ValueEventListener chatEventListener = new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				List<Chat> allChats = chats.getValue();

				Chat newChat = dataSnapshot.getValue(Chat.class);
				newChat.id = dataSnapshot.getKey();

				// If the chat is updated, i.e. it already exists, replace the existing data with the new data
				Integer existingChatIndex = IntStream.range(0, allChats.size())
						.filter(i -> newChat.id.equals(allChats.get(i).id))
						.findFirst()
						.orElse(-1);

				if (existingChatIndex == -1) {
					allChats.add(newChat);
				} else {
					allChats.set(existingChatIndex, newChat);
				}

				chats.setValue(allChats);

				loadUsers(newChat.members.keySet(), newChat.id);
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
				Log.d("CHAT_VIEW_MODEL", "loadChat cancelled", databaseError.toException());
			}
		};

		database.child("chats").child(chatId).addValueEventListener(chatEventListener);
	}

	private void loadUsers(Set<String> userIds, String chatId) {
		for (String userId : userIds) {
			ValueEventListener fetchUserProfile = new ValueEventListener() {
				@Override
				public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

					User user = dataSnapshot.getValue(User.class);

					// TODO: This can be removed once the database data is entirely valid, i.e. all logged in users are under "users"
					if (user != null) {
						user.id = userId;
						addUserProfileToChatData(user, chatId);
						addUserProfileToChatMessages(user, chatId);
					}
				}

				@Override
				public void onCancelled(@NonNull DatabaseError databaseError) {
					Log.d("CHAT_VIEW_MODEL", "loadUsers cancelled", databaseError.toException());
				}
			};

			database.child("users").child("public").child(userId)
					.addListenerForSingleValueEvent(fetchUserProfile);
		}
	}

	private void getChatUserData(String chatId, String userId, Consumer<User> callback) {
		List<Chat> chats = this.chats.getValue();

		Chat matchingChat = chats.stream()
				.filter(chat -> chat.id.equals(chatId))
				.findAny()
				.orElse(null);

		if (matchingChat != null) {
			User u = matchingChat.memberProfiles.stream()
					.filter(member -> member.id.equals(userId))
					.findAny()
					.orElse(null);
			if (u != null) {
				callback.accept(u);
			}

			database.child("users").child("public").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
				@Override
				public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
					callback.accept(dataSnapshot.getValue(User.class));
				}

				@Override
				public void onCancelled(@NonNull DatabaseError databaseError) { }
			});


		}
	}

	private void addUserProfileToChatData(User user, String chatId) {
		List<Chat> allChats = chats.getValue();

		Chat matchingChat = allChats.stream()
				.filter(chat -> chat.id.equals(chatId))
				.findFirst()
				.orElse(null);

		if (matchingChat != null) {
			Integer chatIndex = allChats.indexOf(matchingChat);
			allChats.get(chatIndex).memberProfiles.add(user);

			chats.setValue(allChats);
		}
	}

	private void addUserProfileToChatMessages(User user, String chatId) {
		if (!this.messages.containsKey(chatId)) {
			return;
		}

		List<Message> messages = this.messages.get(chatId).getValue();

		for (Message message : messages) {
			if (message.senderData == null && message.sender.equals(user.id)) {
				message.senderData = user;
			}
		}
	}

	private String getOldestFetchedMessageId(String chatId) {
		List<Message> messages = this.messages.get(chatId).getValue();
		return messages.isEmpty() ? "a": messages.get(0).id;
	}

	private void initializeMessageList(String chatId) {
		MutableLiveData<List<Message>> newMessageList = new MutableLiveData<>();
		newMessageList.setValue(new ArrayList<>());

		messages.put(chatId, newMessageList);
	}

	private void addMessage(String chatId, DataSnapshot messageSnapshot) {
		List<Message> allMessages = messages.get(chatId).getValue();

		Message message = messageSnapshot.getValue(Message.class);
		message.senderData = new User("Unknown user");
		getChatUserData(chatId, message.sender, (user) -> message.senderData = user);
		message.id = messageSnapshot.getKey();

		if (messageSentBeforeUserJoinedChat(message, chatId)) {
			hasMoreMessages.put(chatId, false);
		}

		// Check that same messages aren't added twice, because the paging always includes one duplicate message
		else if (!messageExists(allMessages, message.id)) {
			allMessages.add(message);
			Collections.sort(allMessages);

			messages.get(chatId).setValue(allMessages);
			newMessageCount.put(chatId, 0);
		}
	}

	private boolean messageExists(List<Message> messages, String messageId) {
		return messages.stream()
				.anyMatch(message -> message.id.equals(messageId));
	}

	private boolean messageSentBeforeUserJoinedChat(Message message, String chatId) {
		List<Chat> allChats = chats.getValue();

		Chat matchingChat = allChats.stream()
				.filter(chat -> chat.id.equals(chatId))
				.findFirst()
				.orElse(null);

		if (matchingChat == null) {
			return false;
		}

		Long chatJoinTimestamp = matchingChat.members.get(userId);
		return chatJoinTimestamp > message.createdAt;
	}
}

