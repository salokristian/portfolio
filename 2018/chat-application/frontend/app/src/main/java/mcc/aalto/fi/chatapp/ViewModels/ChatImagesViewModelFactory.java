package mcc.aalto.fi.chatapp.ViewModels;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

// A factory class is required to be able to pass params to the view model
public class ChatImagesViewModelFactory implements ViewModelProvider.Factory {
	private String chatId;
	private Long userJoinedTimestamp;

	public ChatImagesViewModelFactory(String chatId, Long userJoinedTimestamp) {
		this.chatId = chatId;
		this.userJoinedTimestamp = userJoinedTimestamp;
	}

	@Override
	public <T extends ViewModel> T create(Class<T> modelClass) {
		return (T) new ChatImagesViewModel(chatId, userJoinedTimestamp);
	}
}
