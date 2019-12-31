package com.capentory.capentory_client.ui;

import android.animation.LayoutTransition;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.fragment.NavHostFragment;

import com.capentory.capentory_client.R;
import com.capentory.capentory_client.androidutility.ToastUtility;
import com.capentory.capentory_client.models.MergedItem;
import com.capentory.capentory_client.models.MergedItemField;
import com.capentory.capentory_client.models.Room;
import com.capentory.capentory_client.models.ValidationEntry;
import com.capentory.capentory_client.repos.DetailItemRepository;
import com.capentory.capentory_client.ui.errorhandling.BasicNetworkErrorHandler;
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
    // "comment" ==> to generated View for the Field
    private Map<String, View> mergedItemFieldViewMap = new HashMap<>();

    public DetailedItemFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_item_detail, container, false);
    }

    private SensorManager mSensorManager;
    /*    private float accel;
        private float accelCurrent;
        private float accelLast;*/
    private boolean called = false;

    private final SensorEventListener mSensorListener = new SensorEventListener() {
        /* @Override
         public void onSensorChanged(SensorEvent event) {
             float x = event.values[0];
             float y = event.values[1];
             float z = event.values[2];
             accelLast = accelCurrent;
             //Log.e("x", x + "/" + y + "/" + z);
             accelCurrent = (float) Math.sqrt((double) (x * x + y * y + z + z));
             float delta = accelCurrent - accelLast;
             accel = accel * 0.9f + delta;
             Log.e("XXX", accel + "");
             if (accel > 25 && !called) {
                 called = true;
                 Toast.makeText(getContext(), "Item validated!", Toast.LENGTH_SHORT).show();
                 handleValidate();
             }
         }*/
        long lastUpdate = 0;
        float last_x = 0;
        float last_y = 0;
        float last_z = 0;

        public void onSensorChanged(SensorEvent event) {
            long curTime = System.currentTimeMillis();
            // only allow one update every 100ms.
            if ((curTime - lastUpdate) > 50) {
                long diffTime = (curTime - lastUpdate);
                lastUpdate = curTime;

                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];

                float speed = Math.abs(x + y + z - last_x - last_y - last_z) / diffTime * 10000;

                if (speed > 6500 && !called) {
                    //((TextView) (view.findViewById(R.id.bezeichnung_fragment_itemdetail))).setText( ""+speed);
                    called = true;
                    Toast.makeText(getContext(), "Item validated!", Toast.LENGTH_SHORT).show();
                    handleValidate();
                }
                last_x = x;
                last_y = y;
                last_z = z;
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    @Inject
    ViewModelProviderFactory providerFactory;
    private boolean shouldStop = false;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        mSensorManager = (SensorManager) Objects.requireNonNull(getContext()).getApplicationContext().getSystemService(Context.SENSOR_SERVICE);

        Objects.requireNonNull(mSensorManager).registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION),
                SensorManager.SENSOR_DELAY_NORMAL);
      /*  accel = 10f;
        accelCurrent = SensorManager.GRAVITY_EARTH;
        accelLast = SensorManager.GRAVITY_EARTH;*/

        this.view = view;
        LinearLayout linearLayout = view.findViewById(R.id.content_fragment_item_detail);
        basicNetworkErrorHandler = new BasicNetworkErrorHandler(getContext(), view.findViewById(R.id.dummy));
        itemxDetailSharedViewModel = ViewModelProviders.of(Objects.requireNonNull(getActivity())).get(ItemxDetailSharedViewModel.class);
        detailXAttachmentViewModel = ViewModelProviders.of(Objects.requireNonNull(getActivity())).get(DetailXAttachmentViewModel.class);

        detailXAttachmentViewModel.getExitedAttachmentScreen().observe(getViewLifecycleOwner(), aBoolean -> {
            shouldStop = aBoolean;

        });

        /*if (shouldStop) return;*/

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

        view.findViewById(R.id.attachment_btn_fragment_itemdetail).setOnClickListener(v -> handleAttachment());
    }

    private void handleAttachment() {
        detailXAttachmentViewModel.setCurrentItem(itemxDetailSharedViewModel.getCurrentItem());
        NavHostFragment.findNavController(this).navigate(R.id.next);
    }

    private boolean fetchSearchedForItem(@NonNull View view) {
        MergedItem mergedItem = itemxDetailSharedViewModel.getCurrentItem();
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
        MergedItem mergedItem = Objects.requireNonNull(itemxDetailSharedViewModel.getCurrentItem());
        StatusAwareData<Map<String, MergedItemField>> mapStatusAwareData = Objects.requireNonNull(networkViewModel.getData().getValue());
        if (mergedItem.isSearchedForItem() || mapStatusAwareData.getData() == null) return;
        JSONObject fieldsWithValues = mergedItem.getNormalFieldsWithValues();
        JSONObject customFieldsWithValues = mergedItem.getCustomFieldsWithValues();

        displayStaticViews(view, mergedItem);
        LinearLayout content = view.findViewById(R.id.linearLayout_fragment_itemdetail);
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
        View hr = new View(getContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                1
        );
        params.setMargins(4, 20, 4, 20);
        hr.setLayoutParams(params);
        hr.setBackgroundColor(Color.parseColor("#D8D8D8"));
        content.addView(hr);

        ImageButton expandButton = new ImageButton(getContext());
        expandButton.setImageResource(R.drawable.ic_expand_more_black_48dp);
        expandButton.setBackground(null);
        content.addView(expandButton);
        return expandButton;
    }

    @NonNull
    private LinearLayout addSeparateLinearLayout(LinearLayout content) {
        LinearLayout linearLayoutExtraFields = new LinearLayout(getContext());
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
            if (fieldsWithValuesFromItem.has(currentField.getKey()))
                return;
        }

        if (currentField.isReadOnly()) {
            TextView textView = new TextView(Objects.requireNonNull(getContext()));
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
                    TextInputLayout textInputLayout = new TextInputLayout(view.getContext());
                    textInputLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                    textInputLayout.setPadding(0, 0, 0, 40);

                    TextInputEditText editText = new TextInputEditText(Objects.requireNonNull(getContext()));

                    if (!fieldsWithValuesFromItem.isNull(currentField.getKey()))
                        editText.setText(fieldsWithValuesFromItem.optString(currentField.getKey()));
                    editText.setHint(currentField.getVerboseName());
                    textInputLayout.addView(editText);
                    linearLayout.addView(textInputLayout);

                    mergedItemFieldViewMap.put(currentField.getKey(), editText);
                    break;

                case "boolean":
                    CheckBox checkbox = new CheckBox(getContext());
                    checkbox.setChecked(fieldsWithValuesFromItem.optBoolean(currentField.getKey()));

                    checkbox.setText(currentField.getVerboseName());
                    checkbox.setPadding(0, 20, 0, 20);
                    linearLayout.addView(checkbox);
                    mergedItemFieldViewMap.put(currentField.getKey(), checkbox);
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
                        e.printStackTrace();
                        break;
                    }

                    KeyValueDropDownAdapter adapter = new KeyValueDropDownAdapter(Objects.requireNonNull(getContext()), R.layout.support_simple_spinner_dropdown_item, choices);
                    spinner.setAdapter(adapter);
                    Object i;
                    try {
                        i = fieldsWithValuesFromItem.get(currentField.getKey());
                    } catch (JSONException e) {
                        ToastUtility.displayCenteredToastMessage(getContext(), getString(R.string.dropdown_error_fragment_item_detail), Toast.LENGTH_SHORT);
                        e.printStackTrace();
                        break;
                    }
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
            validationEntry.setStaticRoomChange(selectedRoom, currentItem.getSubroom());
        }

        // fieldName, MergedItemField
        Map<String, MergedItemField> mapFieldNameToField = Objects.requireNonNull(networkViewModel.getData().getValue()).getData();
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
        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
        super.onResume();
    }

    @Override
    public void onPause() {
        mSensorManager.unregisterListener(mSensorListener);
        super.onPause();
    }
}
