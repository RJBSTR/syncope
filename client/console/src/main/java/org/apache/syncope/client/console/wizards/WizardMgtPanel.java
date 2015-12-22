/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.syncope.client.console.wizards;

import java.io.Serializable;
import org.apache.syncope.client.console.commons.Constants;
import org.apache.syncope.client.console.panels.NotificationPanel;
import org.apache.syncope.client.console.wicket.markup.html.bootstrap.dialog.BaseModal;
import org.apache.wicket.PageReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.event.IEventSource;
import org.apache.wicket.markup.html.TransparentWebMarkupContainer;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

public abstract class WizardMgtPanel<T extends Serializable> extends Panel implements IEventSource {

    private static final long serialVersionUID = 1L;

    private final WebMarkupContainer container;

    private final Fragment initialFragment;

    private final AjaxLink<?> addAjaxLink;

    private AjaxWizardBuilder<T> newItemPanelBuilder;

    private NotificationPanel notificationPanel;

    private final PageReference pageRef;

    /**
     * Modal window.
     */
    protected final BaseModal<T> modal = new BaseModal<T>("modal") {

        private static final long serialVersionUID = 1L;

        @Override
        protected void onConfigure() {
            super.onConfigure();
            setFooterVisible(false);
        }

    };

    private final boolean wizardInModal;

    protected WizardMgtPanel(final String id, final PageReference pageRef) {
        this(id, pageRef, false);
    }

    protected WizardMgtPanel(final String id, final PageReference pageRef, final boolean wizardInModal) {
        super(id);
        setOutputMarkupId(true);
        this.pageRef = pageRef;
        this.wizardInModal = wizardInModal;

        add(modal);

        container = new TransparentWebMarkupContainer("container");
        container.setOutputMarkupPlaceholderTag(true).setOutputMarkupId(true);
        add(container);

        initialFragment = new Fragment("content", "default", this);
        container.addOrReplace(initialFragment);

        addAjaxLink = new AjaxLink<T>("add") {

            private static final long serialVersionUID = 1L;

            @Override
            public void onClick(final AjaxRequestTarget target) {
                send(WizardMgtPanel.this, Broadcast.EXACT, new AjaxWizard.NewItemActionEvent<T>(null, target));
            }
        };

        addAjaxLink.setEnabled(false);
        addAjaxLink.setVisible(false);
        initialFragment.add(addAjaxLink);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onEvent(final IEvent<?> event) {
        if (event.getPayload() instanceof AjaxWizard.NewItemEvent) {
            final AjaxWizard.NewItemEvent<T> newItemEvent = AjaxWizard.NewItemEvent.class.cast(event.getPayload());
            final AjaxRequestTarget target = newItemEvent.getTarget();
            final T item = newItemEvent.getItem();

            if (event.getPayload() instanceof AjaxWizard.NewItemActionEvent) {
                newItemPanelBuilder.setItem(item);

                final AjaxWizard<T> wizard = newItemPanelBuilder.build(
                        ((AjaxWizard.NewItemActionEvent<T>) newItemEvent).getIndex(), item != null);

                if (wizardInModal) {
                    final IModel<T> model = new CompoundPropertyModel<>(item);
                    modal.setFormModel(model);

                    target.add(modal.setContent(wizard));

                    modal.header(new StringResourceModel(
                            String.format("any.%s", newItemEvent.getEventDescription()),
                            this,
                            new Model<T>(wizard.getItem())));

                    modal.show(true);
                } else {
                    final Fragment fragment = new Fragment("content", "wizard", WizardMgtPanel.this);
                    fragment.add(wizard);
                    container.addOrReplace(fragment);
                }
            } else {
                if (event.getPayload() instanceof AjaxWizard.NewItemFinishEvent) {
                    if (notificationPanel != null) {
                        getSession().info(getString(Constants.OPERATION_SUCCEEDED));
                        notificationPanel.refresh(target);
                    }
                }

                if (wizardInModal) {
                    modal.show(false);
                    modal.close(target);
                } else {
                    container.addOrReplace(initialFragment);
                }
            }

            target.add(container);
        }
        super.onEvent(event);
    }

    private WizardMgtPanel<T> addNewItemPanelBuilder(final AjaxWizardBuilder<T> panelBuilder) {
        this.newItemPanelBuilder = panelBuilder;

        if (this.newItemPanelBuilder != null) {
            addAjaxLink.setEnabled(true);
            addAjaxLink.setVisible(true);
        }

        return this;
    }

    private WizardMgtPanel<T> addNotificationPanel(final NotificationPanel notificationPanel) {
        this.notificationPanel = notificationPanel;
        return this;
    }

    /**
     * PanelInWizard abstract builder.
     *
     * @param <T> list item reference type.
     */
    public abstract static class Builder<T extends Serializable> implements Serializable {

        private static final long serialVersionUID = 1L;

        protected final PageReference pageRef;

        private AjaxWizardBuilder<T> newItemPanelBuilder;

        private NotificationPanel notificationPanel;

        protected Builder(final PageReference pageRef) {
            this.pageRef = pageRef;
        }

        protected abstract WizardMgtPanel<T> newInstance(final String id);

        /**
         * Builds a list view.
         *
         * @param id component id.
         * @return List view.
         */
        public WizardMgtPanel<T> build(final String id) {
            return newInstance(id).addNewItemPanelBuilder(newItemPanelBuilder).addNotificationPanel(notificationPanel);
        }

        public Builder<T> addNewItemPanelBuilder(final AjaxWizardBuilder<T> panelBuilder) {
            this.newItemPanelBuilder = panelBuilder;
            return this;
        }

        public Builder<T> addNotificationPanel(final NotificationPanel notificationPanel) {
            this.notificationPanel = notificationPanel;
            return this;
        }
    }
}