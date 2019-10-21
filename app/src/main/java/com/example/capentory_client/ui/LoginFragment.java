package com.example.capentory_client.ui;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
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
import com.example.capentory_client.ui.errorhandling.BasicNetworkErrorHandler;
import com.example.capentory_client.viewmodels.LoginFragmentViewModel;
import com.example.capentory_client.viewmodels.ViewModelProviderFactory;
import com.example.capentory_client.viewmodels.wrappers.StatusAwareData;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Objects;

import javax.inject.Inject;

import static android.content.Context.MODE_PRIVATE;


/**
 * A simple {@link Fragment} subclass.
 */
public class LoginFragment extends NetworkFragment<String> {

    @Inject
    ViewModelProviderFactory providerFactory;

    public LoginFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.e("XXXX", String.valueOf(PreferenceUtility.isLoggedIn(getContext())));

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
        NavHostFragment.findNavController(this).popBackStack();
    }


    public void clearOnServer() {
        ((LoginFragmentViewModel) (networkViewModel)).logout();

        ((LoginFragmentViewModel) (networkViewModel)).getLogoutSuccessful().observe(getViewLifecycleOwner(), booleanStatusAwareData -> {

            switch (booleanStatusAwareData.getStatus()) {
                case SUCCESS:
                    clearLocally();
                    break;
                case ERROR:
                    handleError(booleanStatusAwareData.getError());
                    break;
                case FETCHING:
                    handleFetching();
                    break;
            }
            if (booleanStatusAwareData.getStatus() != StatusAwareData.State.ERROR)
                basicNetworkErrorHandler.reset();
        });
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        init(ViewModelProviders.of(this, providerFactory).get(LoginFragmentViewModel.class),
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


        //Move this to viewmodel
        Cryptography cryptography = new Cryptography(getContext());
        SharedPreferences.Editor editor = Objects.requireNonNull(getContext()).getSharedPreferences(PreferenceUtility.LOG_SERVER, MODE_PRIVATE).edit();
        editor.putString("api_tоkеn", cryptography.encrypt(statusAwareData.getData()));
        editor.putBoolean("logged_in", true);
        editor.apply();

        ToastUtility.displayCenteredToastMessage(getContext(), "Token erfolgreich generiert!", Toast.LENGTH_LONG);
        DisplayUtility.displayLoggedInMenu(getActivity());
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
