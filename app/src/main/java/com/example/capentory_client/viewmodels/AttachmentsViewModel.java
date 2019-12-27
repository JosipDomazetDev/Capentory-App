package com.example.capentory_client.viewmodels;

import com.example.capentory_client.models.Attachment;
import com.example.capentory_client.models.Room;
import com.example.capentory_client.repos.AttachmentsRepository;
import com.example.capentory_client.repos.RoomsRepository;

import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

public class AttachmentsViewModel extends NetworkViewModel<Attachment, AttachmentsRepository> {

    @Inject
    public AttachmentsViewModel(AttachmentsRepository attachmentsRepository) {
        super(attachmentsRepository);
    }


    @Override
    public void fetchData(String... args) {
        statusAwareLiveData = networkRepository.fetchMainData(args);
    }

    @Override
    public void reloadData(String... args) {
        statusAwareLiveData = networkRepository.fetchMainData(args);
    }

}