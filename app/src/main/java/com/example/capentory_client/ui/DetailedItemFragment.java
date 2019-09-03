package com.example.capentory_client.ui;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.capentory_client.R;
import com.example.capentory_client.models.MergedItemField;
import com.example.capentory_client.ui.errorhandling.BasicNetworkErrorHandler;
import com.example.capentory_client.viewmodels.DetailItemFragmentViewModel;
import com.example.capentory_client.viewmodels.ViewModelProviderFactory;
import com.example.capentory_client.viewmodels.sharedviewmodels.ItemxDetailSharedViewModel;
import com.example.capentory_client.viewmodels.wrappers.StatusAwareData;
import com.google.android.material.textfield.TextInputLayout;

import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import dagger.android.support.DaggerFragment;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * create an instance of this fragment.
 */
public class DetailedItemFragment extends DaggerFragment {
    private ItemxDetailSharedViewModel itemxDetailSharedViewModel;

    public DetailedItemFragment() {
        // Required empty public constructor
    }

    @Inject
    ViewModelProviderFactory providerFactory;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_item_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        DetailItemFragmentViewModel detailItemFragmentViewModel = ViewModelProviders.of(this, providerFactory).get(DetailItemFragmentViewModel.class);
        BasicNetworkErrorHandler basicNetworkErrorHandler = new BasicNetworkErrorHandler(getContext(), view.findViewById(R.id.dummy));


        detailItemFragmentViewModel.fetchForm();
        detailItemFragmentViewModel.getFields().observe(getViewLifecycleOwner(), fields -> {

            switch (fields.getStatus()) {

                case SUCCESS:
                    displayForm(view, fields.getData());
                    break;
                case ERROR:
                    basicNetworkErrorHandler.displayTextViewMessage(fields.getError());
                    break;
                case FETCHING:
                    //displayProgressbarAndHideContent();
                    break;

            }
            if (fields.getStatus() != StatusAwareData.State.ERROR)
                basicNetworkErrorHandler.reset();
        });


        itemxDetailSharedViewModel = ViewModelProviders.of(Objects.requireNonNull(getActivity())).get(ItemxDetailSharedViewModel.class);

        itemxDetailSharedViewModel.getCurrentItem().observe(getViewLifecycleOwner(), mergedItem -> {
            TextView txt = view.findViewById(R.id.dummy);
            if (mergedItem == null) {
                txt.setText("Server gib Formular");
            } else {
                //txt.setText(mergedItem.getMergedItemJSONPayload().toString());
            }
        });

        ImageButton validateButton = view.findViewById(R.id.validate_btn_fragment_itemdetail);
        validateButton.setOnClickListener(v -> handleValidate());
    }

    private void displayForm(View view, List<MergedItemField> fields) {
        LinearLayout linearLayout = view.findViewById(R.id.linearLayout_fragment_itemdetail);
        for (MergedItemField field : fields) {
            TextInputLayout textInputLayout = new TextInputLayout(view.getContext());
            textInputLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

            EditText myEditText = new EditText(getContext()); // Pass it an Activity or Context
            myEditText.setLayoutParams(new LinearLayout.LayoutParams(100, 40));
            myEditText.setText("eeeeeeeee");

            //myEditText.setId(field.getKey());

            textInputLayout.addView(myEditText);

            TextView textView = new TextView(view.getContext());
            textView.setText("rjhhehhhhhhhh");

            linearLayout.addView(textInputLayout);
            linearLayout.addView(textView);

        }
    }

    public void handleValidate() {
        itemxDetailSharedViewModel.setCurrentItemValidated(true);
        NavHostFragment.findNavController(this).popBackStack();
    }
}
