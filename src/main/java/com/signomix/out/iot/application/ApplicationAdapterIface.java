package com.signomix.out.iot.application;

import java.util.List;

import com.signomix.out.iot.ThingsDataException;

public interface ApplicationAdapterIface {
    public Application getApplication(long id) throws ThingsDataException;
    public List<Application> getAllApplications() throws ThingsDataException;
    public Application createApplication(Application application) throws ThingsDataException;
    public void removeApplication(long applicationId) throws ThingsDataException;
    public void modifyApplication(Application application) throws ThingsDataException;
}
