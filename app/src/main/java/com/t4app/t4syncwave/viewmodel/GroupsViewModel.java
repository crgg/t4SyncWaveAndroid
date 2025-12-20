package com.t4app.t4syncwave.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class GroupsViewModel extends ViewModel {

    public enum Filter{
        MY_GROUPS,
        ALL_GROUPS
    }

    private final MutableLiveData<Filter> filter = new MutableLiveData<>(Filter.MY_GROUPS);

    public LiveData<Filter> getFilter(){
        return filter;
    }

    public void setFilter(Filter newFilter){
        filter.setValue(newFilter);
    }
}
