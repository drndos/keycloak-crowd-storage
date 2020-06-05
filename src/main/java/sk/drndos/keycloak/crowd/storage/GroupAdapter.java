package sk.drndos.keycloak.crowd.storage;

import com.atlassian.crowd.model.group.Group;
import com.atlassian.crowd.service.client.CrowdClient;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.jboss.logging.Logger;
import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.ClientModel;
import org.keycloak.models.GroupModel;
import org.keycloak.models.RoleModel;
import org.keycloak.storage.StorageId;

public class GroupAdapter implements GroupModel {

  private static final Logger logger = Logger.getLogger(GroupAdapter.class);

  private final Group entity;
  private final String keycloakId;
  private final CrowdClient client;
  private final ComponentModel model;

  private final Map<String, Function<Group, String>> attributeFunctions = new HashMap<String, Function<Group, String>>() {{
    put("description", Group::getDescription);
  }};

  public GroupAdapter(Group entity, CrowdClient client, ComponentModel model) {
    this.keycloakId = StorageId.keycloakId(model, entity.getName());
    this.entity = entity;
    this.client = client;
    this.model = model;
  }

  @Override
  public String getId() {
    return keycloakId;
  }

  @Override
  public String getName() {
    return entity.getName();
  }

  @Override
  public void setName(String name) {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public void setSingleAttribute(String name, String value) {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public void setAttribute(String name, List<String> values) {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public void removeAttribute(String name) {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public String getFirstAttribute(String name) {
    return getAttribute(name).stream().findFirst().orElse(null);
  }

  @Override
  public List<String> getAttribute(String name) {
    if (attributeFunctions.containsKey(name)) {
      List<String> listOfAttribute = new LinkedList<>();
      listOfAttribute.add(attributeFunctions.get(name).apply(entity));
      return listOfAttribute;
    } else {
      return Collections.emptyList();
    }
  }

  @Override
  public Map<String, List<String>> getAttributes() {
    MultivaluedHashMap<String, String> all = new MultivaluedHashMap<>();
    attributeFunctions.forEach((key, value) -> all.add(key, value.apply(entity)));
    return all;
  }

  @Override
  public GroupModel getParent() {
    try {
      return client.getParentGroupsForGroup(entity.getName(), 0, 1).stream()
          .findFirst()
          .map(g -> new GroupAdapter(g, client, model))
          .orElse(null);
    } catch (Exception e) {
      logger.info(e);
      return null;
    }
  }

  @Override
  public String getParentId() {
    return getParent().getId();
  }

  @Override
  public Set<GroupModel> getSubGroups() {
    try {
      return client.getChildGroupsOfGroup(entity.getName(), 0, 1000).stream()
          .map(g -> new GroupAdapter(g, client, model)).collect(Collectors.toSet());
    } catch (Exception e) {
      logger.info(e);
      return null;
    }
  }

  @Override
  public void setParent(GroupModel group) {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public void addChild(GroupModel subGroup) {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public void removeChild(GroupModel subGroup) {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public Set<RoleModel> getRealmRoleMappings() {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public Set<RoleModel> getClientRoleMappings(ClientModel app) {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public boolean hasRole(RoleModel role) {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public void grantRole(RoleModel role) {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public Set<RoleModel> getRoleMappings() {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public void deleteRoleMapping(RoleModel role) {
    throw new UnsupportedOperationException("Not implemented");
  }
}
