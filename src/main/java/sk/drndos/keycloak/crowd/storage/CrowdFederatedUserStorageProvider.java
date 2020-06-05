package sk.drndos.keycloak.crowd.storage;

import static java.util.stream.Collectors.toSet;

import com.atlassian.crowd.service.client.CrowdClient;
import java.util.List;
import java.util.Set;
import org.jboss.logging.Logger;
import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.component.ComponentModel;
import org.keycloak.credential.CredentialModel;
import org.keycloak.models.ClientModel;
import org.keycloak.models.ClientScopeModel;
import org.keycloak.models.FederatedIdentityModel;
import org.keycloak.models.GroupModel;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.UserConsentModel;
import org.keycloak.models.UserModel;
import org.keycloak.storage.federated.UserFederatedStorageProvider;

public class CrowdFederatedUserStorageProvider implements UserFederatedStorageProvider {
  private static final Logger logger = Logger.getLogger(CrowdFederatedUserStorageProvider.class);

  private final KeycloakSession session;
  private final CrowdClient client;
  private final ComponentModel model;

  public CrowdFederatedUserStorageProvider(KeycloakSession session,
      CrowdClient client, ComponentModel model) {
    this.session = session;
    this.client = client;
    this.model = model;
  }

  @Override
  public Set<GroupModel> getGroups(RealmModel realm, String userId) {
    logger.info("Getting groups for user " + userId);
    try {
      return client.getGroupsForUser(CrowdUserStorageProvider.idToUsername(userId), 0, 1000).stream()
          .map(g -> new GroupAdapter(g, client, model))
          .collect(toSet());
    } catch (Exception e) {
      logger.info(e);
      return null;
    }
  }

  @Override
  public void joinGroup(RealmModel realm, String userId, GroupModel group) {
    throw new UnsupportedOperationException("Not supported by Crowd");
  }

  @Override
  public void leaveGroup(RealmModel realm, String userId, GroupModel group) {
    throw new UnsupportedOperationException("Not supported by Crowd");
  }

  @Override
  public List<String> getMembership(RealmModel realm, GroupModel group, int firstResult, int max) {
    throw new UnsupportedOperationException("Not supported by Crowd");
  }

  @Override
  public List<String> getStoredUsers(RealmModel realm, int first, int max) {
    return session.userFederatedStorage().getStoredUsers(realm, first, max);
  }

  @Override
  public int getStoredUsersCount(RealmModel realm) {
    return session.userFederatedStorage().getStoredUsersCount(realm);
  }

  @Override
  public void preRemove(RealmModel realm, ClientModel client) {
    session.userFederatedStorage().preRemove(realm, client);
  }

  @Override
  public void preRemove(ProtocolMapperModel protocolMapper) {
    session.userFederatedStorage().preRemove(protocolMapper);
  }

  @Override
  public void preRemove(ClientScopeModel clientScope) {
    session.userFederatedStorage().preRemove(clientScope);
  }

  @Override
  public void preRemove(RealmModel realm, UserModel user) {
    session.userFederatedStorage().preRemove(realm, user);
  }

  @Override
  public void preRemove(RealmModel realm, ComponentModel model) {
    session.userFederatedStorage().preRemove(realm, model);
  }

  @Override
  public void setSingleAttribute(RealmModel realm, String userId, String name, String value) {
    session.userFederatedStorage().setSingleAttribute(realm, userId, name, value);
  }

  @Override
  public void setAttribute(RealmModel realm, String userId, String name, List<String> values) {
    session.userFederatedStorage().setAttribute(realm, userId, name, values);
  }

  @Override
  public void removeAttribute(RealmModel realm, String userId, String name) {
    session.userFederatedStorage().removeAttribute(realm, userId, name);
  }

  @Override
  public MultivaluedHashMap<String, String> getAttributes(RealmModel realm, String userId) {
    return session.userFederatedStorage().getAttributes(realm, userId);
  }

  @Override
  public List<String> getUsersByUserAttribute(RealmModel realm, String name, String value) {
    return session.userFederatedStorage().getUsersByUserAttribute(realm, name, value);
  }

  @Override
  public String getUserByFederatedIdentity(FederatedIdentityModel socialLink, RealmModel realm) {
    return session.userFederatedStorage().getUserByFederatedIdentity(socialLink, realm);
  }

  @Override
  public void addFederatedIdentity(RealmModel realm, String userId, FederatedIdentityModel socialLink) {
    session.userFederatedStorage().addFederatedIdentity(realm, userId, socialLink);
  }

  @Override
  public boolean removeFederatedIdentity(RealmModel realm, String userId, String socialProvider) {
    return session.userFederatedStorage().removeFederatedIdentity(realm, userId, socialProvider);
  }

  @Override
  public void preRemove(RealmModel realm, IdentityProviderModel provider) {
    session.userFederatedStorage().preRemove(realm, provider);
  }

