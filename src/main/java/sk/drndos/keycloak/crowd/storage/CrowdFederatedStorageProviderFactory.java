package sk.drndos.keycloak.crowd.storage;

import org.jboss.logging.Logger;
import org.keycloak.Config.Scope;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.storage.federated.UserFederatedStorageProvider;
import org.keycloak.storage.federated.UserFederatedStorageProviderFactory;

public class CrowdFederatedStorageProviderFactory implements UserFederatedStorageProviderFactory {
  private static final Logger logger = Logger.getLogger(CrowdFederatedStorageProviderFactory.class);

  public static final String PROVIDER_NAME = "crowd-rest";

  @Override
  public UserFederatedStorageProvider create(KeycloakSession session) {
    logger.info("This is a factory, bitch!");
    return new CrowdFederatedUserStorageProvider(session, null, null);
  }

  @Override
  public void init(Scope config) {
    logger.info(config);
  }

  @Override
  public void postInit(KeycloakSessionFactory factory) {

  }

  @Override
  public void close() {

  }

  @Override
  public String getId() {
    return PROVIDER_NAME;
  }
}
