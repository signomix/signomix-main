/*
* Copyright (C) Grzegorz Skorupa 2020.
* Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */
package com.signomix.mailing;

/**
 *
 * @author greg
 */
public class Mailing {

    public final static int SCHEDULED = 0;
    public final static int SENT = 1;
    public final static int CANCELLED = 2;

    private long id;
    private String title;
    private String documentUID;
    private String dateDefinition;
    private boolean toAll;
    private int status;
    private String recipents;
    private String documenTitle;
    private String documentContent;

    /**
     * @return the id
     */
    public long getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return the documentUID
     */
    public String getDocumentUID() {
        return documentUID;
    }

    /**
     * @param documentUID the documentUID to set
     */
    public void setDocumentUID(String documentUID) {
        this.documentUID = documentUID;
    }

    /**
     * @return the dateDefinition
     */
    public String getDateDefinition() {
        return dateDefinition;
    }

    /**
     * @param dateDefinition the dateDefinition to set
     */
    public void setDateDefinition(String dateDefinition) {
        this.dateDefinition = dateDefinition;
    }

    /**
     * @return the toAll
     */
    public boolean isToAll() {
        return toAll;
    }

    /**
     * @param toAll the toAll to set
     */
    public void setToAll(boolean toAll) {
        this.toAll = toAll;
    }

    /**
     * @return the status
     */
    public int getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(int status) {
        this.status = status;
    }

    /**
     * @return the recipents
     */
    public String getRecipents() {
        return recipents;
    }

    /**
     * @param recipents the recipents to set
     */
    public void setRecipents(String recipents) {
        this.recipents = recipents;
    }

    /**
     * @return the documenTitle
     */
    public String getDocumenTitle() {
        return documenTitle;
    }

    /**
     * @param documenTitle the documenTitle to set
     */
    public void setDocumenTitle(String documenTitle) {
        this.documenTitle = documenTitle;
    }

    /**
     * @return the documentContent
     */
    public String getDocumentContent() {
        return documentContent;
    }

    /**
     * @param documentContent the documentContent to set
     */
    public void setDocumentContent(String documentContent) {
        this.documentContent = documentContent;
    }

}
