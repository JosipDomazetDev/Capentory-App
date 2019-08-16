package com.example.capentory_client;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import android.os.AsyncTask;

import org.json.JSONObject;

import java.util.List;

public class RoomFragmentViewModel extends ViewModel {
    private MutableLiveData<List<JSONObject>> rooms;
    private RalphRepository ralphRepository;
    private MutableLiveData<Boolean> mIsUpdating = new MutableLiveData<>();

    public void init(){
        if(rooms != null){
            return;
        }
        ralphRepository = RalphRepository.getInstance();
        rooms = ralphRepository.getRooms();
    }

   /* public void addNewValue(final NicePlace nicePlace){
        mIsUpdating.setValue(true);

        new AsyncTask<Void, Void, Void>(){
            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                List<JSONObject> currentPlaces = rooms.getValue();
                currentPlaces.add(nicePlace);
                rooms.postValue(currentPlaces);
                mIsUpdating.postValue(false);
            }

            @Override
            protected Void doInBackground(Void... voids) {

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute();
    }*/

    public LiveData<List<JSONObject>> getRooms(){
        return rooms;
    }


    public LiveData<Boolean> getIsUpdating(){
        return mIsUpdating;
    }

}
