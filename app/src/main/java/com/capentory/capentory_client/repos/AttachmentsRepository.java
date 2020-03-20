package com.capentory.capentory_client.repos;


import android.content.Context;
import android.graphics.Bitmap;

import com.capentory.capentory_client.R;
import com.capentory.capentory_client.androidutility.PreferenceUtility;
import com.capentory.capentory_client.models.Attachment;
import com.capentory.capentory_client.models.SerializerEntry;
import com.capentory.capentory_client.ui.SettingsFragment;
import com.capentory.capentory_client.ui.errorhandling.CustomException;
import com.capentory.capentory_client.viewmodels.customlivedata.StatusAwareLiveData;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import id.zelory.compressor.Compressor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

@Singleton
public class AttachmentsRepository extends NetworkRepository<Attachment> {

    public interface AttachmentAPI {

        @Multipart
        @POST(SerializerEntry.attachmentUrl)
        Call<String> addFile(@Header("authorization") String auth,
                             @Part MultipartBody.Part file,
                             @Part("description") String description);
    }

    @Inject
    public AttachmentsRepository(Context context) {
        super(context);
    }


    @Override
    public StatusAwareLiveData<Attachment> fetchMainData(String... args) {
        if (args.length != 2)
            throw new IllegalArgumentException("Only needs the filepath and description as argument!");


        // Unsubscribe from old observers with this hack...
        mainContentRepoData = new StatusAwareLiveData<>();
        mainContentRepoData.postFetching();

        Call<String> call = prepareCall(args);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                handleMainSuccessfulResponse(response.body());
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                mainContentRepoData.postError(new Exception(t));
            }
        });


        return mainContentRepoData;
    }

    public static OkHttpClient getUnsafeOkHttpClient() {
        // Taken from https://stackoverflow.com/questions/25509296/trusting-all-certificates-with-okhttp/25992879#25992879
        try {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };

            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
            builder.hostnameVerifier((hostname, session) -> true);

            return builder.build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Call<String> prepareCall(String[] args) {
        Retrofit retrofit;
        if (PreferenceUtility.getBoolean(context, SettingsFragment.TRUST_ALL_CERTICATES_KEY)) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(getNonJsonUrl(context, true, ""))
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .client(getUnsafeOkHttpClient())
                    .build();
        } else {
            retrofit = new Retrofit.Builder()
                    .baseUrl(getNonJsonUrl(context, true, ""))
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .build();
        }

        AttachmentAPI apiService = retrofit.create(AttachmentAPI.class);
        File file = new File(args[0]);

        file = handleImageCompression(args, file);


        RequestBody fileAsRequestBody = RequestBody.create(MediaType.parse("*/*"), file);
        MultipartBody.Part fileAsMultipart = MultipartBody.Part.createFormData("file", file.getName(), fileAsRequestBody);

        return apiService.addFile(
                "Token " + PreferenceUtility.getToken(context),
                fileAsMultipart,
                args[1]);
    }

    private File handleImageCompression(String[] args, File file) {
        if (Attachment.isImage(args[0])) {
            int compressionRate = Integer.parseInt(PreferenceUtility.getString(context, SettingsFragment.COMPRESS_RATE_KEY));

            // -1 means user doesn't want to compress at all
            if (compressionRate != -1) {
                try {
                    file = new Compressor(context)
                            .setQuality(compressionRate)
                            .setCompressFormat(Bitmap.CompressFormat.WEBP)
                            .compressToFile(file);

                } catch (Exception e) {
                    // Compression didn't work
                    return new File(args[0]);
                }
            }
        }
        return file;
    }


    @Override
    protected void handleMainSuccessfulResponse(String stringPayload) {
        // We are not using the Volley library so we need to directly call this method
        try {
            if (stringPayload == null) {
                mainContentRepoData.postError(new CustomException(context.getString(R.string.no_rights_fragment_attachments)));
            } else
                mainContentRepoData.postSuccess(new Attachment(new JSONObject(stringPayload), true));
        } catch (JSONException e) {
            mainContentRepoData.postError(e);
        }
    }

}
