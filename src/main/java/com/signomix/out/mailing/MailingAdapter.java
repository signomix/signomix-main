/**
 * Copyright (C) Grzegorz Skorupa 2020.
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */
package com.signomix.out.mailing;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.cricketmsf.Adapter;
import org.cricketmsf.in.http.ResponseCode;
import org.cricketmsf.in.http.StandardResult;
import org.cricketmsf.microsite.cms.CmsException;
import org.cricketmsf.microsite.cms.CmsIface;
import org.cricketmsf.microsite.cms.Document;
import org.cricketmsf.microsite.out.user.UserAdapterIface;
import org.cricketmsf.microsite.out.user.UserException;
import org.cricketmsf.microsite.user.User;
import org.cricketmsf.out.OutboundAdapter;
import org.slf4j.LoggerFactory;

import com.signomix.out.notification.MessageBrokerIface;
import com.signomix.out.notification.dto.MessageEnvelope;

/**
 *
 * @author greg
 */
public class MailingAdapter extends OutboundAdapter implements MailingIface, Adapter {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(MailingAdapter.class);
    private static final String MAILING_TESTER_ROLE_NAME = "mailingtester";
    private static final String REPORT_LANGUAGE = "en";

    private String reportPath;
    private String reportLanguage;
    private String reportTesterRoleName;
    private String welcomeDocId;

    @Override
    public void loadProperties(HashMap<String, String> properties, String adapterName) {
        super.loadProperties(properties, adapterName);
        reportPath = (String) properties.get("reports-path");
        logger.info("\treports-path: " + reportPath);
        reportLanguage = (String) properties.getOrDefault("reports-language", REPORT_LANGUAGE);
        logger.info("\treports-language: " + reportLanguage);
        reportTesterRoleName = (String) properties.getOrDefault("tester-role-name", MAILING_TESTER_ROLE_NAME);
        logger.info("\ttester-role-name: " + reportTesterRoleName);
        welcomeDocId = (String) properties.get("welcome-document-id");
        logger.info("\twelcome-document-id: " + welcomeDocId);
    }

    @Override
    public Object sendMailing(
            String docUid,
            String target,
            UserAdapterIface userAdapter,
            CmsIface cmsAdapter,
            MessageBrokerIface externalNotificator) {
                StandardResult result = new StandardResult();
                User userStub=new User();
                userStub.setRole(target);
                send(userStub, docUid, externalNotificator);
                return result;
            }

