/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sk.drndos.keycloak.crowd.storage;

import com.atlassian.crowd.model.user.User;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.jboss.logging.Logger;
import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.adapter.AbstractUserAdapterFederatedStorage;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class UserAdapter extends AbstractUserAdapterFederatedStorage {

  private static final Logger logger = Logger.getLogger(UserAdapter.class);
  private User entity;
  private String keycloakId;

  private Map<String, Function<User, String>> attributeFunctions = new HashMap<String, Function<User, String>>() {{
    put("firstName", User::getFirstName);
    put("lastName", User::getLastName);
    put("displayName", User::getDisplayName);
  }};

  public UserAdapter(KeycloakSession session, RealmModel realm, ComponentModel model, User entity) {
    super(session, realm, model);
    this.entity = entity;
    keycloakId = StorageId.keycloakId(model, entity.getExternalId());
  }

  @Override
  public String getUsername() {
    return entity.getName();
  }

  @Override
  public void setUsername(String username) {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public void setEmail(String email) {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public String getEmail() {
    return entity.getEmailAddress();
  }

  @Override
  public String getId() {
    return keycloakId;
  }

  @Override
  public void setSingleAttribute(String name, String value) {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public void removeAttribute(String name) {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public void setAttribute(String name, List<String> values) {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public String getFirstAttribute(String name) {
    return attributeFunctions.getOrDefault(name, key -> super.getFirstAttribute(name)).apply(entity);
  }

  @Override
  public Map<String, List<String>> getAttributes() {
    Map<String, List<String>> attrs = super.getAttributes();
    MultivaluedHashMap<String, String> all = new MultivaluedHashMap<>();
    all.putAll(attrs);
    attributeFunctions.forEach((key, value) -> all.add(key, value.apply(entity)));
    return all;
  }

  @Override
  public List<String> getAttribute(String name) {
    if (attributeFunctions.containsKey(name)) {
      List<String> listOfAttribute = new LinkedList<>();
      listOfAttribute.add(attributeFunctions.get(name).apply(entity));
      return listOfAttribute;
    } else {
      return super.getAttribute(name);
    }
  }
}
