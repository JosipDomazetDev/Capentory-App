package com.capentory.capentory_client.ui;

import android.animation.LayoutTransition;
import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.fragment.NavHostFragment;

import com.capentory.capentory_client.R;
import com.capentory.capentory_client.androidutility.ToastUtility;
import com.capentory.capentory_client.androidutility.UserUtility;
import com.capentory.capentory_client.androidutility.AlertUtility;
import com.capentory.capentory_client.models.MergedItem;
import com.capentory.capentory_client.models.MergedItemField;
import com.capentory.capentory_client.models.Room;
import com.capentory.capentory_client.models.ValidationEntry;
import com.capentory.capentory_client.repos.DetailItemRepository;
import com.capentory.capentory_client.ui.errorhandling.ErrorHandler;
import com.capentory.capentory_client.ui.shakedetection.ShakeDetector;
import com.capentory.capentory_client.viewmodels.DetailItemViewModel;
import com.capentory.capentory_client.viewmodels.ViewModelProviderFactory;
import com.capentory.capentory_client.viewmodels.adapter.GenericDropDownAdapter;
import com.capentory.capentory_client.viewmodels.adapter.KeyValueDropDownAdapter;
import com.capentory.capentory_client.viewmodels.sharedviewmodels.DetailXAttachmentViewModel;
import com.capentory.capentory_client.viewmodels.sharedviewmodels.ItemxDetailSharedViewModel;
import com.capentory.capentory_client.viewmodels.wrappers.StatusAwareData;
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
import java.util.Set;

import javax.inject.Inject;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * create an instance of this fragment.
 */
public class DetailedItemFragment extends NetworkFragment<Map<String, MergedItemField>, DetailItemRepository, DetailItemViewModel> {
    private ItemxDetailSharedViewModel itemxDetailSharedViewModel;
    private DetailXAttachmentViewModel detailXAttachmentViewModel;
    private View view;
    // field e.g. "comment" / maps to / generated View for the Field
    private Map<String, View> mergedItemFieldViewMap = new HashMap<>();
    private ShakeDetector shakeDetector;

