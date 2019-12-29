package com.example.capentory_client.repos;


import android.content.Context;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.example.capentory_client.androidutility.FileUtility;
import com.example.capentory_client.androidutility.PreferenceUtility;
import com.example.capentory_client.models.Attachment;
import com.example.capentory_client.models.Room;
import com.example.capentory_client.models.SerializerEntry;
import com.example.capentory_client.models.Stocktaking;
import com.example.capentory_client.ui.AttachmentsFragment;
import com.example.capentory_client.ui.MainActivity;
import com.example.capentory_client.ui.errorhandling.CustomException;
import com.example.capentory_client.viewmodels.customlivedata.StatusAwareLiveData;
import com.example.capentory_client.viewmodels.wrappers.StatusAwareData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

@Singleton
public class AttachmentsRepository extends NetworkRepository<Attachment> {

    public interface AttachmentAPI {

        @Multipart
        @POST(SerializerEntry.attachmentUrl)
        Call<ResponseBody> addFile(@Header("authorization") String aut,
                                   @Part MultipartBody.Part file,
                                   @Part("description") String description);
    }

    @Inject
    public AttachmentsRepository(Context context) {
        super(context);
    }


    @Override
    public StatusAwareLiveData<Attachment> fetchMainData(String... args) {
        if (args.length != 1)
            throw new IllegalArgumentException("Only needs the filepath as argument!");

        mainContentRepoData.postFetching();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(NetworkRepository.getNonJsonUrl(context, true, ""))
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        AttachmentAPI apiService = retrofit.create(AttachmentAPI.class);
        File file = new File(args[0]);


        RequestBody fileAsRequestBody = RequestBody.create(MediaType.parse("*/*"), file);
        MultipartBody.Part fileAsMultipart = MultipartBody.Part.createFormData("file", file.getName(), fileAsRequestBody);


        Call<ResponseBody> call = apiService.addFile("Token "
                        + PreferenceUtility.getToken(context),
                fileAsMultipart,
                "lul");


        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                String message = response.message();
               /* Log.e("PostSnapResponse", message);*/

                message = "       {\n" +
                        "                            \"url\": \"/attachment/1-506f7ffe-ad71-413b-a100-ca40dcf5e4cd.jpg\",\n" +
                        "                            \"description\": \"\",\n" +
                        "                            \"id\": 1\n" +
                        "                        }";
                handleMainSuccessfulResponse(message);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                mainContentRepoData.postError(new Exception(t));
            }
        });


        return mainContentRepoData;
    }


    @Override
    protected void handleMainSuccessfulResponse(String stringPayload) {
        // We are not using the Volley library so we need to directly call this method
        try {
            mainContentRepoData.postSuccess(new Attachment(new JSONObject(stringPayload)));
        } catch (JSONException e) {
            mainContentRepoData.postError(e);
        }
    }

}
