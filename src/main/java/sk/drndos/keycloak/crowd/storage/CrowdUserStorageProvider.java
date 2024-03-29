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
import com.atlassian.crowd.search.query.entity.restriction.MatchMode;
import com.atlassian.crowd.search.query.entity.restriction.NullRestriction;
import com.atlassian.crowd.search.query.entity.restriction.Property;
import com.atlassian.crowd.search.query.entity.restriction.PropertyImpl;
import com.atlassian.crowd.search.query.entity.restriction.PropertyRestriction;
import com.atlassian.crowd.service.client.CrowdClient;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.component.ComponentModel;
import org.keycloak.credential.CredentialInput;
import org.keycloak.credential.CredentialInputUpdater;
import org.keycloak.credential.CredentialInputValidator;
import org.keycloak.models.GroupModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserCredentialModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.UserStorageProvider;
import org.keycloak.storage.user.UserLookupProvider;
import org.keycloak.storage.user.UserQueryProvider;
import org.keycloak.storage.user.UserRegistrationProvider;

@JBossLog
public class CrowdUserStorageProvider implements
    UserStorageProvider,
    UserLookupProvider,
    CredentialInputValidator,
    CredentialInputUpdater,
    UserRegistrationProvider,
    UserQueryProvider {

  public static final String UNSET_PASSWORD = "#$!-UNSET-PASSWORD";

  private final KeycloakSession session;
  private final CrowdClient client;
  private final ComponentModel model;
  // map of loaded users in this transaction
  protected Map<String, UserModel> loadedUsers = new HashMap<>();


  public CrowdUserStorageProvider(KeycloakSession session, ComponentModel model,
      CrowdClient client) {
    this.session = session;
    this.model = model;
    this.client = client;
  }

  // UserLookupProvider methods

  @Override
  public UserModel getUserByUsername(RealmModel realm, String username) {
    log.info("Getting user by username " + username);
    try {
      User user = client.getUser(username);
      return new UserAdapter(session, realm, model, user);
    } catch (Exception e) {
      log.info(e);
      return null;
    }
  }

  @Override
  public UserModel getUserById(RealmModel realm, String id) {
    log.info("Getting user by id " + id);
    StorageId storageId = new StorageId(id);
    String username = storageId.getExternalId();
    return getUserByUsername(realm, username);
  }

  @Override
  public UserModel getUserByEmail(RealmModel realm, String email) {
    throw new UnsupportedOperationException("Not supported by Crowd");
  }

  // UserQueryProvider methods

  @Override
  public int getUsersCount(RealmModel realm) {
    throw new UnsupportedOperationException("Not supported by Crowd");
  }

  @Override
  public Stream<UserModel> searchForUserStream(RealmModel realm, Map<String, String> params,
      Integer firstResult, Integer maxResults) {
    if (params.isEmpty()) {
      try {
        return client.searchUsers(new NullRestriction() {
            }, firstResult, maxResults)
            .stream()
            .map(user -> new UserAdapter(session, realm, model, user));
      } catch (Exception e) {
        log.info(e);
        return null;
      }
    }
    log.info("Unsupported search for user with params " + params);
    throw new UnsupportedOperationException("Not supported by Crowd");
  }

  @Override
  public Stream<UserModel> getGroupMembersStream(RealmModel realm, GroupModel group,
      Integer firstResult, Integer maxResults) {
    // runtime automatically handles querying UserFederatedStorage
    return Stream.empty();
  }

  @Override
  public Stream<UserModel> searchForUserByUserAttributeStream(RealmModel realm, String attrName,
      String attrValue) {
    // runtime automatically handles querying UserFederatedStorage
    return Stream.empty();
  }

  @Override
  public Stream<UserModel> searchForUserStream(RealmModel realm, String search, Integer firstResult,
      Integer maxResults) {
    try {
      return client.searchUsers(new PropertyRestriction<String>() {
            @Override
            public Property<String> getProperty() {
              return new PropertyImpl<>("name", String.class);
            }

            @Override
            public MatchMode getMatchMode() {
              return MatchMode.CONTAINS;
            }

            @Override
            public String getValue() {
              return search;
            }
          }, firstResult, maxResults)
          .stream()
          .map(user -> new UserAdapter(session, realm, model, user));

    } catch (Exception e) {
      log.info(e);
      return null;
    }
  }

  // UserRegistrationProvider method implementations

  @Override
  public UserModel addUser(RealmModel realm, String username) {
    throw new UnsupportedOperationException("Not supported by Crowd");
  }

  @Override
  public boolean removeUser(RealmModel realm, UserModel user) {
    throw new UnsupportedOperationException("Not supported by Crowd");
  }

  // CredentialInputValidator methods

  @Override
  public boolean isConfiguredFor(RealmModel realm, UserModel user, String credentialType) {
    log.info("Is user " + user.getUsername() + " configured for ? " + credentialType);
    return supportsCredentialType(credentialType);
  }

  @Override
  public boolean supportsCredentialType(String credentialType) {
    log.info("Does realm support ? " + credentialType);
    return credentialType.equals(PasswordCredentialModel.TYPE);
  }

  @Override
  public boolean isValid(RealmModel realm, UserModel user, CredentialInput input) {
    if (!supportsCredentialType(input.getType()) || !(input instanceof UserCredentialModel cred)) {
      return false;
    }

    log.info("Is user valid ? " + user.getUsername());
    try {
      User authenticatedUser = client.authenticateUser(user.getUsername(), cred.getValue());
      return authenticatedUser != null;
    } catch (Exception e) {
      log.info(e);
      return false;
    }
  }

  // CredentialInputUpdater methods

  @Override
  public boolean updateCredential(RealmModel realm, UserModel user, CredentialInput input) {
    throw new UnsupportedOperationException("Not supported by Crowd");
  }

  @Override
  public void disableCredentialType(RealmModel realm, UserModel user, String credentialType) {
    throw new UnsupportedOperationException("Not supported by Crowd");
  }

  @Override
  public Stream<String> getDisableableCredentialTypesStream(RealmModel realm, UserModel user) {
    return Stream.empty();
  }

  @Override
  public void close() {

  }
}