    public DetailedItemFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_item_detail, container, false);
    }

    private boolean called = false;


    @Inject
    ViewModelProviderFactory providerFactory;
    private boolean shouldStop = false;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        this.view = view;
        LinearLayout linearLayout = view.findViewById(R.id.content_fragment_item_detail);
        errorHandler = new ErrorHandler(getContext(), view.findViewById(R.id.dummy));
        itemxDetailSharedViewModel = ViewModelProviders.of(Objects.requireNonNull(getActivity())).get(ItemxDetailSharedViewModel.class);
        detailXAttachmentViewModel = ViewModelProviders.of(Objects.requireNonNull(getActivity())).get(DetailXAttachmentViewModel.class);

        detailXAttachmentViewModel.getExitedAttachmentScreen().observe(getViewLifecycleOwner(), aBoolean -> shouldStop = aBoolean);


        initWithFetch(ViewModelProviders.of(this, providerFactory).get(DetailItemViewModel.class),
                errorHandler,
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

        view.findViewById(R.id.attachment_btn_fragment_itemdetail).setOnClickListener(v -> handleAttachment());

        shakeDetector = new ShakeDetector(Objects.requireNonNull(getContext()), this::handleShake);
        shakeDetector.registerShakeDetector();
    }

    private void handleShake() {
        if (called) return;
        called = true;
        Toast.makeText(getContext(), getString(R.string.item_validated_detaileditem_fragment), Toast.LENGTH_SHORT).show();
        handleValidate();
    }

    private void handleAttachment() {
        detailXAttachmentViewModel.setCurrentItem(itemxDetailSharedViewModel.getCurrentItem());
        NavHostFragment.findNavController(this).navigate(R.id.next);
    }

    private boolean fetchSearchedForItem(@NonNull View view) {
        MergedItem mergedItem = itemxDetailSharedViewModel.getCurrentItem();
        if (mergedItem != null && mergedItem.isSearchedForItem()) {
            // Item is not of the normal case, therefore vibrate
            AlertUtility.makeNormalVibration(getContext());

            networkViewModel.fetchSearchedForItem(mergedItem.getBarcode());
            observeSpecificLiveData(networkViewModel.getSearchedForItem(), liveData -> handleSearchedForItemResponse(view, liveData));
            return true;
        }
        return false;
    }

    private void handleSearchedForItemResponse(@NonNull View view, StatusAwareData<MergedItem> liveData) {
        MergedItem mergedItem = liveData.getData();
        itemxDetailSharedViewModel.setCurrentItem(mergedItem);
        if (mergedItem == null) return;

        TextView edgeCaseTextView = view.findViewById(R.id.otherroom_textview_fragment_itemdetail);

        if (mergedItem.isNewItem()) {
            edgeCaseTextView.setText(getString(R.string.text_unkown_item_fragment_detailitem));
        } else if (mergedItem.isFromOtherRoom()){
            edgeCaseTextView.setText(getString(R.string.text_kown_but_different_room_item_fragment_detailitem, mergedItem.getDescriptionaryRoom()));
        }

        edgeCaseTextView.setVisibility(View.VISIBLE);
        displayForm(view);
        hideProgressBarAndShowContent();
    }


    @Override
    protected void handleSuccess(StatusAwareData<Map<String, MergedItemField>> statusAwareData) {
        super.handleSuccess(statusAwareData);
        if (!fetchSearchedForItem(view)) {

            MergedItem mergedItem = itemxDetailSharedViewModel.getCurrentItem();
            TextView edgeCaseTextView = view.findViewById(R.id.otherroom_textview_fragment_itemdetail);
            edgeCaseTextView.setVisibility(View.VISIBLE);

            // User might move an edge case item from DONE to TO_DO
            if (mergedItem.isNewItem()) {
                edgeCaseTextView.setText(getString(R.string.text_unkown_item_fragment_detailitem));
            } else if (mergedItem.isFromOtherRoom()) {
                edgeCaseTextView.setText(getString(R.string.text_kown_but_different_room_item_fragment_detailitem, mergedItem.getDescriptionaryRoom()));
            } else edgeCaseTextView.setVisibility(View.GONE);

            displayForm(view);
        }
    }


    private void displayForm(View view) {
        MergedItem mergedItem = Objects.requireNonNull(itemxDetailSharedViewModel.getCurrentItem());
        StatusAwareData<Map<String, MergedItemField>> mapStatusAwareData = Objects.requireNonNull(networkViewModel.getLiveData().getValue());
        if (mergedItem.isSearchedForItem() || mapStatusAwareData.getData() == null) return;
        JSONObject fieldsWithValues = mergedItem.getNormalFieldsWithValues();
        JSONObject customFieldsWithValues = mergedItem.getCustomFieldsWithValues();

        displayStaticViews(view, mergedItem);
        LinearLayout content = view.findViewById(R.id.linearLayout_fragment_itemdetail);
        // Animations only for the button click
        content.setLayoutTransition(null);
        content.removeAllViews();

        Map<String, MergedItemField> mapFieldNameToField = mapStatusAwareData.getData();


        // Map is sorted by value, extra fields are at the end
        // Add normal fields
        addNormalFields(view, fieldsWithValues, content, mapFieldNameToField, mapFieldNameToField.keySet());
        // Extra-Fields should be expandable; We need a button for that
        ImageButton expandButton = addShowMoreButton(content);

        // Extra-Fields get their own linearLayout in order to hide it easier
        LinearLayout linearLayoutExtraFields = addSeparateLinearLayout(content);

        // Add the extra fields
        addExtraFields(view, fieldsWithValues, customFieldsWithValues, linearLayoutExtraFields, mapFieldNameToField, mapFieldNameToField.keySet());

        expandButton.setOnClickListener(v -> {
            if (networkViewModel.getExFieldsCollapsedLiveData().getValue() == null) return;

            if (networkViewModel.getExFieldsCollapsedLiveData().getValue()) {
                networkViewModel.setExFieldsCollapsedLiveData(false);
            } else {
                networkViewModel.setExFieldsCollapsedLiveData(true);
            }
        });

        NestedScrollView nestedScrollView = view.findViewById(R.id.scrollView_fragment_item_detail);
        content.setLayoutTransition(new LayoutTransition());
        content.getLayoutTransition().setDuration(100);
        networkViewModel.getExFieldsCollapsedLiveData().observe(getViewLifecycleOwner(), aBoolean -> {
            if (aBoolean) {
                linearLayoutExtraFields.setVisibility(View.GONE);
                expandButton.setImageResource(R.drawable.ic_expand_more_black_48dp);
            } else {
                linearLayoutExtraFields.setVisibility(View.VISIBLE);
                expandButton.setImageResource(R.drawable.ic_expand_less_black_48dp);
                scrollAfterAnimate(view, content, nestedScrollView);
            }
        });
    }

    private void scrollAfterAnimate(View view, LinearLayout content, NestedScrollView nestedScrollView) {
        int[] location = new int[2];
        view.findViewById(R.id.scrollTo_fragment_itemdetail).getLocationOnScreen(location);
        content.getLayoutTransition().addTransitionListener(new LayoutTransition.TransitionListener() {

            @Override
            public void endTransition(LayoutTransition arg0, ViewGroup arg1,
                                      View arg2, int arg3) {
                nestedScrollView.post(() -> nestedScrollView.smoothScrollTo(location[0], location[1]));
            }

            @Override
            public void startTransition(LayoutTransition transition,
                                        ViewGroup container, View view, int transitionType) {

            }
        });
    }

    @NonNull
    private ImageButton addShowMoreButton(LinearLayout content) {
        View hr = new View(content.getContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                1
        );
        params.setMargins(4, 20, 4, 20);
        hr.setLayoutParams(params);
        hr.setBackgroundColor(Color.parseColor("#D8D8D8"));
        content.addView(hr);

        ImageButton expandButton = new ImageButton(content.getContext());
        expandButton.setImageResource(R.drawable.ic_expand_more_black_48dp);
        expandButton.setBackground(null);
        content.addView(expandButton);
        return expandButton;
    }

    @NonNull
    private LinearLayout addSeparateLinearLayout(LinearLayout content) {
        LinearLayout linearLayoutExtraFields = new LinearLayout(content.getContext());
        linearLayoutExtraFields.setOrientation(LinearLayout.VERTICAL);
        linearLayoutExtraFields.setVisibility(View.GONE);
        content.addView(linearLayoutExtraFields);
        return linearLayoutExtraFields;
    }

    private void addExtraFields(View view, JSONObject fieldsWithValues, JSONObject customFieldsWithValues, LinearLayout linearLayoutExtraFields, Map<String, MergedItemField> mapFieldNameToField, Set<String> keys) {
        for (String key : keys) {
            MergedItemField field = mapFieldNameToField.get(key);

            if (field == null) continue;
            // Custom-Fields are also Extra-Fields
            if (field.isCustomField()) {
                addViewForType(customFieldsWithValues, field, view, linearLayoutExtraFields);
            } else if (field.isExtraField()) {
                addViewForType(fieldsWithValues, field, view, linearLayoutExtraFields);
            }
        }
    }

    private void addNormalFields(View view, JSONObject fieldsWithValues, LinearLayout content, Map<String, MergedItemField> mapFieldNameToField, Set<String> keys) {
        content.setLayoutTransition(null);
        for (String key : keys) {
            MergedItemField field = mapFieldNameToField.get(key);

            if (field == null) continue;
            if (field.isExtraField()) break;
            addViewForType(fieldsWithValues, field, view, content);
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
    private void addViewForType(JSONObject fieldsWithValuesFromItem, MergedItemField currentField, View view, LinearLayout linearLayout) {
        if (currentField.isCustomField()) {
            // If the item doesn't contain this CustomField, it means that it doesn't have a CustomField that should be displayed
            if (!fieldsWithValuesFromItem.has(currentField.getKey()))
                return;
        }

        if (currentField.isReadOnly()) {
            TextView textView = new TextView(linearLayout.getContext());
            textView.setText(currentField.getVerboseName() + ": " + fieldsWithValuesFromItem.opt(currentField.getKey()));
            textView.setPadding(4, 20, 0, 20);
            linearLayout.addView(textView);
        } else
            switch (currentField.getType().toLowerCase()) {
                case "integer":
                    TextInputLayout textInputLayoutNumber = new TextInputLayout(view.getContext());
                    textInputLayoutNumber.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                    textInputLayoutNumber.setPadding(0, 0, 0, 40);

                    TextInputEditText editNumber = new TextInputEditText(Objects.requireNonNull(getContext()));
                    editNumber.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_NUMBER);
                    if (!fieldsWithValuesFromItem.isNull(currentField.getKey()))
                        editNumber.setText(fieldsWithValuesFromItem.optString(currentField.getKey()));
                    editNumber.setHint(currentField.getVerboseName());
                    textInputLayoutNumber.addView(editNumber);

                    linearLayout.addView(textInputLayoutNumber);
                    mergedItemFieldViewMap.put(currentField.getKey(), editNumber);
                    break;


                case "url":
                    TextInputLayout textInputLayoutUrl = new TextInputLayout(view.getContext());
                    textInputLayoutUrl.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                    textInputLayoutUrl.setPadding(0, 0, 0, 40);

                    TextInputEditText editUrl = new TextInputEditText(Objects.requireNonNull(getContext()));
                    editUrl.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_WEB_EDIT_TEXT);
                    if (!fieldsWithValuesFromItem.isNull(currentField.getKey()))
                        editUrl.setText(fieldsWithValuesFromItem.optString(currentField.getKey()));
                    editUrl.setHint(currentField.getVerboseName());
                    textInputLayoutUrl.addView(editUrl);

                    linearLayout.addView(textInputLayoutUrl);
                    mergedItemFieldViewMap.put(currentField.getKey(), editUrl);
                    break;


                case "date":
                    TextInputLayout textInputLayoutDate = new TextInputLayout(view.getContext());
                    textInputLayoutDate.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                    textInputLayoutDate.setPadding(0, 0, 0, 40);

                    TextInputEditText editDate = new TextInputEditText(Objects.requireNonNull(getContext()));
                    editDate.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_DATETIME);
                    if (!fieldsWithValuesFromItem.isNull(currentField.getKey()))
                        editDate.setText(fieldsWithValuesFromItem.optString(currentField.getKey()));
                    editDate.setHint(currentField.getVerboseName());
                    textInputLayoutDate.addView(editDate);

                    linearLayout.addView(textInputLayoutDate);
                    mergedItemFieldViewMap.put(currentField.getKey(), editDate);
                    break;

                case "string":
                    TextInputLayout textInputLayout = new TextInputLayout(new ContextThemeWrapper(linearLayout.getContext(), R.style.Widget_MaterialComponents_TextInputLayout_OutlinedBox));
                    textInputLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                    textInputLayout.setPadding(0, 0, 0, 40);
                    //textInputLayout.setHintTextAppearance(R.style.Base_Widget_MaterialComponents_TextInputLayout_TextInputLayout);
                    textInputLayout.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);
                    //textInputLayout.setHintTextColor(textInputLayout.getContext().getResources().getColorStateList());
                    textInputLayout.setBoxBackgroundColor(ContextCompat.getColor(textInputLayout.getContext(), android.R.color.white));


                    TextInputEditText editText = new TextInputEditText(textInputLayout.getContext());

                    if (!fieldsWithValuesFromItem.isNull(currentField.getKey()))
                        editText.setText(fieldsWithValuesFromItem.optString(currentField.getKey()));
                    editText.setHint(currentField.getVerboseName());
                    //editText.setHintTextColor(Color.parseColor("FFFFFF"));

                    textInputLayout.addView(editText, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
                    linearLayout.addView(textInputLayout);

                    mergedItemFieldViewMap.put(currentField.getKey(), editText);
                    break;

                case "boolean":
                    CheckBox checkbox = new CheckBox(linearLayout.getContext());
                    checkbox.setChecked(fieldsWithValuesFromItem.optBoolean(currentField.getKey()));

                    checkbox.setText(currentField.getVerboseName());
                    checkbox.setPadding(0, 20, 0, 20);
                    linearLayout.addView(checkbox);
                    mergedItemFieldViewMap.put(currentField.getKey(), checkbox);
                    break;

                case "choice":
                    TextView textView = new TextView(linearLayout.getContext());
                    textView.setText(currentField.getVerboseName());
                    textView.setTextSize(12);
                    textView.setTextColor(Color.parseColor("#b3b3b3"));

                    Spinner spinner = new Spinner(linearLayout.getContext());
                    KeyValueDropDownAdapter.DropDownEntry[] choices;
                    try {
                        choices = getChoicesFromField(Objects.requireNonNull(currentField.getChoices()));
                    } catch (JSONException | NullPointerException e) {
                        ToastUtility.displayCenteredToastMessage(getContext(), getString(R.string.dropdown_error_fragment_item_detail), Toast.LENGTH_SHORT);
                        e.printStackTrace();
                        break;
                    }

                    KeyValueDropDownAdapter adapter = new KeyValueDropDownAdapter(linearLayout.getContext(), R.layout.support_simple_spinner_dropdown_item, choices);
                    spinner.setAdapter(adapter);
                    Object i;
                    i = fieldsWithValuesFromItem.opt(currentField.getKey());
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
        KeyValueDropDownAdapter.DropDownEntry[] ret = new KeyValueDropDownAdapter.DropDownEntry[choices.length()];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = new KeyValueDropDownAdapter.DropDownEntry(choices.getJSONObject(i));
        }
        return ret;
    }


    public void handleValidate() {
        StatusAwareData<Map<String, MergedItemField>> value = networkViewModel.getLiveData().getValue();
        if (value == null) return;
        if (value.getStatus() != StatusAwareData.State.SUCCESS) return;

        if (networkViewModel.getSearchedForItem() != null) {
            StatusAwareData<MergedItem> value1 = networkViewModel.getSearchedForItem().getValue();
            if (value1 == null) return;
            if (value1.getStatus() != StatusAwareData.State.SUCCESS) return;
        }


        MergedItem currentItem = Objects.requireNonNull(itemxDetailSharedViewModel.getCurrentItem());
        ValidationEntry validationEntry = getValidationEntryFromFormData(currentItem);
        itemxDetailSharedViewModel.setValidationEntryForCurrentItem(validationEntry);
        //NavHostFragment.findNavController(this).popBackStack();
        navigateBack();
    }


    private void handleCancel() {
        MergedItem mergedItem = Objects.requireNonNull(itemxDetailSharedViewModel.getCurrentItem());
        if (mergedItem.isParentItem() && mergedItem.getRemainingTimes() > 0) {
            new AlertDialog.Builder(Objects.requireNonNull(getContext()))
                    .setTitle(getString(R.string.title_handle_cancel_fragment_item_detail))
                    .setMessage(getString(R.string.msg_handle_cancel_fragment_item_detail, mergedItem.getRemainingTimes()))
                    .setPositiveButton(getString(R.string.positive_handle_cancel_fragment_item_detail), (dialog, which) -> {
                        itemxDetailSharedViewModel.setValidationEntryForCurrentItem(ValidationEntry.createCanceledEntry());
                        navigateBack();
                    })
                    .setNegativeButton(getString(R.string.negative_handle_cancel_fragment_item_detail), (dialog, which) -> {
                        itemxDetailSharedViewModel.setValidationEntryForCurrentItem(null);
                        navigateBack();
                    })
                    .show();
        } else {
            itemxDetailSharedViewModel.setValidationEntryForCurrentItem(ValidationEntry.createCanceledEntry());
            navigateBack();
        }
    }

    private void navigateBack() {
        UserUtility.hideKeyboard(getActivity());
        NavHostFragment.findNavController(this).popBackStack();
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
            validationEntry.setStaticRoom(selectedRoom);
        }

        // fieldName, MergedItemField
        Map<String, MergedItemField> mapFieldNameToField = Objects.requireNonNull(networkViewModel.getLiveData().getValue()).getData();
        assert mapFieldNameToField != null;

        for (String fieldName : mapFieldNameToField.keySet()) {
            MergedItemField field = mapFieldNameToField.get(fieldName);
            if (field == null || field.isReadOnly()) continue;

            Object valueFromForm = getValueFromGUIField(field);
            validationEntry.addChangedFieldFromFormValue(field, valueFromForm);
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
                boolean checked = ((CheckBox) generatedView).isChecked();
                return (T) Boolean.valueOf(checked);

            case "choice":
                KeyValueDropDownAdapter.DropDownEntry selectedItem = (KeyValueDropDownAdapter.DropDownEntry) ((Spinner) generatedView).getSelectedItem();
                Object key = selectedItem.getKey();
                return (T) key;
            default:
                return null;
        }

    }


    @Override
    public void onResume() {
        super.onResume();
        shakeDetector.registerShakeDetector();
    }

    @Override
    public void onPause() {
        super.onPause();
        shakeDetector.unregisterShakeDetector();
    }
}
