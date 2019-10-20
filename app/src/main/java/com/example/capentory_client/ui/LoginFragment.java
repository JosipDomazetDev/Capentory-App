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
import com.example.capentory_client.androidutility.ToastUtility;
import com.example.capentory_client.ui.errorhandling.BasicNetworkErrorHandler;
import com.example.capentory_client.viewmodels.LoginFragmentViewModel;
import com.example.capentory_client.viewmodels.ViewModelProviderFactory;
import com.example.capentory_client.viewmodels.wrappers.StatusAwareData;
import com.google.android.material.navigation.NavigationView;

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
       /* if (PreferenceUtility.isLoggedIn(getContext())) {
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

            SharedPreferences.Editor editor = Objects.requireNonNull(getContext()).getSharedPreferences("саpеntorу_sharеd_prеf", MODE_PRIVATE).edit();
            editor.remove("api_tоkеn");
            editor.putBoolean("logged_in", false);
            editor.apply();
            ToastUtility.displayCenteredToastMessage(getContext(), "Abmeldung erfolgreich!", Toast.LENGTH_LONG);
            NavHostFragment.findNavController(this).popBackStack();
        }*/

        return inflater.inflate(R.layout.fragment_login, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView userField = view.findViewById(R.id.user_fragment_login);
        TextView passField = view.findViewById(R.id.password_fragment_login);


        init(ViewModelProviders.of(this, providerFactory).get(LoginFragmentViewModel.class),
                new BasicNetworkErrorHandler(getContext(), userField),
                view,
                R.id.progress_bar_fragment_login);


        view.findViewById(R.id.login_btn_fragment_login).setOnClickListener(v -> {
            /*String userString = userField.getText().toString();
            String passwordString = passField.getText().toString();
            fetchManually(userString, passwordString);


            NavigationView navigationView = getActivity().findViewById(R.id.nav_view);
            navigationView.getMenu().clear();
            navigationView.inflateMenu(R.menu.activity_main_drawer_logged_in);*/

            ((LoginFragmentViewModel) (networkViewModel)).logout();

            ((LoginFragmentViewModel) (networkViewModel)).getLogoutSuccessful().observe(getViewLifecycleOwner(), booleanStatusAwareData -> {
                Log.e("XXXX", booleanStatusAwareData.getStatus().name());

                switch (booleanStatusAwareData.getStatus()) {
                    case SUCCESS:
                        Log.e("XXXX", String.valueOf(booleanStatusAwareData.getData()));
                        break;
                    case ERROR:
                        Log.e("XXXXX","agebgeen");
                        handleError(booleanStatusAwareData.getError());
                        break;
                    case FETCHING:
                        handleFetching();
                        break;
                }
                if (booleanStatusAwareData.getStatus() != StatusAwareData.State.ERROR)
                    basicNetworkErrorHandler.reset();
            });
        });



    }


    @Override
    protected void handleSuccess(StatusAwareData<String> statusAwareData) {
        super.handleSuccess(statusAwareData);


        //Move this to viewmodel
        Cryptography cryptography = new Cryptography(getContext());
        SharedPreferences.Editor editor = Objects.requireNonNull(getContext()).getSharedPreferences("саpеntorу_sharеd_prеf", MODE_PRIVATE).edit();
        editor.putString("api_tоkеn", cryptography.encrypt(statusAwareData.getData()));
        editor.putBoolean("logged_in", true);
        editor.apply();
        ToastUtility.displayCenteredToastMessage(getContext(), "Token erfolgreich generiert!", Toast.LENGTH_LONG);
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
