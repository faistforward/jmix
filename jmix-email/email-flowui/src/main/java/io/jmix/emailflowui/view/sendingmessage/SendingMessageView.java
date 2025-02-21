/*
 * Copyright 2023 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.jmix.emailflowui.view.sendingmessage;

import com.google.common.base.Strings;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.router.Route;
import io.jmix.core.*;
import io.jmix.email.EmailDataProvider;
import io.jmix.email.EmailerProperties;
import io.jmix.email.entity.SendingAttachment;
import io.jmix.email.entity.SendingMessage;
import io.jmix.emailflowui.view.sendingmessage.attachments.SendingMessageAttachmentsListView;
import io.jmix.emailflowui.view.sendingmessage.resend.ResendMessageView;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.UiProperties;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.textarea.JmixTextArea;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.download.ByteArrayDownloadDataProvider;
import io.jmix.flowui.download.DownloadFormat;
import io.jmix.flowui.download.Downloader;
import io.jmix.flowui.download.FileRefDownloadDataProvider;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.upload.TemporaryStorage;
import io.jmix.flowui.view.*;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@Route(value = "email/sendingmessage", layout = DefaultMainViewParent.class)
@ViewController("email_sendingMessageView")
@ViewDescriptor("sending-message-view.xml")
@DialogMode(width = "80em", height = "65em", resizable = true)
public class SendingMessageView extends StandardView {

    private static final Logger log = LoggerFactory.getLogger(SendingMessageView.class);

    @ViewComponent
    protected DataGrid<SendingMessage> sendingMessageDataGrid;
    @ViewComponent
    protected JmixTextArea contentTextArea;
    @ViewComponent
    protected TypedTextField<String> bodyContentTypeField;
    @ViewComponent
    protected JmixButton showContentBtn;
    @ViewComponent
    protected MessageBundle messageBundle;

    @Autowired
    protected EmailerProperties emailerProperties;
    @Autowired
    protected UiProperties uiProperties;
    @Autowired
    protected CoreProperties coreProperties;

    @Autowired
    protected EmailDataProvider emailDataProvider;
    @Autowired
    protected FileStorageLocator fileStorageLocator;
    @Autowired
    protected TemporaryStorage temporaryStorage;

    @Autowired
    protected DialogWindows dialogWindows;
    @Autowired
    protected Notifications notifications;
    @Autowired
    protected DataManager dataManager;
    @Autowired
    protected Downloader downloader;

    @Subscribe(id = "sendingMessagesDc", target = Target.DATA_CONTAINER)
    public void onSendingMessagesDcItemChange(CollectionContainer.ItemChangeEvent<SendingMessage> event) {
        SendingMessage selectedItem = event.getItem();

        if (selectedItem != null
                && Strings.isNullOrEmpty(selectedItem.getContentText())
                && selectedItem.getContentTextFile() != null) {
            selectedItem.setContentText(readContentTextFile(selectedItem));
        }

        showContentBtn.setEnabled(selectedItem != null && !Strings.isNullOrEmpty(selectedItem.getContentText()));
    }

    @Subscribe("showContentBtn")
    public void onShowContentBtnClick(ClickEvent<JmixButton> event) {
        ByteArrayDownloadDataProvider dataProvider = new ByteArrayDownloadDataProvider(
                contentTextArea.getValue().getBytes(StandardCharsets.UTF_8),
                uiProperties.getSaveExportedByteArrayDataThresholdBytes(),
                coreProperties.getTempDir()
        );

        if (StringUtils.containsIgnoreCase(bodyContentTypeField.getValue(), DownloadFormat.HTML.getContentType())) {
            downloader.download(dataProvider, "email-preview.html", DownloadFormat.HTML);
        } else {
            downloader.download(dataProvider, "email-preview.txt", DownloadFormat.TEXT);
        }
    }

    @Subscribe("sendingMessageDataGrid.downloadAttachment")
    public void onSendingMessageDownloadAttachment(ActionPerformedEvent event) {
        SendingMessage selectedMessage = sendingMessageDataGrid.getSingleSelectedItem();

        if (selectedMessage != null) {
            List<SendingAttachment> attachments = getAttachments(selectedMessage);

            if (attachments.size() == 1) {
                downloadFile(attachments.get(0));
            } else if (attachments.size() > 1) {
                openAttachmentLookupDialog(selectedMessage);
            } else {
                notifications.create(messageBundle.getMessage("sendingMessageView.noAttachmentsNotification.header"))
                        .withType(Notifications.Type.WARNING)
                        .show();
            }
        }
    }

    @Subscribe("sendingMessageDataGrid.resendEmail")
    public void onSendingMessageResendEmail(ActionPerformedEvent event) {
        SendingMessage selectedMessage = sendingMessageDataGrid.getSingleSelectedItem();

        if (selectedMessage != null) {
            DialogWindow<ResendMessageView> resendMessageViewDialogWindow = dialogWindows
                    .view(this, ResendMessageView.class)
                    .build();

            resendMessageViewDialogWindow.getView().setMessage(selectedMessage);

            resendMessageViewDialogWindow.open();
        }
    }

    protected void openAttachmentLookupDialog(SendingMessage message) {
        DialogWindow<SendingMessageAttachmentsListView> messageAttachmentsLookupViewDialogWindow = dialogWindows
                .lookup(this, SendingAttachment.class)
                .withViewClass(SendingMessageAttachmentsListView.class)
                .withSelectHandler(attachments -> {
                    if (attachments.size() == 1) {
                        downloadFile(IterableUtils.get(attachments, 0));
                    }
                })
                .build();

        messageAttachmentsLookupViewDialogWindow.getView().setMessage(message);

        messageAttachmentsLookupViewDialogWindow.open();
    }

    protected void downloadFile(SendingAttachment attachment) {
        FileStorage fileStorage = fileStorageLocator.getDefault();

        FileRef fileRef;

        if (emailerProperties.isUseFileStorage()
                && attachment.getContentFile() != null
                && fileStorage.fileExists(attachment.getContentFile())) {
            fileRef = attachment.getContentFile();
        } else {
            fileRef = getNewReference(attachment);
        }

        downloader.download(new FileRefDownloadDataProvider(fileRef, fileStorage),
                attachment.getName(),
                DownloadFormat.OCTET_STREAM
        );
    }

    protected List<SendingAttachment> getAttachments(SendingMessage message) {
        return dataManager.load(SendingMessage.class)
                .id(message.getId())
                .fetchPlan("sendingMessage.loadFromQueue")
                .one()
                .getAttachments();
    }

    protected FileRef getNewReference(SendingAttachment attachment) {
        UUID uuid = temporaryStorage.saveFile(attachment.getContent());
        return temporaryStorage.putFileIntoStorage(uuid, attachment.getName());
    }

    @Nullable
    protected String readContentTextFile(SendingMessage sendingMessage) {
        FileRef fileRef = sendingMessage.getContentTextFile();
        if (fileRef == null) {
            return null;
        }
        FileStorage fileStorage = fileStorageLocator.getDefault();
        if (fileStorage.fileExists(fileRef)) {
            try (InputStream inputStream = fileStorage.openStream(fileRef)) {
                return IOUtils.toString(inputStream, Charset.defaultCharset());
            } catch (IOException e) {
                log.warn("Cannot read message content from file.", e);
            }
        }
        return null;
    }
}