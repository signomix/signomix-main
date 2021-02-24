/**
* Copyright (C) Grzegorz Skorupa 2018.
* Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/
package com.signomix.out.gui;

import java.util.List;
import java.util.Map;
import org.cricketmsf.microsite.out.auth.AuthAdapterIface;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public interface DashboardAdapterIface {
    public void addDashboard(String userID, Dashboard dashboard, AuthAdapterIface authAdapter) throws DashboardException;
    public void modifyDashboard(String userID, Dashboard dashboard, AuthAdapterIface authAdapter) throws DashboardException;
    public void removeDashboard(String userID, String dashboardID) throws DashboardException;
    public void removeUserDashboards(String userID) throws DashboardException;
    public Dashboard getDashboard(String userId, String dashboardID) throws DashboardException;
    public Dashboard getDashboardByName(String userId, String dashboardName) throws DashboardException;
    public List<Dashboard> getUserDashboards(String userID) throws DashboardException;
    public Map<String,Dashboard> getUserDashboardsMap(String userID) throws DashboardException;
    public boolean isAuthorized(String userIF, String dashboardID) throws DashboardException;
}
