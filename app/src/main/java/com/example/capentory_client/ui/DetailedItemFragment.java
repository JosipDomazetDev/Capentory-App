package com.example.capentory_client.ui;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.fragment.NavHostFragment;

import com.example.capentory_client.R;
import com.example.capentory_client.androidutility.ToastUtility;
import com.example.capentory_client.models.MergedItem;
import com.example.capentory_client.models.MergedItemField;
import com.example.capentory_client.models.Room;
import com.example.capentory_client.models.ValidationEntry;
import com.example.capentory_client.repos.DetailItemRepository;
import com.example.capentory_client.ui.errorhandling.BasicNetworkErrorHandler;
import com.example.capentory_client.viewmodels.DetailItemViewModel;
import com.example.capentory_client.viewmodels.ViewModelProviderFactory;
import com.example.capentory_client.viewmodels.adapter.GenericDropDownAdapter;
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
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.inject.Inject;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * create an instance of this fragment.
 */
public class DetailedItemFragment extends NetworkFragment<Map<String, MergedItemField>, DetailItemRepository, DetailItemViewModel> {
    private ItemxDetailSharedViewModel itemxDetailSharedViewModel;
    private View view;
    // "comment" ==> to generated View for the Field
    private Map<String, View> mergedItemFieldViewMap = new HashMap<>();

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
        itemxDetailSharedViewModel = ViewModelProviders.of(Objects.requireNonNull(getActivity())).get(ItemxDetailSharedViewModel.class);


        initWithFetch(ViewModelProviders.of(this, providerFactory).get(DetailItemViewModel.class),
                basicNetworkErrorHandler,
                view,
                R.id.progress_bar_fragment_item_detail,
                linearLayout,
                R.id.swipe_refresh_fragment_item_detail, () -> {
                    networkViewModel.reloadData();
                    if (networkViewModel.getSearchedForItem() != null) {
                        StatusAwareData<MergedItem> searchedForItemLiveData = networkViewModel.getSearchedForItem().getValue();
                        if (searchedForItemLiveData != null && searchedForItemLiveData.getStatus() == StatusAwareData.State.ERROR) {
                            fetchSearchedForItem(view);
                        }
                    }
                });


        ImageButton validateButton = view.findViewById(R.id.validate_btn_fragment_itemdetail);
        validateButton.setOnClickListener(v -> handleValidate());

