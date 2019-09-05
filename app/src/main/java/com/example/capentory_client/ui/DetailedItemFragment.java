package com.example.capentory_client.ui;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.fragment.NavHostFragment;

import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.example.capentory_client.R;
import com.example.capentory_client.models.MergedItemField;
import com.example.capentory_client.ui.errorhandling.BasicNetworkErrorHandler;
import com.example.capentory_client.viewmodels.DetailItemFragmentViewModel;
import com.example.capentory_client.viewmodels.ViewModelProviderFactory;
import com.example.capentory_client.viewmodels.sharedviewmodels.ItemxDetailSharedViewModel;
import com.example.capentory_client.viewmodels.wrappers.StatusAwareData;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

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
                    try {
                        displayForm(view, fields.getData());
                    } catch (JSONException e) {
                        basicNetworkErrorHandler.displayTextViewMessage(e);
                    }
                    break;
                case ERROR:
                    fields.getError().printStackTrace();
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

    private void displayForm(View view, Map<String, MergedItemField> mapFields) throws JSONException {
        JSONObject mergedItemJSONPayload = Objects.requireNonNull(itemxDetailSharedViewModel.getCurrentItem().getValue()).getMergedItemJSONPayload();
        Map<String, List<MergedItemField>> map = new HashMap<>();

        LinearLayout linearLayout = view.findViewById(R.id.linearLayout_fragment_itemdetail);
        Iterator<String> iterator = mergedItemJSONPayload.keys();

        while (iterator.hasNext()) {
            MergedItemField currentField = getCurrentField(iterator.next(), mapFields);

            // If the currentField is null this means that it wasn't included in the form request and therefore has no valid representation
            if (currentField == null) continue;
            String mapKeyCurrentFieldType = currentField.getType();

            if (map.containsKey(mapKeyCurrentFieldType)) {
                List<MergedItemField> fields = map.get(mapKeyCurrentFieldType);
                if (fields == null) continue;

                fields.add(currentField);
                map.put(mapKeyCurrentFieldType, fields);

            } else {
                List<MergedItemField> fields = new ArrayList<>();
                fields.add(currentField);
                map.put(mapKeyCurrentFieldType, fields);
            }
        }

        for (String mapKeyFieldType : new TreeMap<>(map).keySet()) {

            List<MergedItemField> fieldsForType = map.get(mapKeyFieldType);
            if (fieldsForType == null) continue;

            for (MergedItemField currentField : fieldsForType) {
                switch (currentField.getType().toLowerCase()) {
                    case "integer":
                        TextInputLayout textInputLayoutNumber = new TextInputLayout(view.getContext());
                        textInputLayoutNumber.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

                        TextInputEditText editNumber = new TextInputEditText(Objects.requireNonNull(getContext()));
                        editNumber.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        editNumber.setText(mergedItemJSONPayload.getString(currentField.getKey()));
                        editNumber.setHint(currentField.getLabel());
                        editNumber.setPadding(0, 0, 0, 30);
                        textInputLayoutNumber.addView(editNumber);

                        linearLayout.addView(textInputLayoutNumber);
                        break;

                    case "datetime":
                        //Textview
                        break;

                    case "string":
                        Log.e("eeeee", "eeeeee");
                        TextInputLayout textInputLayout = new TextInputLayout(view.getContext());
                        textInputLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

                        TextInputEditText editText = new TextInputEditText(Objects.requireNonNull(getContext()));
                        editText.setText(mergedItemJSONPayload.getString(currentField.getKey()));
                        editText.setHint(currentField.getLabel());
                        editText.setPadding(0, 0, 0, 30);
                        textInputLayout.addView(editText);
                        linearLayout.addView(textInputLayout);

                        break;

                    case "field":
                        //Field => z.b. fk
                        break;

                    case "boolean":
                        Switch switch_ = new Switch(getContext());
                        switch_.setChecked(mergedItemJSONPayload.getBoolean(currentField.getKey()));

                        switch_.setText(currentField.getLabel());
                        switch_.setPadding(0, 0, 0, 30);
                        linearLayout.addView(switch_);
                        break;

                    case "choice":
                        Spinner spinner = new Spinner(getContext());

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(Objects.requireNonNull(getContext()), R.layout.support_simple_spinner_dropdown_item, new String[]{"eee"});
                        spinner.setAdapter(adapter);
                        ArrayList<String> choices = getChoicesFromField(currentField.getChoices());

                        linearLayout.addView(spinner);
                        break;

                    default:
                        break;
                }
            }
        }


    }

    private ArrayList<String> getChoicesFromField(JSONArray choices) {
        return null;
    }

    private MergedItemField getCurrentField(String key, Map<String, MergedItemField> mapFields) throws JSONException {
        if (mapFields.containsKey(key)) {
            return mapFields.get(key);
        }
        return null;
    }


    public void handleValidate() {
        itemxDetailSharedViewModel.setCurrentItemValidated(true);
        NavHostFragment.findNavController(this).popBackStack();

        //itemxDetailSharedViewModel.setValidatedData();
    }
}
