package com.signomix;

import org.cricketmsf.Event;
import org.cricketmsf.microsite.out.user.UserAdapterIface;

import com.signomix.out.iot.ThingsDataIface;

public interface DeviceManagementLogicIface {

    public Object processDeviceEvent(Event event, ThingsDataIface thingsAdapter, UserAdapterIface users, PlatformAdministrationModule platform);
    public Object processTemplateEvent(Event event, ThingsDataIface thingsAdapter, UserAdapterIface users, PlatformAdministrationModule platform);
    public Object processGroupEvent(Event event, ThingsDataIface thingsAdapter, UserAdapterIface users, PlatformAdministrationModule platform);
    public Object processGroupPublicationEvent(Event event, ThingsDataIface thingsAdapter, UserAdapterIface users, PlatformAdministrationModule platform);
    public void removeUserData(String userId, ThingsDataIface thingsAdapter);
    public void checkStatus(ThingsDataIface thingsAdapter);

}
