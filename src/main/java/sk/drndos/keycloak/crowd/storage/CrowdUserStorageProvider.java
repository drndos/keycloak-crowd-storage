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
import com.atlassian.crowd.search.query.entity.restriction.Property;
import com.atlassian.crowd.search.query.entity.restriction.PropertyImpl;
import com.atlassian.crowd.search.query.entity.restriction.PropertyRestriction;
import com.atlassian.crowd.service.client.CrowdClient;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.jboss.logging.Logger;
import org.keycloak.component.ComponentModel;
import org.keycloak.credential.CredentialInput;
import org.keycloak.credential.CredentialInputUpdater;
import org.keycloak.credential.CredentialInputValidator;
import org.keycloak.credential.CredentialModel;
import org.keycloak.models.GroupModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserCredentialModel;
import org.keycloak.models.UserModel;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.UserStorageProvider;
import org.keycloak.storage.user.UserLookupProvider;
import org.keycloak.storage.user.UserQueryProvider;
import org.keycloak.storage.user.UserRegistrationProvider;

public class CrowdUserStorageProvider implements
    UserStorageProvider,
    UserLookupProvider,
    CredentialInputValidator,
    CredentialInputUpdater,
    UserRegistrationProvider,
    UserQueryProvider {

  private static final Logger logger = Logger.getLogger(CrowdUserStorageProvider.class);

  public static final String UNSET_PASSWORD = "#$!-UNSET-PASSWORD";

  private KeycloakSession session;
  private CrowdClient client;
  private ComponentModel model;
  // map of loaded users in this transaction
  protected Map<String, UserModel> loadedUsers = new HashMap<>();


  public CrowdUserStorageProvider(KeycloakSession session, ComponentModel model, CrowdClient client) {
    this.session = session;
    this.model = model;
    this.client = client;
  }

  // UserLookupProvider methods

  @Override
  public UserModel getUserByUsername(String username, RealmModel realm) {
    logger.info("Getting user by username " + username);
    try {
      User user = client.getUser(username);
      return new UserAdapter(session, realm, model, user);
    } catch (Exception e) {
      logger.info(e);
      return null;
    }
  }


  @Override
  public UserModel getUserById(String id, RealmModel realm) {
    logger.info("Getting user by id " + id);
    StorageId storageId = new StorageId(id);
    String username = storageId.getExternalId();
    return getUserByUsername(username, realm);
  }

  @Override
  public UserModel getUserByEmail(String email, RealmModel realm) {
    throw new UnsupportedOperationException("Not supported by Crowd");
  }

  // UserQueryProvider methods

  @Override
  public int getUsersCount(RealmModel realm) {
    throw new UnsupportedOperationException("Not supported by Crowd");
  }

  @Override
  public List<UserModel> getUsers(RealmModel realm) {
    return getUsers(realm, 0, Integer.MAX_VALUE);
  }

  @Override
  public List<UserModel> getUsers(RealmModel realm, int firstResult, int maxResults) {
    throw new UnsupportedOperationException("Not supported by Crowd");
  }

  // UserQueryProvider method implementations

  @Override
  public List<UserModel> searchForUser(String search, RealmModel realm) {
    return searchForUser(search, realm, 0, Integer.MAX_VALUE);
  }

  @Override
  public List<UserModel> searchForUser(String search, RealmModel realm, int firstResult, int maxResults) {
    try {
      return client.searchUsers(new PropertyRestriction() {
        @Override
        public Property getProperty() {
          return new PropertyImpl("name", String.class);
        }

        @Override
        public MatchMode getMatchMode() {
          return MatchMode.CONTAINS;
        }

        @Override
        public Object getValue() {
          return search;
        }
      }, firstResult, maxResults)
          .stream()
          .map(user -> new UserAdapter(session, realm, model, user))
          .collect(Collectors.toList());

    } catch (Exception e) {
      logger.info(e);
      return null;
    }
  }

  @Override
  public List<UserModel> searchForUser(Map<String, String> params, RealmModel realm) {
    return searchForUser(params, realm, 0, Integer.MAX_VALUE);
  }

  @Override
  public List<UserModel> searchForUser(Map<String, String> params, RealmModel realm, int firstResult, int maxResults) {
    throw new UnsupportedOperationException("Not supported by Crowd");
  }

  @Override
  public List<UserModel> getGroupMembers(RealmModel realm, GroupModel group, int firstResult, int maxResults) {
    // runtime automatically handles querying UserFederatedStorage
    return Collections.EMPTY_LIST;
  }

  @Override
  public List<UserModel> getGroupMembers(RealmModel realm, GroupModel group) {
    // runtime automatically handles querying UserFederatedStorage
    return Collections.EMPTY_LIST;
  }

  @Override
  public List<UserModel> searchForUserByUserAttribute(String attrName, String attrValue, RealmModel realm) {
    // runtime automatically handles querying UserFederatedStorage
    return Collections.EMPTY_LIST;
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
    logger.info("Is user " + user.getUsername() + " configured for ? " + credentialType);
    return supportsCredentialType(credentialType);
  }

  @Override
  public boolean supportsCredentialType(String credentialType) {
    logger.info("Does realm support ? " + credentialType);
    return credentialType.equals(CredentialModel.PASSWORD);
  }

  @Override
  public boolean isValid(RealmModel realm, UserModel user, CredentialInput input) {
    if (!supportsCredentialType(input.getType()) || !(input instanceof UserCredentialModel)) {
      return false;
    }

    UserCredentialModel cred = (UserCredentialModel) input;
    logger.info("Is user valid ? " + user.getUsername());
    try {
      User authenticatedUser = client.authenticateUser(user.getUsername(), cred.getChallengeResponse());
      return authenticatedUser != null;
    } catch (Exception e) {
      logger.info(e);
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
  public Set<String> getDisableableCredentialTypes(RealmModel realm, UserModel user) {

    return Collections.emptySet();
  }

  @Override
  public void close() {

  }
}
