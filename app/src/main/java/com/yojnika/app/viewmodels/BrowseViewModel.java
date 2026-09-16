package com.yojnika.app.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.yojnika.app.models.Scheme;
import com.yojnika.app.repository.SchemeRepository;

import java.util.ArrayList;
import java.util.List;

public class BrowseViewModel extends AndroidViewModel {

    private final SchemeRepository repository;
    private final MutableLiveData<List<Scheme>> pagedSchemesLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoadingLiveData = new MutableLiveData<>(false);

    private int currentPage = 1;
    private static final int PAGE_SIZE = 20;

    private String searchQuery = "";
    private String stateFilter = "All India";
    private String typeFilter = "All Types";
    private String categoryFilter = "All";

    public BrowseViewModel(@NonNull Application application) {
        super(application);
        repository = SchemeRepository.getInstance(application);
    }

    public LiveData<List<Scheme>> getPagedSchemes() {
        return pagedSchemesLiveData;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoadingLiveData;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCategoryFilter(String category) {
        this.categoryFilter = category != null ? category : "All";
        this.currentPage = 1;
        loadSchemes();
    }

    public void setFilters(String query, String state, String type, String category) {
        this.searchQuery = query != null ? query : "";
        this.stateFilter = state != null ? state : "All India";
        this.typeFilter = type != null ? type : "All Types";
        this.categoryFilter = category != null ? category : "All";
        this.currentPage = 1;
        loadSchemes();
    }

    public void loadSchemes() {
        isLoadingLiveData.setValue(true);
        repository.searchAndFilterSchemesPaged(currentPage, PAGE_SIZE, searchQuery, stateFilter, typeFilter, categoryFilter, schemes -> {
            pagedSchemesLiveData.postValue(schemes != null ? schemes : new ArrayList<>());
            isLoadingLiveData.postValue(false);
        });
    }

    public void nextPage() {
        currentPage++;
        loadSchemes();
    }

    public void previousPage() {
        if (currentPage > 1) {
            currentPage--;
            loadSchemes();
        }
    }
}
