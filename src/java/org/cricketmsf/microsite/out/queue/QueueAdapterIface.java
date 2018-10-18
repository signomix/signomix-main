/**
* Copyright (C) Grzegorz Skorupa 2018.
* Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/
package org.cricketmsf.microsite.out.queue;

import java.util.List;
import org.cricketmsf.Event;

/**
 * 
 * @author greg
 */
public interface QueueAdapterIface {
    public void init(String helperName, String categoriesToHandle, String categoriesToIgnore) throws QueueException;
    public boolean isHandling(String eventCategoryName);
    public List<Event> get(String path) throws QueueException;
    public void send(String path, Event event) throws QueueException;
    public void send(Event event) throws QueueException;
}