        ImageButton cancelButton = view.findViewById(R.id.cancel_btn_fragment_itemdetail);
        cancelButton.setOnClickListener(v -> handleCancel());
    }

    private boolean fetchSearchedForItem(@NonNull View view) {
        MergedItem mergedItem = itemxDetailSharedViewModel.getCurrentItem().getValue();
        if (mergedItem != null && mergedItem.isSearchedForItem()) {
            networkViewModel.fetchSearchedForItem(mergedItem.getBarcode());
            observeSpecificLiveData(networkViewModel.getSearchedForItem(), liveData -> handleSearchedForItemResponse(view, liveData));
            return true;
        }
        return false;
    }

    private void handleSearchedForItemResponse(@NonNull View view, StatusAwareData<MergedItem> liveData) {
        MergedItem mergedItem = liveData.getData();
        itemxDetailSharedViewModel.setCurrentItem(mergedItem);
        TextView textView = view.findViewById(R.id.otherroom_textview_fragment_itemdetail);
        assert mergedItem != null;

        if (mergedItem.isNewItem())
            textView.setText(getString(R.string.text_unkown_item_fragment_detailitem));
        else
            textView.setText(getString(R.string.text_kown_but_different_room_item_fragment_detailitem, mergedItem.getDescriptionaryRoom()));

        textView.setVisibility(View.VISIBLE);
        displayForm(view);
        hideProgressBarAndShowContent();
    }


    @Override
    protected void handleSuccess(StatusAwareData<Map<String, MergedItemField>> statusAwareData) {
        super.handleSuccess(statusAwareData);
        if (!fetchSearchedForItem(view))
            displayForm(view);
    }


    private void displayForm(View view) {
        MergedItem mergedItem = Objects.requireNonNull(itemxDetailSharedViewModel.getCurrentItem().getValue());
        StatusAwareData<Map<String, MergedItemField>> mapStatusAwareData = Objects.requireNonNull(networkViewModel.getData().getValue());
        if (mergedItem.isSearchedForItem() || mapStatusAwareData.getData() == null) return;


        JSONObject fieldsWithValues = mergedItem.getFieldsWithValues();

        displayStaticViews(view, mergedItem);

        LinearLayout linearLayout = view.findViewById(R.id.linearLayout_fragment_itemdetail);
        linearLayout.removeAllViews();

        Map<String, MergedItemField> mapFieldNameToField = mapStatusAwareData.getData();
        assert mapFieldNameToField != null;

        for (String fieldName : mapFieldNameToField.keySet()) {
            displayViewForType(fieldsWithValues, Objects.requireNonNull(mapFieldNameToField.get(fieldName)), view, linearLayout
            );
        }


    }

    private void displayStaticViews(View view, MergedItem mergedItem) {
        ((TextView) view.findViewById(R.id.barcode_fragment_itemdetail))
                .setText(Html.fromHtml(
                        getString(R.string.barcode_detailitem_fragment, mergedItem.getCheckedDisplayBarcode())));
        ((TextView) view.findViewById(R.id.bezeichnung_fragment_itemdetail))
                .setText(Html.fromHtml(
                        getString(R.string.bez_detailitem_fragment, mergedItem.getCheckedDisplayName())));


        List<Room> currentRooms = itemxDetailSharedViewModel.getCurrentRooms().getValue();
        if (itemxDetailSharedViewModel.areSubRoomsInvolved()) {
            GenericDropDownAdapter<Room> adapter =
                    new GenericDropDownAdapter<>(Objects.requireNonNull(getContext()), (ArrayList<Room>) currentRooms);

            Spinner subRoomDropDown = view.findViewById(R.id.subroom_dropdown_fragment_itemdetail);
            subRoomDropDown.setAdapter(adapter);
            subRoomDropDown.setSelection(adapter.getPosition(mergedItem.getSubroom()));
            subRoomDropDown.setVisibility(View.VISIBLE);
            view.findViewById(R.id.subroom_dropdown_text_fragment_itemdetail).setVisibility(View.VISIBLE);
        }
    }

    @SuppressLint("SetTextI18n")
    private void displayViewForType(JSONObject fieldsWithValuesFromItem, MergedItemField currentField, View view, LinearLayout linearLayout) {
        if (currentField.isReadOnly()) {
            TextView textView = new TextView(Objects.requireNonNull(getContext()));
            textView.setText(currentField.getVerboseName() + ": " + fieldsWithValuesFromItem.opt(currentField.getKey()));
            textView.setPadding(4, 0, 0, 40);
            linearLayout.addView(textView);
        } else
            switch (currentField.getType().toLowerCase()) {
                case "integer":
                    TextInputLayout textInputLayoutNumber = new TextInputLayout(view.getContext());
                    textInputLayoutNumber.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                    textInputLayoutNumber.setPadding(0, 0, 0, 40);

                    TextInputEditText editNumber = new TextInputEditText(Objects.requireNonNull(getContext()));
                    editNumber.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_NUMBER);
                    editNumber.setText(fieldsWithValuesFromItem.optString(currentField.getKey()));
                    editNumber.setHint(currentField.getVerboseName());
                    textInputLayoutNumber.addView(editNumber);

                    linearLayout.addView(textInputLayoutNumber);
                    mergedItemFieldViewMap.put(currentField.getKey(), editNumber);
                    break;

                case "datetime":
                    TextInputLayout textInputLayoutDate = new TextInputLayout(view.getContext());
                    textInputLayoutDate.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                    textInputLayoutDate.setPadding(0, 0, 0, 40);

                    TextInputEditText editDate = new TextInputEditText(Objects.requireNonNull(getContext()));
                    editDate.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_DATETIME);
                    editDate.setText(fieldsWithValuesFromItem.optString(currentField.getKey()));
                    editDate.setHint(currentField.getVerboseName());
                    textInputLayoutDate.addView(editDate);

                    linearLayout.addView(textInputLayoutDate);
                    mergedItemFieldViewMap.put(currentField.getKey(), editDate);
                    break;

                case "string":
                    TextInputLayout textInputLayout = new TextInputLayout(view.getContext());
                    textInputLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                    textInputLayout.setPadding(0, 0, 0, 40);

                    TextInputEditText editText = new TextInputEditText(Objects.requireNonNull(getContext()));
                    editText.setText(fieldsWithValuesFromItem.optString(currentField.getKey()));
                    editText.setHint(currentField.getVerboseName());
                    textInputLayout.addView(editText);
                    linearLayout.addView(textInputLayout);

                    mergedItemFieldViewMap.put(currentField.getKey(), editText);
                    break;

            /*case "field":
                TextInputLayout textInputLayoutField = new TextInputLayout(view.getContext());
                textInputLayoutField.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                textInputLayoutField.setPadding(0, 0, 0, 40);

                TextInputEditText editTextField = new TextInputEditText(Objects.requireNonNull(getContext()));
                editTextField.setText(mergedItemJSONPayload.getString(currentField.getKey()));
                editTextField.setHint(currentField.getVerboseName());
                textInputLayoutField.addView(editTextField);


                linearLayout.addView(textInputLayoutField);
                mergedItemFieldViewMap.put(currentField, editTextField);
                break;
*/
                case "boolean":
                    Switch switch_ = new Switch(getContext());
                    switch_.setChecked(fieldsWithValuesFromItem.optBoolean(currentField.getKey()));

                    switch_.setText(currentField.getVerboseName());
                    switch_.setPadding(0, 0, 0, 40);
                    linearLayout.addView(switch_);
                    mergedItemFieldViewMap.put(currentField.getKey(), switch_);
                    break;

                case "choice":
                    TextView textView = new TextView(getContext());
                    textView.setText(currentField.getVerboseName());
                    textView.setTextSize(12);
                    textView.setTextColor(Color.parseColor("#b3b3b3"));

                    Spinner spinner = new Spinner(getContext());
                    KeyValueDropDownAdapter.DropDownEntry[] choices;
                    try {
                        choices = getChoicesFromField(Objects.requireNonNull(currentField.getChoices()));
                    } catch (JSONException | NullPointerException e) {
                        ToastUtility.displayCenteredToastMessage(getContext(), getString(R.string.dropdown_error_fragment_item_detail), Toast.LENGTH_SHORT);
                        break;
                    }

                    KeyValueDropDownAdapter adapter = new KeyValueDropDownAdapter(Objects.requireNonNull(getContext()), R.layout.support_simple_spinner_dropdown_item, choices);
                    spinner.setAdapter(adapter);
                    int i = fieldsWithValuesFromItem.optInt(currentField.getKey(), KeyValueDropDownAdapter.NULL_KEY_VALUE);
                    spinner.setSelection(adapter.getItemIndexFromKey(i));
                    linearLayout.addView(textView);
                    linearLayout.addView(spinner);

                    mergedItemFieldViewMap.put(currentField.getKey(), spinner);
                    break;

                default:
                    break;
            }
    }


    private KeyValueDropDownAdapter.DropDownEntry[] getChoicesFromField(JSONArray choices) throws JSONException {
        int maxKey = 1;
        KeyValueDropDownAdapter.DropDownEntry[] ret = new KeyValueDropDownAdapter.DropDownEntry[choices.length() + 1];
        for (int i = 0; i < ret.length - 1; i++) {
            ret[i] = new KeyValueDropDownAdapter.DropDownEntry(choices.getJSONObject(i));
            maxKey = Math.max(maxKey, ret[i].getKey());
        }
        ret[ret.length - 1] = new KeyValueDropDownAdapter.DropDownEntry(maxKey + 1, getString(R.string.null_dropdown_value_fragment_item_detail));
        return ret;
    }


    public void handleValidate() {
        MergedItem currentItem = Objects.requireNonNull(itemxDetailSharedViewModel.getCurrentItem().getValue());
        ValidationEntry validationEntry = getValidationEntryFromFormData(currentItem);
        itemxDetailSharedViewModel.setValidationEntryForCurrentItem(validationEntry);
        NavHostFragment.findNavController(this).popBackStack();
    }


    private void handleCancel() {
        MergedItem mergedItem = Objects.requireNonNull(itemxDetailSharedViewModel.getCurrentItem().getValue());
        if (mergedItem.isParentItem() && mergedItem.getRemainingTimes() > 0) {
            new AlertDialog.Builder(Objects.requireNonNull(getContext()))
                    .setTitle(getString(R.string.title_handle_cancel_fragment_item_detail))
                    .setMessage(getString(R.string.msg_handle_cancel_fragment_item_detail, mergedItem.getRemainingTimes()))
                    .setPositiveButton(getString(R.string.positive_handle_cancel_fragment_item_detail), (dialog, which) -> {
                        itemxDetailSharedViewModel.setValidationEntryForCurrentItem(ValidationEntry.createCanceledEntry());
                        NavHostFragment.findNavController(DetailedItemFragment.this).popBackStack();
                    })
                    .setNegativeButton(getString(R.string.negative_handle_cancel_fragment_item_detail), (dialog, which) -> {
                        itemxDetailSharedViewModel.setValidationEntryForCurrentItem(null);
                        NavHostFragment.findNavController(DetailedItemFragment.this).popBackStack();
                    })
                    .show();
        } else {
            itemxDetailSharedViewModel.setValidationEntryForCurrentItem(ValidationEntry.createCanceledEntry());
            NavHostFragment.findNavController(this).popBackStack();
        }
    }

    @NonNull
    private ValidationEntry getValidationEntryFromFormData(MergedItem currentItem) {
        // Prepare the ValidationEntry for the currentItem
        ValidationEntry validationEntry = new ValidationEntry(currentItem);
        // Set static mark for later value
        validationEntry.setStaticMarkForLater(((CheckBox) view.findViewById(R.id.mark_for_later_checkbox_fragment_itemdetail)).isChecked());

        // Subrooms also have a static field to change them
        if (itemxDetailSharedViewModel.areSubRoomsInvolved()) {
            Spinner subRoomDropDown = view.findViewById(R.id.subroom_dropdown_fragment_itemdetail);
            Room selectedRoom = (Room) subRoomDropDown.getSelectedItem();
            validationEntry.setStaticRoomChange(selectedRoom, currentItem.getSubroom());
        }

        // fieldName, MergedItemField
        Map<String, MergedItemField> mapFieldNameToField = Objects.requireNonNull(networkViewModel.getData().getValue()).getData();
        assert mapFieldNameToField != null;

        for (String fieldName : mapFieldNameToField.keySet()) {
            MergedItemField field = mapFieldNameToField.get(fieldName);
            if (field == null || field.isReadOnly()) continue;

            Object valueFromForm = getValueFromGUIField(field);
            validationEntry.addChangedFieldFromFormValue(fieldName, valueFromForm);
        }
        return validationEntry;
    }


    @SuppressWarnings("unchecked")
    public <T> T getValueFromGUIField(MergedItemField fieldForItem) {

        View generatedView = mergedItemFieldViewMap.get(fieldForItem.getKey());

        if (generatedView == null) return null;

        switch (fieldForItem.getType()) {
            case "integer":
                int i = Integer.parseInt(((TextInputEditText) generatedView).getText().toString());
                return (T) Integer.valueOf(i);

            case "datetime":
                return (T) ((TextInputEditText) generatedView).getText().toString();


            case "string":
                String s = ((TextInputEditText) generatedView).getText().toString();
                if (s.equals("null")) return null;
                return (T) s;


            case "field":
                return (T) ((TextInputEditText) generatedView).getText().toString();

            case "boolean":
                boolean checked = ((Switch) generatedView).isChecked();
                return (T) Boolean.valueOf(checked);


            case "choice":
                KeyValueDropDownAdapter.DropDownEntry selectedItem = (KeyValueDropDownAdapter.DropDownEntry) ((Spinner) generatedView).getSelectedItem();
                if (selectedItem.isNullRepresentationEntry()) return null;
                int key = selectedItem.getKey();
                return (T) Integer.valueOf(key);
            default:
                return null;
        }

    }


}
