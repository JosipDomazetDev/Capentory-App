package com.example.capentory_client.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.fragment.NavHostFragment;

import com.example.capentory_client.R;
import com.example.capentory_client.androidutility.ToastUtility;
import com.example.capentory_client.models.MergedItemField;
import com.example.capentory_client.repos.FormRepository;
import com.example.capentory_client.ui.errorhandling.BasicNetworkErrorHandler;
import com.example.capentory_client.viewmodels.DetailItemFragmentViewModel;
import com.example.capentory_client.viewmodels.NetworkViewModel;
import com.example.capentory_client.viewmodels.ViewModelProviderFactory;
import com.example.capentory_client.viewmodels.adapter.KeyValueDropDownAdapter;
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


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * create an instance of this fragment.
 */
public class DetailedItemFragment extends NetworkFragment<Map<String, MergedItemField>, FormRepository, DetailItemFragmentViewModel> {
    private ItemxDetailSharedViewModel itemxDetailSharedViewModel;
    private BasicNetworkErrorHandler basicNetworkErrorHandler;
    private View view;

    public DetailedItemFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_item_detail, container, false);
    }


    @Inject
    ViewModelProviderFactory providerFactory;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view = view;
        LinearLayout linearLayout = view.findViewById(R.id.content_fragment_item_detail);
        basicNetworkErrorHandler = new BasicNetworkErrorHandler(getContext(), view.findViewById(R.id.dummy));

        init(ViewModelProviders.of(this, providerFactory).get(DetailItemFragmentViewModel.class),
                basicNetworkErrorHandler,
                view,
                R.id.progress_bar_fragment_item_detail,
                linearLayout,
                R.id.swipe_refresh_fragment_item_detail);


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


    @Override
    protected void handleSuccess(StatusAwareData<Map<String, MergedItemField>> statusAwareData) {
        super.handleSuccess(statusAwareData);
        try {
            displayForm(statusAwareData.getData(), view);
        } catch (JSONException e) {
            basicNetworkErrorHandler.displayTextViewMessage(e);
        }
    }


    private void displayForm(Map<String, MergedItemField> mapFields, View view) throws JSONException {
        JSONObject mergedItemJSONPayload = Objects.requireNonNull(itemxDetailSharedViewModel.getCurrentItem().getValue()).getMergedItemJSONPayload();
        Map<String, List<MergedItemField>> map = new HashMap<>();

        LinearLayout linearLayout = view.findViewById(R.id.linearLayout_fragment_itemdetail);
        linearLayout.removeAllViews();
        Iterator<String> iterator = mergedItemJSONPayload.keys();

        while (iterator.hasNext()) {
            MergedItemField currentField = getCurrentFieldFromKey(iterator.next(), mapFields);

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

        for (String mapKeyFieldType : new TreeMap<>(map).descendingKeySet()) {

            List<MergedItemField> fieldsForType = map.get(mapKeyFieldType);
            if (fieldsForType == null) continue;

            for (MergedItemField currentField : fieldsForType) {
                if (currentField.isReadOnly()) continue;

                switch (currentField.getType().toLowerCase()) {
                    case "integer":
                        TextInputLayout textInputLayoutNumber = new TextInputLayout(view.getContext());
                        textInputLayoutNumber.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                        textInputLayoutNumber.setPadding(0, 0, 0, 40);

                        TextInputEditText editNumber = new TextInputEditText(Objects.requireNonNull(getContext()));
                        editNumber.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_NUMBER);
                        editNumber.setText(mergedItemJSONPayload.getString(currentField.getKey()));
                        editNumber.setHint(currentField.getLabel());
                        textInputLayoutNumber.addView(editNumber);

                        linearLayout.addView(textInputLayoutNumber);
                        break;

                    case "datetime":
                        TextInputLayout textInputLayoutDate = new TextInputLayout(view.getContext());
                        textInputLayoutDate.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                        textInputLayoutDate.setPadding(0, 0, 0, 40);

                        TextInputEditText editDate = new TextInputEditText(Objects.requireNonNull(getContext()));
                        editDate.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_DATETIME);
                        editDate.setText(mergedItemJSONPayload.getString(currentField.getKey()));
                        editDate.setHint(currentField.getLabel());
                        textInputLayoutDate.addView(editDate);

                        linearLayout.addView(textInputLayoutDate);
                        break;

                    case "string":
                        TextInputLayout textInputLayout = new TextInputLayout(view.getContext());
                        textInputLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                        textInputLayout.setPadding(0, 0, 0, 40);

                        TextInputEditText editText = new TextInputEditText(Objects.requireNonNull(getContext()));
                        editText.setText(mergedItemJSONPayload.getString(currentField.getKey()));
                        editText.setHint(currentField.getLabel());
                        textInputLayout.addView(editText);
                        linearLayout.addView(textInputLayout);

                        break;

                    case "field":
                        TextInputLayout textInputLayoutField = new TextInputLayout(view.getContext());
                        textInputLayoutField.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                        textInputLayoutField.setPadding(0, 0, 0, 40);

                        TextInputEditText editTextField = new TextInputEditText(Objects.requireNonNull(getContext()));
                        editTextField.setText(mergedItemJSONPayload.getString(currentField.getKey()));
                        editTextField.setHint(currentField.getLabel());
                        textInputLayoutField.addView(editTextField);
                        linearLayout.addView(textInputLayoutField);
                        break;

                    case "boolean":
                        Switch switch_ = new Switch(getContext());
                        switch_.setChecked(mergedItemJSONPayload.getBoolean(currentField.getKey()));

                        switch_.setText(currentField.getLabel());
                        switch_.setPadding(0, 0, 0, 40);
                        linearLayout.addView(switch_);
                        break;

                    case "choice":
                        TextView textView = new TextView(getContext());
                        textView.setText(currentField.getLabel());
                        textView.setTextSize(12);
                        textView.setTextColor(Color.parseColor("#b3b3b3"));

                        Spinner spinner = new Spinner(getContext());
                        KeyValueDropDownAdapter.DropDownEntry[] choices;
                        try {
                            choices = getChoicesFromField(Objects.requireNonNull(currentField.getChoices()));
                        } catch (JSONException | NullPointerException e) {
                            ToastUtility.displayCenteredToastMessage(getContext(), "Falsches Server-Format! Dropdowns können nicht angezeigt werden!", Toast.LENGTH_SHORT);
                            break;
                        }

                        KeyValueDropDownAdapter adapter = new KeyValueDropDownAdapter(Objects.requireNonNull(getContext()), R.layout.support_simple_spinner_dropdown_item, choices);
                        spinner.setAdapter(adapter);
                        spinner.setSelection(adapter.getItemIndexFromDescription(mergedItemJSONPayload.getString(currentField.getKey())));
                        linearLayout.addView(textView);
                        linearLayout.addView(spinner);
                        break;

                    default:
                        break;
                }
            }
        }


    }

    private KeyValueDropDownAdapter.DropDownEntry[] getChoicesFromField(JSONArray choices) throws
            JSONException {
        KeyValueDropDownAdapter.DropDownEntry[] ret = new KeyValueDropDownAdapter.DropDownEntry[choices.length()];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = new KeyValueDropDownAdapter.DropDownEntry(choices.getJSONObject(i));
        }
        return ret;
    }

    private MergedItemField getCurrentFieldFromKey(String key, Map<String, MergedItemField> mapFields) {
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
