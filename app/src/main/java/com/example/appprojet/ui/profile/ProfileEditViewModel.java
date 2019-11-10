package com.example.appprojet.ui.profile;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.appprojet.R;
import com.example.appprojet.models.User;
import com.example.appprojet.repositories.FirebaseAuthenticationRepository;
import com.example.appprojet.repositories.IAuthenticationRepository;
import com.example.appprojet.utils.Callback;
import com.example.appprojet.utils.CallbackException;
import com.example.appprojet.utils.form_data_with_validators.EmailValidator;
import com.example.appprojet.utils.form_data_with_validators.FormData;
import com.example.appprojet.utils.form_data_with_validators.NameValidator;
import com.example.appprojet.utils.form_data_with_validators.PasswordConfirmationValidator;
import com.example.appprojet.utils.form_data_with_validators.PasswordValidator;


/**
 * View model to handle process of ProfileViewFragment to update profile.
 *
 * There are one FormData for each field and on is loading state Live data for each form, there are
 * 3 forms :
 *  - email
 *  - username
 *  - password
 *
 * The fragment ask the repo to perform actions and update Live Data according the responses.
 */
public class ProfileEditViewModel extends AndroidViewModel {

    private final IAuthenticationRepository authRepo;

    final MutableLiveData<String> errorLive = new MutableLiveData<>(null);
    final MutableLiveData<String> successLive = new MutableLiveData<>(null);

    final FormData emailFormData = new FormData(new EmailValidator());
    final MutableLiveData<Boolean> emailFormIsLoading = new MutableLiveData<>(false);

    final FormData newPasswordFormData = new FormData(new PasswordValidator());
    final FormData newPasswordConfirmFormData = new FormData(new PasswordConfirmationValidator(newPasswordFormData));
    final MutableLiveData<Boolean> newPasswordFormIsLoading = new MutableLiveData<>(false);

    final FormData usernameFormData = new FormData(new NameValidator());
    final MutableLiveData<Boolean> usernameFormIsLoading = new MutableLiveData<>(false);

    final MutableLiveData<Boolean> isDeleted = new MutableLiveData<>();


    public ProfileEditViewModel(Application application) {
        super(application);
        authRepo = FirebaseAuthenticationRepository.getInstance();
        User user = authRepo.getCurrentUser();
        if (user != null) {
            emailFormData.setValue(user.getEmail());
            usernameFormData.setValue(user.getName());
        }
    }

    private void resetInfoLiveData() {
        errorLive.setValue(null);
        successLive.setValue(null);
    }

    void submitEmailForm() {
        resetInfoLiveData();
        if (emailFormData.isValid()) {
            emailFormIsLoading.setValue(true);
            authRepo.updateEmail(emailFormData.getValue(), new UpdateCallBack(emailFormIsLoading));
        }
    }

    void submitNewPasswordForm() {
        resetInfoLiveData();
        if (newPasswordFormData.isValid() && newPasswordConfirmFormData.isValid()) {
            newPasswordFormIsLoading.setValue(true);
            authRepo.updatePassword(newPasswordConfirmFormData.getValue(), new UpdateCallBack(newPasswordFormIsLoading));
        }
    }

    void submitUsernameForm() {
        resetInfoLiveData();
        if (usernameFormData.isValid()) {
            usernameFormIsLoading.setValue(true);
            authRepo.updateName(usernameFormData.getValue(), new UpdateCallBack(usernameFormIsLoading));
        }
    }

    void deleteAccount() {
        authRepo.deleteAccount(new Callback<Void>() {
            @Override
            public void onSucceed(Void result) {
                isDeleted.setValue(true);
            }

            @Override
            public void onFail(CallbackException exception) {
                errorLive.setValue(exception.getErrorMessage(getApplication()));
            }
        });
    }

    private class UpdateCallBack implements Callback<User> {

        final MutableLiveData<Boolean> loadingLiveData;

        UpdateCallBack(MutableLiveData<Boolean> loadingLiveData) {
            this.loadingLiveData = loadingLiveData;
        }

        @Override
        public void onSucceed(User result) {
            loadingLiveData.setValue(false);
            successLive.setValue(getApplication().getString(R.string.profile_updated_success));
        }

        @Override
        public void onFail(CallbackException exception) {
            loadingLiveData.setValue(false);
            errorLive.setValue(exception.getErrorMessage(getApplication()));
        }
    }
}
