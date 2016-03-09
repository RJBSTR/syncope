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
package org.apache.syncope.client.console.panels;

import java.io.Serializable;
import java.util.List;
import org.apache.syncope.client.console.commons.Constants;
import org.apache.syncope.client.console.wicket.markup.html.bootstrap.dialog.BaseModal;
import org.apache.syncope.common.lib.to.AnyObjectTO;
import org.apache.wicket.PageReference;

/**
 * Modal window with Display any attributes form.
 *
 * @param <T> can be {@link org.apache.syncope.common.lib.to.AnyTO} or
 * {@link org.apache.syncope.client.console.wizards.any.AnyHandler}
 */
public class AnyObjectDisplayAttributesModalPage<T extends Serializable> extends DisplayAttributesModalPanel<T> {

    private static final long serialVersionUID = 5194630813773543054L;

    public static final String[] ANY_OBJECT_DEFAULT_SELECTION = { "key" };

    public AnyObjectDisplayAttributesModalPage(
            final BaseModal<T> modal,
            final PageReference pageRef,
            final List<String> schemaNames,
            final List<String> dSchemaNames,
            final String type) {

        super(modal, pageRef, schemaNames, dSchemaNames, type);
    }

    @Override
    public String getPrefDetailView() {
        return String.format(Constants.PREF_ANY_OBJECT_DETAILS_VIEW, type);
    }

    @Override
    public String getPrefAttributeView() {
        return String.format(Constants.PREF_ANY_OBJECT_PLAIN_ATTRS_VIEW, type);
    }

    @Override
    public String getPrefDerivedAttributeView() {
        return String.format(Constants.PREF_ANY_OBJECT_DER_ATTRS_VIEW, type);
    }

    @Override
    public Class<AnyObjectTO> getTOClass() {
        return AnyObjectTO.class;
    }
}