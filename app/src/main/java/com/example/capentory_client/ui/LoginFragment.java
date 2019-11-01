package com.example.capentory_client.ui;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.fragment.NavHostFragment;

import com.example.capentory_client.R;
import com.example.capentory_client.androidutility.Cryptography;
import com.example.capentory_client.androidutility.DisplayUtility;
import com.example.capentory_client.androidutility.PreferenceUtility;
import com.example.capentory_client.androidutility.ToastUtility;
import com.example.capentory_client.repos.LoginRepository;
import com.example.capentory_client.ui.errorhandling.BasicNetworkErrorHandler;
import com.example.capentory_client.viewmodels.LoginViewModel;
import com.example.capentory_client.viewmodels.ViewModelProviderFactory;
import com.example.capentory_client.viewmodels.wrappers.StatusAwareData;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Objects;

import javax.inject.Inject;


/**
 * A simple {@link Fragment} subclass.
 */
public class LoginFragment extends NetworkFragment<String, LoginRepository, LoginViewModel> {

    @Inject
    ViewModelProviderFactory providerFactory;

    public LoginFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (PreferenceUtility.isLoggedIn(getContext())) {
            return inflater.inflate(R.layout.fragment_login_logged_in, container, false);

        }
        return inflater.inflate(R.layout.fragment_login, container, false);

    }

    public void clearLocally() {
        Cryptography cryptography = new Cryptography(getContext());
        try {
            cryptography.removeKeys();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ToastUtility.displayCenteredToastMessage(getContext(), "Abmeldung erfolgreich!", Toast.LENGTH_LONG);
        PreferenceUtility.logout(getContext());
        DisplayUtility.displayLoggedOutMenu(getActivity());
        DisplayUtility.hideKeyboard(Objects.requireNonNull(getActivity()));
        NavHostFragment.findNavController(this).popBackStack();
    }


    public void clearOnServer() {
        // already logged out when this happens????

        if (PreferenceUtility.isLoggedIn(getContext()))
            networkViewModel.logout();

        observeSpecificLiveData(networkViewModel.getLogoutSuccessful(), liveData -> clearLocally());
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        initWithoutFetch(ViewModelProviders.of(this, providerFactory).get(LoginViewModel.class),
                new BasicNetworkErrorHandler(getContext(), view.findViewById(R.id.subtitle_text_view_fragment_login)),
                view,
                R.id.progress_bar_fragment_login);


        if (PreferenceUtility.isLoggedIn(getContext())) {
            view.findViewById(R.id.logout_locally_btn_fragment_login).setOnClickListener(v -> clearLocally());
            view.findViewById(R.id.logout_server_btn_fragment_login).setOnClickListener(v -> clearOnServer());
        } else {
            TextView userField = view.findViewById(R.id.user_fragment_login);
            TextView passField = view.findViewById(R.id.password_fragment_login);

            view.findViewById(R.id.login_btn_fragment_login).setOnClickListener(v -> {
                String userString = userField.getText().toString();
                String passwordString = passField.getText().toString();
                fetchManually(userString, passwordString);
            });
        }


    }


    @Override
    protected void handleSuccess(StatusAwareData<String> statusAwareData) {
        super.handleSuccess(statusAwareData);

        PreferenceUtility.login(getContext(), statusAwareData.getData());
        ToastUtility.displayCenteredToastMessage(getContext(), "Token erfolgreich generiert!", Toast.LENGTH_LONG);
        DisplayUtility.displayLoggedInMenu(getActivity());
        DisplayUtility.hideKeyboard(Objects.requireNonNull(getActivity()));
        NavHostFragment.findNavController(this).popBackStack();
    }


    @Override
    protected void displayProgressbarAndHideContent() {
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    protected void hideProgressBarAndShowContent() {
        progressBar.setVisibility(View.GONE);
    }

    @Override
    protected void hideProgressBarAndHideContent() {
        progressBar.setVisibility(View.GONE);
    }

}