  @Override
  public void updateFederatedIdentity(RealmModel realm, String userId, FederatedIdentityModel federatedIdentityModel) {
    session.userFederatedStorage().updateFederatedIdentity(realm, userId, federatedIdentityModel);
  }

  @Override
  public Set<FederatedIdentityModel> getFederatedIdentities(String userId, RealmModel realm) {
    return session.userFederatedStorage().getFederatedIdentities(userId, realm);
  }

  @Override
  public FederatedIdentityModel getFederatedIdentity(String userId, String socialProvider, RealmModel realm) {
    return session.userFederatedStorage().getFederatedIdentity(userId, socialProvider, realm);
  }

  @Override
  public void addConsent(RealmModel realm, String userId, UserConsentModel consent) {
    session.userFederatedStorage().addConsent(realm, userId, consent);
  }

  @Override
  public UserConsentModel getConsentByClient(RealmModel realm, String userId, String clientInternalId) {
    return session.userFederatedStorage().getConsentByClient(realm, userId, clientInternalId);
  }

  @Override
  public List<UserConsentModel> getConsents(RealmModel realm, String userId) {
    return session.userFederatedStorage().getConsents(realm, userId);
  }

  @Override
  public void updateConsent(RealmModel realm, String userId, UserConsentModel consent) {
    session.userFederatedStorage().updateConsent(realm, userId, consent);
  }

  @Override
  public boolean revokeConsentForClient(RealmModel realm, String userId, String clientInternalId) {
    return session.userFederatedStorage().revokeConsentForClient(realm, userId, clientInternalId);
  }

  @Override
  public void updateCredential(RealmModel realm, String userId, CredentialModel cred) {
    session.userFederatedStorage().updateCredential(realm, userId, cred);
  }

  @Override
  public CredentialModel createCredential(RealmModel realm, String userId, CredentialModel cred) {
    return session.userFederatedStorage().createCredential(realm, userId, cred);
  }

  @Override
  public boolean removeStoredCredential(RealmModel realm, String userId, String id) {
    return session.userFederatedStorage().removeStoredCredential(realm, userId, id);
  }

  @Override
  public CredentialModel getStoredCredentialById(RealmModel realm, String userId, String id) {
    return session.userFederatedStorage().getStoredCredentialById(realm, userId, id);
  }

  @Override
  public List<CredentialModel> getStoredCredentials(RealmModel realm, String userId) {
    return session.userFederatedStorage().getStoredCredentials(realm, userId);
  }

  @Override
  public List<CredentialModel> getStoredCredentialsByType(RealmModel realm, String userId, String type) {
    return session.userFederatedStorage().getStoredCredentialsByType(realm, userId, type);
  }

  @Override
  public CredentialModel getStoredCredentialByNameAndType(RealmModel realm, String userId, String name, String type) {
    return session.userFederatedStorage().getStoredCredentialByNameAndType(realm, userId, name, type);
  }

  @Override
  public void setNotBeforeForUser(RealmModel realm, String userId, int notBefore) {
    session.userFederatedStorage().setNotBeforeForUser(realm, userId, notBefore);
  }

  @Override
  public int getNotBeforeOfUser(RealmModel realm, String userId) {
    return session.userFederatedStorage().getNotBeforeOfUser(realm, userId);
  }

  @Override
  public Set<String> getRequiredActions(RealmModel realm, String userId) {
    return session.userFederatedStorage().getRequiredActions(realm, userId);
  }

  @Override
  public void addRequiredAction(RealmModel realm, String userId, String action) {
    session.userFederatedStorage().addRequiredAction(realm, userId, action);
  }

  @Override
  public void removeRequiredAction(RealmModel realm, String userId, String action) {
    session.userFederatedStorage().removeRequiredAction(realm, userId, action);
  }

  @Override
  public void grantRole(RealmModel realm, String userId, RoleModel role) {
    session.userFederatedStorage().grantRole(realm, userId, role);
  }

  @Override
  public Set<RoleModel> getRoleMappings(RealmModel realm, String userId) {
    return session.userFederatedStorage().getRoleMappings(realm, userId);
  }

  @Override
  public void deleteRoleMapping(RealmModel realm, String userId, RoleModel role) {
    session.userFederatedStorage().deleteRoleMapping(realm, userId, role);
  }

  @Override
  public void preRemove(RealmModel realm) {
    session.userFederatedStorage().preRemove(realm);
  }

  @Override
  public void preRemove(RealmModel realm, GroupModel group) {
    session.userFederatedStorage().preRemove(realm, group);
  }

  @Override
  public void preRemove(RealmModel realm, RoleModel role) {
    session.userFederatedStorage().preRemove(realm, role);
  }

  @Override
  public void close() {

  }
}
