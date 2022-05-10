package org.cricketmsf.microsite.user;

import java.util.Map;

import org.cricketmsf.microsite.out.user.UserException;

public interface OrganizationAdapterIface {
    public Organization getOrganization(long id) throws UserException;
    public Map getAllOrganizations() throws UserException;
    public Organization createOrganization(Organization organization) throws UserException;
    public void removeOrganization(Organization organization) throws UserException;
    public void modifyOrganization(Organization organization) throws UserException;
}
