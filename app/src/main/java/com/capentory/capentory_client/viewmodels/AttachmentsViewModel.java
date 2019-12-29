package com.capentory.capentory_client.viewmodels;

import com.capentory.capentory_client.models.Attachment;
import com.capentory.capentory_client.repos.AttachmentsRepository;

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