    /* @Override
    public Object sendMailing(
            String docUid,
            String target,
            UserAdapterIface userAdapter,
            CmsIface cmsAdapter,
            MessageBrokerIface externalNotificator) {
        try {
            ArrayList<String> recipients = new ArrayList<>();
            ArrayList<String> failures = new ArrayList<>();
            StandardResult result = new StandardResult();
            Document documentPl = null;
            Document documentEn = null;
            try {
                documentPl = cmsAdapter.getDocument(docUid, "pl", "published", null);
                documentEn = cmsAdapter.getDocument(docUid, "en", "published", null);
            } catch (CmsException ex) {
                logger.warn(ex.getMessage());
            }
            if (null == documentPl && null == documentEn) {
                System.out.println("DOCUMENT NOT FOUND");
                result.setCode(ResponseCode.NOT_FOUND);
                result.setMessage("document not found");
                result.setPayload("document not found".getBytes());
                return result;
            }
            Iterator it;
            User user;
            boolean accepted = false;
            try {
                it = userAdapter.getAll().values().iterator();
                while (it.hasNext()) {
                    user = (User) it.next();
                    switch (target) {
                        case "test":
                            accepted = user.hasRole(MAILING_TESTER_ROLE_NAME);
                            break;
                        case "users":
                            accepted = user.getType() != User.SUBSCRIBER;
                            break;
                        case "subscribers":
                            accepted = user.getType() == User.SUBSCRIBER;
                            break;
                        case "all":
                            accepted = true;
                            break;
                        default:
                            accepted = false;
                    }
                    if (accepted && null != user.getEmail() && !user.getEmail().isBlank()) {
                        if ("pl".equalsIgnoreCase(user.getPreferredLanguage()) && null != documentPl) {
                            if (send(user, documentPl, externalNotificator)) {
                                recipients.add(user.getEmail());
                            } else {
                                failures.add(user.getEmail());
                            }
                        } else if (null != documentEn) {
                            if (send(user, documentEn, externalNotificator)) {
                                recipients.add(user.getEmail());
                            } else {
                                failures.add(user.getEmail());
                            }
                        }
                    }
                }
                StringBuffer sb = new StringBuffer("success:\r\n");
                for (int i = 0; i < recipients.size(); i++) {
                    sb.append(recipients.get(i)).append(";");
                }
                sb.append("\r\nfailure:\r\n");
                for (int i = 0; i < failures.size(); i++) {
                    sb.append(failures.get(i)).append(";");
                }
                sb.append("\r\n");
                saveReport(cmsAdapter, docUid, target, sb.toString());
            } catch (UserException ex) {
                logger.warn(ex.getMessage());
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
 */
    @Override
    public Object sendWelcomeDocument(
            User user,
            CmsIface cmsAdapter,
            MessageBrokerIface externalNotificator) {
        try {
            StandardResult result = new StandardResult();
            Document documentPl = null;
            Document documentEn = null;
            try {
                documentPl = cmsAdapter.getDocument(welcomeDocId, "pl", "published", null);
                documentEn = cmsAdapter.getDocument(welcomeDocId, "en", "published", null);
            } catch (CmsException ex) {
                logger.warn(ex.getMessage());
            }
            if (null == documentPl && null == documentEn) {
                result.setCode(ResponseCode.NOT_FOUND);
                result.setMessage("document not found");
                result.setPayload("document not found".getBytes());
                return result;
            }
            if (null == user.getEmail() || user.getEmail().isBlank()) {
                result.setCode(ResponseCode.NOT_FOUND);
                result.setMessage("e-mail not set");
                result.setPayload("e-mail not set".getBytes());
                return result;
            }
            if ("pl".equalsIgnoreCase(user.getPreferredLanguage()) && null != documentPl) {
                sendDocument(user, documentPl, externalNotificator);
            } else if (null != documentEn) {
                sendDocument(user, documentEn, externalNotificator);
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void saveReport(CmsIface cmsAdapter, String docUid, String target, String reportContent) {
        if (null == reportPath) {
            logger.info("report path not configured");
            return;
        }
        try {
            ArrayList<String> roles = new ArrayList<>();
            roles.add("redactor");
            Document report = new Document();
            long timestamp = System.currentTimeMillis();
            report.setUid(reportPath + "/" + timestamp);
            report.setLanguage("en");
            report.setMimeType("text/plain");
            report.setTags("");
            report.setType(Document.ARTICLE);
            report.setTitle("mailing report " + timestamp);
            report.setSummary("");
            report.setContent(reportContent);
            report.setExtra(docUid + ";" + target);
            cmsAdapter.addDocument(report, roles);
        } catch (CmsException ex) {
            logger.warn(ex.getMessage());
        }
    }

    private boolean send(User userStub, String docUid, MessageBrokerIface externalNotificator) {
        if (null == externalNotificator) {
            return false;
        }
        MessageEnvelope envelope = new MessageEnvelope();
        envelope.subject = "";
        envelope.message = docUid;
        envelope.user = userStub;
        envelope.type = MessageEnvelope.NEXTMAILING;
        externalNotificator.send(envelope);
        return true;
    }

    /* private boolean send(User user, Document doc, MessageBrokerIface externalNotificator) {
        if (null == externalNotificator) {
            return false;
        }
        String topic;
        String content;
        try {
            topic = URLDecoder.decode(doc.getTitle(), "UTF-8");
            content = URLDecoder.decode(doc.getContent(), "UTF-8");

            content = content.replaceFirst("\\$user.name", user.getName());
            content = content.replaceFirst("\\$mailing.name", user.getSurname());
            content = content.replaceFirst("\\$user.uid", user.getUid());
            User userStub = new User();
            userStub.setEmail(user.getEmail());
            MessageEnvelope envelope = new MessageEnvelope();
            envelope.subject = topic;
            envelope.message = content;
            envelope.user = userStub;
            envelope.type = MessageEnvelope.MAILING;
            externalNotificator.send(envelope);
            return true;
        } catch (UnsupportedEncodingException ex) {
            logger.warn(ex.getMessage());
            return false;
        }
    } */

    private boolean sendDocument(User user, Document doc, MessageBrokerIface externalNotificator) {
        if (null == externalNotificator) {
            return false;
        }
        String topic;
        String content;
        try {
            topic = URLDecoder.decode(doc.getTitle(), "UTF-8");
            content = URLDecoder.decode(doc.getContent(), "UTF-8");

            content = content.replaceFirst("\\$user.name", user.getName());
            content = content.replaceFirst("\\$user.uid", user.getUid());
            User userStub = new User();
            userStub.setEmail(user.getEmail());
            MessageEnvelope envelope = new MessageEnvelope();
            envelope.subject = topic;
            envelope.message = content;
            envelope.user = userStub;
            envelope.type = MessageEnvelope.DIRECT_EMAIL;
            externalNotificator.send(envelope);
            return true;
        } catch (UnsupportedEncodingException ex) {
            logger.warn(ex.getMessage());
            return false;
        }
    }

}
