package mcc.aalto.fi.chatapp.ViewModels;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

// A factory class is required to be able to pass params to the view model
public class UserViewModelFactory implements ViewModelProvider.Factory {
	private String mParam;

	public UserViewModelFactory(String param) {
		mParam = param;
	}

	@Override
	public <T extends ViewModel> T create(Class<T> modelClass) {
		return (T) new UserViewModel(mParam);
	}
}

