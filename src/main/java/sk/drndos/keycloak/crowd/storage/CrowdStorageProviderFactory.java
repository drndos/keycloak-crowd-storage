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

import com.atlassian.crowd.integration.rest.service.factory.RestCrowdClientFactory;
import com.atlassian.crowd.service.client.CrowdClient;
import java.util.List;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.component.ComponentModel;
import org.keycloak.component.ComponentValidationException;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;
import org.keycloak.storage.UserStorageProviderFactory;

@JBossLog
public class CrowdStorageProviderFactory implements UserStorageProviderFactory<CrowdUserStorageProvider> {

  public static final String PROVIDER_NAME = "crowd";

  protected static final List<ProviderConfigProperty> configMetadata;

  static {
    configMetadata = ProviderConfigurationBuilder.create()
        .property().name("url")
        .type(ProviderConfigProperty.STRING_TYPE)
        .label("Crowd URL")
        .helpText("Base url for Crowd server")
        .add()
        .property().name("applicationName")
        .type(ProviderConfigProperty.STRING_TYPE)
        .label("Crowd Application Name")
        .helpText("Application name registered in Crowd server")
        .add()
        .property().name("applicationPassword")
        .type(ProviderConfigProperty.PASSWORD)
        .label("Crowd Application Password")
        .helpText("Application password registered in Crowd server")
        .add()
        .build();
  }

  @Override
  public List<ProviderConfigProperty> getConfigProperties() {
    return configMetadata;
  }

  @Override
  public void validateConfiguration(KeycloakSession session, RealmModel realm, ComponentModel config)
      throws ComponentValidationException {
    String url = config.getConfig().getFirst("url");
      if (url == null) {
          throw new ComponentValidationException("please provide base URL to crowd server");
      }
    String applicationName = config.getConfig().getFirst("applicationName");
      if (applicationName == null) {
          throw new ComponentValidationException("please provide Application name registered in crowd");
      }
    String applicationPassword = config.getConfig().getFirst("applicationPassword");
      if (applicationPassword == null) {
          throw new ComponentValidationException("please provide Application password registered in crowd");
      }
  }

  @Override
  public String getId() {
    return PROVIDER_NAME;
  }

  @Override
  public CrowdUserStorageProvider create(KeycloakSession session, ComponentModel model) {
    String url = model.getConfig().getFirst("url");
    String applicationName = model.getConfig().getFirst("applicationName");
    String applicationPassword = model.getConfig().getFirst("applicationPassword");
    CrowdClient client = new RestCrowdClientFactory().newInstance(url, applicationName, applicationPassword);
    return new CrowdUserStorageProvider(session, model, client);
  }
}
