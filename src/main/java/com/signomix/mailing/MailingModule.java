/*
* Copyright (C) Grzegorz Skorupa 2020.
* Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/
package com.signomix.mailing;

import org.cricketmsf.Event;
import org.cricketmsf.Kernel;
import org.cricketmsf.microsite.cms.Document;

/**
 * TODO: Use this class as a target solution for mailing 
 * @author greg
 */
public class MailingModule {
    
    private static MailingModule self;

    public static MailingModule getInstance() {
        if (self == null) {
            self = new MailingModule();
        }
        return self;
    }    
    
    public Mailing getMailing(long id){
        return null;
    }
    
    /**
     * Przygotowuje wysyłkę mailingu
    */
    public void defineMailing(String docUID, String sendingDate, boolean allUsers){
        Mailing mailing = new Mailing();
        mailing.setDocumentUID(docUID);
        mailing.setToAll(allUsers);
        //TODO
        Event ev=new Event(this.getClass().getSimpleName(), Event.CATEGORY_GENERIC, "MAILING_SEND", sendingDate, mailing.getId());
        Kernel.getInstance().dispatchEvent(ev);
    }
    
    /**
     * 
     * @param id 
     */
    public void sendMailing(long id){
        Mailing mailing = getMailing(id);
        if(null==mailing || mailing.getStatus()!=Mailing.SCHEDULED){
            return;
        }
        Document doc=null;
        if(null==doc){
            return;
        }
        mailing.setTitle(doc.getTitle());
        mailing.setDocumentContent(doc.getContent());
        // TODO: send
        mailing.setStatus(Mailing.SENT);
        // TODO: save
    }
    
    public void removeMailing(long id){
        
    }
}
