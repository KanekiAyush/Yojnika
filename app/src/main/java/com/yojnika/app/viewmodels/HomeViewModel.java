package com.yojnika.app.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.yojnika.app.models.Scheme;
import com.yojnika.app.models.UserProfile;
import com.yojnika.app.repository.SchemeRepository;

import java.util.ArrayList;
import java.util.List;

public class HomeViewModel extends AndroidViewModel {

    private final SchemeRepository repository;
    private final MutableLiveData<List<Scheme>> matchedSchemesLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoadingLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isProfileCompleteLiveData = new MutableLiveData<>(false);

    private int currentPage = 1;
    private static final int PAGE_SIZE = 20;
    private List<Scheme> allMatchedSchemes = new ArrayList<>();

    public HomeViewModel(@NonNull Application application) {
        super(application);
        repository = SchemeRepository.getInstance(application);
    }

    public LiveData<List<Scheme>> getMatchedSchemes() {
        return matchedSchemesLiveData;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoadingLiveData;
    }

    public LiveData<Boolean> getIsProfileComplete() {
        return isProfileCompleteLiveData;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public boolean hasNextPage() {
        return currentPage * PAGE_SIZE < allMatchedSchemes.size();
    }

    public boolean hasPreviousPage() {
        return currentPage > 1;
    }

    public void loadRecommendations() {
        UserProfile profile = repository.getUserProfile();
        boolean isComplete = profile != null && profile.isComplete();
        isProfileCompleteLiveData.setValue(isComplete);

        if (!isComplete) {
            matchedSchemesLiveData.setValue(new ArrayList<>());
            return;
        }

        isLoadingLiveData.setValue(true);
        repository.getMatchedSchemes(profile, schemes -> {
            allMatchedSchemes = schemes != null ? schemes : new ArrayList<>();
            currentPage = 1;
            updatePagedList();
            isLoadingLiveData.postValue(false);
        });
    }

    public void nextPage() {
        if (hasNextPage()) {
            currentPage++;
            updatePagedList();
        }
    }

    public void previousPage() {
        if (hasPreviousPage()) {
            currentPage--;
            updatePagedList();
        }
    }

    private void updatePagedList() {
        if (allMatchedSchemes.isEmpty()) {
            matchedSchemesLiveData.postValue(new ArrayList<>());
            return;
        }

        int start = (currentPage - 1) * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, allMatchedSchemes.size());

        if (start < allMatchedSchemes.size()) {
            List<Scheme> pageList = new ArrayList<>(allMatchedSchemes.subList(start, end));
            matchedSchemesLiveData.postValue(pageList);
        } else {
            matchedSchemesLiveData.postValue(new ArrayList<>());
        }
    }
}
