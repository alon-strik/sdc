/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.action.dao.impl;

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static com.datastax.driver.core.querybuilder.QueryBuilder.in;
import static com.datastax.driver.core.querybuilder.QueryBuilder.set;
import static org.openecomp.core.nosqldb.impl.cassandra.CassandraSessionFactory.getSession;
import static org.openecomp.sdc.action.util.ActionUtil.actionLogPostProcessor;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.UDTValue;
import com.datastax.driver.core.exceptions.NoHostAvailableException;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.Result;
import com.datastax.driver.mapping.UDTMapper;
import com.datastax.driver.mapping.annotations.Accessor;
import com.datastax.driver.mapping.annotations.Query;
import org.openecomp.core.dao.impl.CassandraBaseDao;
import org.openecomp.core.logging.api.Logger;
import org.openecomp.core.logging.api.LoggerFactory;
import org.openecomp.core.nosqldb.api.NoSqlDb;
import org.openecomp.core.nosqldb.factory.NoSqlDbFactory;
import org.openecomp.core.utilities.json.JsonUtil;
import org.openecomp.sdc.action.ActionConstants;
import org.openecomp.sdc.action.dao.ActionDao;
import org.openecomp.sdc.action.dao.types.ActionEntity;
import org.openecomp.sdc.action.dao.types.EcompComponentEntity;
import org.openecomp.sdc.action.errors.ActionErrorConstants;
import org.openecomp.sdc.action.errors.ActionException;
import org.openecomp.sdc.action.logging.CategoryLogLevel;
import org.openecomp.sdc.action.logging.StatusCode;
import org.openecomp.sdc.action.types.Action;
import org.openecomp.sdc.action.types.ActionStatus;
import org.openecomp.sdc.action.types.ActionSubOperation;
import org.openecomp.sdc.action.types.EcompComponent;
import org.openecomp.sdc.action.util.ActionUtil;
import org.openecomp.sdc.versioning.VersioningManagerFactory;
import org.openecomp.sdc.versioning.dao.VersionInfoDao;
import org.openecomp.sdc.versioning.dao.VersionInfoDaoFactory;
import org.openecomp.sdc.versioning.dao.VersionInfoDeletedDao;
import org.openecomp.sdc.versioning.dao.VersionInfoDeletedDaoFactory;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.dao.types.VersionInfoDeletedEntity;
import org.openecomp.sdc.versioning.dao.types.VersionInfoEntity;
import org.openecomp.sdc.versioning.types.VersionableEntityMetadata;
import org.slf4j.MDC;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ActionDaoImpl extends CassandraBaseDao<ActionEntity> implements ActionDao {
  private static final NoSqlDb noSqlDb = NoSqlDbFactory.getInstance().createInterface();
  private static final Mapper<ActionEntity> mapper =
      noSqlDb.getMappingManager().mapper(ActionEntity.class);
  private static final ActionAccessor accessor =
      noSqlDb.getMappingManager().createAccessor(ActionAccessor.class);
  private static final UDTMapper<Version> versionMapper =
      noSqlDb.getMappingManager().udtMapper(Version.class);
  private static final VersionInfoDao versionInfoDao =
      VersionInfoDaoFactory.getInstance().createInterface();
  private static final VersionInfoDeletedDao versionInfoDeletedDao =
      VersionInfoDeletedDaoFactory.getInstance().createInterface();

  private final Logger log = (Logger) LoggerFactory.getLogger(this.getClass().getName());


  @Override
  public void registerVersioning(String versionableEntityType) {
    VersioningManagerFactory.getInstance().createInterface()
        .register(versionableEntityType, new VersionableEntityMetadata(
            mapper.getTableMetadata().getName(),
            mapper.getTableMetadata().getPartitionKey().get(0).getName(),
            mapper.getTableMetadata().getPartitionKey().get(1).getName()));
  }

  @Override
  public Action createAction(Action actionDto) {
    try {
      ActionUtil.actionLogPreProcessor(
          ActionSubOperation.CREATE_ACTION_ENTITY, ActionConstants.TARGET_ENTITY_DB);
      this.create(actionDto.toEntity());
      ActionUtil.actionLogPostProcessor(StatusCode.COMPLETE, null, "", false);
      log.metrics("");
      return actionDto;
    } catch (NoHostAvailableException noHostAvailableException) {
      logGenericException(noHostAvailableException);
      throw new ActionException(ActionErrorConstants.ACTION_INTERNAL_SERVER_ERR_CODE,
          ActionErrorConstants.ACTION_ENTITY_INTERNAL_SERVER_ERROR_MSG);
    }
  }

  @Override
  public Action updateAction(Action actionDto) {
    try {
      log.debug(" entering updateAction with actionUUID= " + actionDto.getActionUuId());
      ActionUtil
          .actionLogPreProcessor(ActionSubOperation.UPDATE_ACTION,
              ActionConstants.TARGET_ENTITY_DB);
      this.update(actionDto.toEntity());
      ActionUtil.actionLogPostProcessor(StatusCode.COMPLETE, null, "", false);
      log.metrics("");
      log.debug(" exit updateAction with actionUUID= " + actionDto.getActionUuId());
      return actionDto;
    } catch (NoHostAvailableException noHostAvailableException) {
      logGenericException(noHostAvailableException);
      throw new ActionException(ActionErrorConstants.ACTION_INTERNAL_SERVER_ERR_CODE,
          ActionErrorConstants.ACTION_ENTITY_INTERNAL_SERVER_ERROR_MSG);
    }
  }

  @Override
  public void deleteAction(String actionInvariantUuId) {
    try {
      log.debug("entering deleteAction with actionInvariantUUID = " + actionInvariantUuId);
      ActionUtil
          .actionLogPreProcessor(ActionSubOperation.GET_ACTION_VERSION,
              ActionConstants.TARGET_ENTITY_DB);
      VersionInfoDeletedEntity activeVersionEntity = versionInfoDeletedDao.get(
          new VersionInfoDeletedEntity(ActionConstants.ACTION_VERSIONABLE_TYPE,
              actionInvariantUuId));
      ActionUtil.actionLogPostProcessor(StatusCode.COMPLETE, null, "", false);
      log.metrics("");
      Version activeVersion = activeVersionEntity.getActiveVersion();
      Statement getNameFromInvUuId = QueryBuilder.select().column("name").from("dox", "Action")
          .where(eq("actioninvariantuuid", actionInvariantUuId))
          .and(in("version", versionMapper.toUDT(activeVersion)));
      ActionUtil
          .actionLogPreProcessor(ActionSubOperation.GET_NAME_BY_ACTIONINVID,
              ActionConstants.TARGET_ENTITY_DB);
      ResultSet results = getSession().execute(getNameFromInvUuId);
      ActionUtil.actionLogPostProcessor(StatusCode.COMPLETE, null, "", false);
      log.metrics("");
      if (!results.isExhausted()) {
        String name = results.one().getString("name");
        List<Version> versions = getVersionsByName(name);
        updateActionStatusForDelete(actionInvariantUuId, versions);
      }
    } catch (NoHostAvailableException noHostAvailableException) {
      logGenericException(noHostAvailableException);
      throw new ActionException(ActionErrorConstants.ACTION_INTERNAL_SERVER_ERR_CODE,
          ActionErrorConstants.ACTION_ENTITY_INTERNAL_SERVER_ERROR_MSG);
    }
    log.debug("exit deleteAction");
  }


  @Override
  public List<Action> getActionsByActionInvariantUuId(String actionInvariantUuId) {
    List<Action> actions = new ArrayList<>();
    try {
      log.debug(" entering getActionsByActionInvariantUUID with actionInvariantUUID= "
          + actionInvariantUuId);
      Set<Version> viewableVersions = new HashSet<>();
      VersionPredicate filter = new VersionPredicate();
      ActionUtil
          .actionLogPreProcessor(ActionSubOperation.GET_ACTION_VERSION,
              ActionConstants.TARGET_ENTITY_DB);
      VersionInfoEntity versionInfoEntity = versionInfoDao
          .get(new VersionInfoEntity(ActionConstants.ACTION_VERSIONABLE_TYPE, actionInvariantUuId));
      if (versionInfoEntity == null) {
        //Check for action in the Delete version info table
        VersionInfoDeletedEntity versionInfoDeletedEntity = versionInfoDeletedDao.get(
            new VersionInfoDeletedEntity(ActionConstants.ACTION_VERSIONABLE_TYPE,
                actionInvariantUuId));
        if (versionInfoDeletedEntity != null) {
          viewableVersions = versionInfoDeletedEntity.getViewableVersions();
          //Remove intermediate minor versions from viewable versions
          if (versionInfoDeletedEntity.getActiveVersion() != null) {
            filter.activeVersion = versionInfoDeletedEntity.getActiveVersion();
            filter.finalVersion = versionInfoDeletedEntity.getLatestFinalVersion();
            viewableVersions.removeIf(filter::isIntermediateMinorVersion);
          }
        }
      } else {
        viewableVersions = versionInfoEntity.getViewableVersions();
        //Remove intermediate minor versions from viewable versions
        if (versionInfoEntity.getActiveVersion() != null) {
          filter.activeVersion = versionInfoEntity.getActiveVersion();
          filter.finalVersion = versionInfoEntity.getLatestFinalVersion();
          viewableVersions.removeIf(filter::isIntermediateMinorVersion);
        }
        //Add candidate version if available
        if (versionInfoEntity.getCandidate() != null) {
          viewableVersions.add(versionInfoEntity.getCandidate().getVersion());
        }
      }
      MDC.put(ActionConstants.TARGET_ENTITY, ActionConstants.TARGET_ENTITY_DB);
      ActionUtil.actionLogPostProcessor(StatusCode.COMPLETE, null, "", false);
      log.metrics("");

      log.debug(
          "Found " + viewableVersions + " viewable version for action with actionInvariantUUID "
              + actionInvariantUuId);

      //Fetch action data for the viewable versions
      if (!viewableVersions.isEmpty()) {
        ActionUtil.actionLogPreProcessor(ActionSubOperation.GET_ACTIONENTITY_BY_ACTIONINVID,
            ActionConstants.TARGET_ENTITY_DB);
        Result<ActionEntity> result =
            accessor.getActionsByInvId(actionInvariantUuId, viewableVersions);
        ActionUtil.actionLogPostProcessor(StatusCode.COMPLETE, null, "", false);
        log.metrics("");
        if (result != null) {
          actions
              .addAll(result.all().stream().map(ActionEntity::toDto).collect(Collectors.toList()));
        }
      }
    } catch (NoHostAvailableException noHostAvailableException) {
      logGenericException(noHostAvailableException);
      throw new ActionException(ActionErrorConstants.ACTION_INTERNAL_SERVER_ERR_CODE,
          ActionErrorConstants.ACTION_ENTITY_INTERNAL_SERVER_ERROR_MSG);
    }
    log.debug(
        " exit getActionsByActionInvariantUUID with actionInvariantUUID= " + actionInvariantUuId);
    return actions;
  }

  private void logGenericException(Exception exception) {
    ActionUtil
        .actionLogPostProcessor(StatusCode.ERROR, ActionErrorConstants.ACTION_QUERY_FAILURE_CODE,
            ActionErrorConstants.ACTION_ENTITY_INTERNAL_SERVER_ERROR_MSG, false);
    log.metrics("");
    ActionUtil.actionErrorLogProcessor(
        CategoryLogLevel.FATAL, ActionErrorConstants.ACTION_QUERY_FAILURE_CODE,
        ActionErrorConstants.ACTION_QUERY_FAILURE_MSG);
    log.error(exception.getMessage());
  }


  @Override
  public List<Action> getFilteredActions(String filterType, String filterId) {
    List<Action> actions = new ArrayList<>();
    Result<ActionEntity> result = null;
    log.debug(
        " entering getFilteredActions By filterType = " + filterType + " With value = " + filterId);
    try {
      switch (filterType) {
        case ActionConstants.FILTER_TYPE_VENDOR:
          ActionUtil
              .actionLogPreProcessor(ActionSubOperation.GET_ACTIONENTITY_BY_VENDOR,
                  ActionConstants.TARGET_ENTITY_DB);
          result = accessor.getActionsByVendor(filterId);
          ActionUtil.actionLogPostProcessor(StatusCode.COMPLETE);
          log.metrics("");
          break;
        case ActionConstants.FILTER_TYPE_CATEGORY:
          ActionUtil.actionLogPreProcessor(ActionSubOperation.GET_ACTIONENTITY_BY_CATEGORY,
              ActionConstants.TARGET_ENTITY_DB);
          result = accessor.getActionsByCategory(filterId);
          ActionUtil.actionLogPostProcessor(StatusCode.COMPLETE);
          log.metrics("");
          break;
        case ActionConstants.FILTER_TYPE_MODEL:
          ActionUtil
              .actionLogPreProcessor(ActionSubOperation.GET_ACTIONENTITY_BY_MODEL,
                  ActionConstants.TARGET_ENTITY_DB);
          result = accessor.getActionsByModel(filterId);
          ActionUtil.actionLogPostProcessor(StatusCode.COMPLETE);
          log.metrics("");
          break;
        case ActionConstants.FILTER_TYPE_ECOMP_COMPONENT:
          ActionUtil.actionLogPreProcessor(ActionSubOperation.GET_ACTIONENTITY_BY_COMPONENT,
              ActionConstants.TARGET_ENTITY_DB);
          result = accessor.getActionsByEcompComponent(filterId);
          ActionUtil.actionLogPostProcessor(StatusCode.COMPLETE);
          log.metrics("");
          break;
        case ActionConstants.FILTER_TYPE_NONE:
          ActionUtil
              .actionLogPreProcessor(ActionSubOperation.GET_ALL_ACTIONS,
                  ActionConstants.TARGET_ENTITY_DB);
          result = accessor.getAllActions();
          ActionUtil.actionLogPostProcessor(StatusCode.COMPLETE);
          log.metrics("");
          break;
        case ActionConstants.FILTER_TYPE_NAME:
          ActionUtil
              .actionLogPreProcessor(ActionSubOperation.GET_ACTIONINVID_BY_NAME,
                  ActionConstants.TARGET_ENTITY_DB);
          result = accessor.getInvIdByName(filterId);
          ActionUtil.actionLogPostProcessor(StatusCode.COMPLETE);
          log.metrics("");
          List<ActionEntity> actionEntities = result.all();
          if (actionEntities != null && !actionEntities.isEmpty()) {
            String actionInvariantUuId = actionEntities.get(0).getActionInvariantUuId();
            if (actionInvariantUuId != null) {
              return getActionsByActionInvariantUuId(actionInvariantUuId);
            } else {
              return actions;
            }
          }
          break;
        default:
          break;
      }
      if (result != null) {
        actions.addAll(result.all().stream().map(ActionEntity::toDto).collect(Collectors.toList()));
      }
      log.debug(
          " exit getFilteredActions By filterType = " + filterType + " With value = " + filterId);
    } catch (NoHostAvailableException noHostAvailableException) {
      logGenericException(noHostAvailableException);
      throw new ActionException(ActionErrorConstants.ACTION_INTERNAL_SERVER_ERR_CODE,
          ActionErrorConstants.ACTION_ENTITY_INTERNAL_SERVER_ERROR_MSG);
    }
    return actions;
  }

  @Override
  public List<EcompComponent> getEcompComponents() {
    List<EcompComponent> ecompComponents = new ArrayList<>();
    Result<EcompComponentEntity> result;
    try {
      log.debug(" entering getECOMPComponents ");
      ActionUtil
          .actionLogPreProcessor(ActionSubOperation.GET_ECOMP_COMPONENTS_ENTITY,
              ActionConstants.TARGET_ENTITY_DB);
      result = accessor.getEcompComponents();
      ActionUtil.actionLogPostProcessor(StatusCode.COMPLETE, null, "", false);
      log.metrics("");
      if (result != null) {
        ecompComponents.addAll(
            result.all().stream().map(EcompComponentEntity::toDto).collect(Collectors.toList()));
      }
    } catch (NoHostAvailableException noHostAvailableException) {
      logGenericException(noHostAvailableException);
      throw new ActionException(ActionErrorConstants.ACTION_INTERNAL_SERVER_ERR_CODE,
          ActionErrorConstants.ACTION_ENTITY_INTERNAL_SERVER_ERROR_MSG);
    }
    log.debug(" exit getECOMPComponents ");
    return ecompComponents;
  }

  @Override
  public Action getActionsByActionUuId(String uniqueId) {
    try {
      log.debug(" entering getActionsByActionUUID with actionUUID= " + uniqueId);
      ActionUtil
          .actionLogPreProcessor(ActionSubOperation.GET_ACTIONENTITY_BY_ACTIONUUID,
              ActionConstants.TARGET_ENTITY_DB);
      Result<ActionEntity> result = accessor.actionInvariantUuId(uniqueId);
      ActionUtil.actionLogPostProcessor(StatusCode.COMPLETE, null, "", false);
      log.metrics("");
      if (result != null) {
        log.debug(" exit getActionsByActionUUID with actionUUID= " + uniqueId);
        ActionEntity entity = result.one();
        if (entity != null) {
          return entity.toDto();
        }
      }
      log.debug(" exit getActionsByActionUUID with actionUUID= " + uniqueId);
      return null;
    } catch (NoHostAvailableException noHostAvailableException) {
      logGenericException(noHostAvailableException);
      throw new ActionException(ActionErrorConstants.ACTION_INTERNAL_SERVER_ERR_CODE,
          ActionErrorConstants.ACTION_ENTITY_INTERNAL_SERVER_ERROR_MSG);
    }
  }

  @Override
  public Action getLockedAction(String actionInvariantUuId, String user) throws ActionException {
    log.debug(" entering getLockedAction with actionInvariantUUID= " + actionInvariantUuId);
    ActionUtil
        .actionLogPreProcessor(ActionSubOperation.GET_ACTION_VERSION,
            ActionConstants.TARGET_ENTITY_DB);
    Action action = null;
    VersionInfoEntity versionInfoEntity = versionInfoDao
        .get(new VersionInfoEntity(ActionConstants.ACTION_VERSIONABLE_TYPE, actionInvariantUuId));
    ActionUtil.actionLogPostProcessor(StatusCode.COMPLETE, null, "", false);
    log.metrics("");
    if (versionInfoEntity != null) {
      if (versionInfoEntity.getCandidate() != null) {
        String actionUser = versionInfoEntity.getCandidate().getUser();
        if (actionUser != null && actionUser.equals(user)) {
          Set<Version> versions = new HashSet<>();
          versions.add(versionInfoEntity.getCandidate().getVersion());
          ActionUtil.actionLogPreProcessor(ActionSubOperation.GET_ACTIONENTITY_BY_ACTIONINVID,
              ActionConstants.TARGET_ENTITY_DB);
          Result<ActionEntity> result = accessor.getActionsByInvId(actionInvariantUuId, versions);
          ActionUtil.actionLogPostProcessor(StatusCode.COMPLETE, null, "", false);
          log.metrics("");
          if (result != null) {
            ActionEntity actionEntity = result.one();
            action = actionEntity != null ? actionEntity.toDto() : null;
          }
        } else {
          throw new ActionException(ActionErrorConstants.ACTION_ARTIFACT_DEL_LOCKED_OTHER_USER_CODE,
              String
                  .format(ActionErrorConstants.ACTION_ARTIFACT_DEL_LOCKED_OTHER_USER, actionUser));
        }
      } else {
        throw new ActionException(
            ActionErrorConstants.ACTION_NOT_LOCKED_CODE,
            ActionErrorConstants.ACTION_NOT_LOCKED_MSG);
      }
    } else {
      throw new ActionException(ActionErrorConstants.ACTION_ENTITY_NOT_EXIST_CODE,
          ActionErrorConstants.ACTION_ENTITY_NOT_EXIST);
    }
    return action;
  }

  @Override
  protected Mapper<ActionEntity> getMapper() {
    return mapper;
  }


  @Override
  protected Object[] getKeys(ActionEntity entity) {
    return new Object[]{entity.getActionInvariantUuId(), versionMapper.toUDT(entity.getVersion())};
  }

  @Override
  public Collection<ActionEntity> list(ActionEntity entity) {
    return accessor.getAllActions().all();
  }

  /**
   * param actionInvariantUuId. param versions.
   */
  private void updateActionStatusForDelete(String actionInvariantUuId, List<Version> versions) {
    log.debug(
        "entering updateActionStatusForDelete with actionInvariantUUID = " + actionInvariantUuId
            + " for versions " + versions);
    List<UDTValue> versionUdt = new ArrayList<>();

    versions.forEach(v -> versionMapper.toUDT(v));
    ActionUtil
        .actionLogPreProcessor(ActionSubOperation.UPDATE_ACTION_STATUS,
            ActionConstants.TARGET_ENTITY_DB);

    //Update the status column of action table
    Statement updateStatusStatement =
        QueryBuilder.update("dox", "Action").with(set("status", ActionStatus.Deleted.name()))
            .where(eq("actioninvariantuuid", actionInvariantUuId)).and(in("version", versionUdt));
    getSession().execute(updateStatusStatement);
    ActionUtil.actionLogPostProcessor(StatusCode.COMPLETE, null, "", false);
    log.metrics("");
    //Update the status in the data field of action table
    updateStatusInActionData(actionInvariantUuId, versions, ActionStatus.Deleted);
    log.debug("exit updateActionStatusForDelete with actionInvariantUUID = " + actionInvariantUuId
        + " for versions " + versions);
  }

  /**
   * Update status for a list of versions for a given action.
   *
   * @param actionInvariantUuId Invariant UUID of the action.
   * @param versions            List of {@link Version} for which the status has to be updated.
   * @param status              The status value.
   */
  private void updateStatusInActionData(String actionInvariantUuId, List<Version> versions,
                                        ActionStatus status) {
    log.debug("entering updateStatusInActionData for actionInvariantUUID = " + actionInvariantUuId
        + " and status = " + status + " for versions " + versions);
    for (Version v : versions) {
      ActionEntity entity = this.get(new ActionEntity(actionInvariantUuId, v));
      String currentData = entity.getData();
      Map<String, Object> currentDataMap = JsonUtil.json2Object(currentData, LinkedHashMap.class);
      currentDataMap.put(ActionConstants.STATUS, status);
      String updatedActionData = JsonUtil.object2Json(currentDataMap);
      entity.setData(updatedActionData);
      this.updateAction(entity.toDto());
    }
    log.debug("exit updateStatusInActionData");
  }

  /**
   * Get list of all major and minor version values for a given action by action name.
   *
   * @param name Name of the action
   * @return List of {@link Version} objects for the action
   */
  private List<Version> getVersionsByName(String name) {
    log.debug("entering getVersionsByName for Action Name = " + name);
    ActionUtil
        .actionLogPreProcessor(ActionSubOperation.GET_ACTION_VERSION,
            ActionConstants.TARGET_ENTITY_DB);
    Statement statement =
        QueryBuilder.select().column("version").from("dox", "Action").where(eq("name", name));
    ResultSet results = getSession().execute(statement);
    ActionUtil.actionLogPostProcessor(StatusCode.COMPLETE, null, "", false);
    log.metrics("");
    List<Version> versionList = new ArrayList<>();
    for (Row row : results) {
      Version version = versionMapper.fromUDT((UDTValue) row.getObject("version"));
      versionList.add(version);
    }
    log.debug("exit getVersionsByName for Action Name = " + name);
    return versionList;
  }

  @Accessor
  interface ActionAccessor {

    @Query("SELECT * FROM Action")
    Result<ActionEntity> getAllActions();

    @Query("SELECT * FROM Action where actionInvariantUuId = ? and version in ? ")
    Result<ActionEntity> getActionsByInvId(String actionInvariantUuId, Set<Version> versions);

    @Query("SELECT * FROM Action where supportedModels CONTAINS ?")
    Result<ActionEntity> getActionsByModel(String resource);

    @Query("SELECT * FROM Action where supportedComponents CONTAINS ?")
    Result<ActionEntity> getActionsByEcompComponent(String resource);

    @Query("SELECT * FROM Action where vendor_list CONTAINS ?")
    Result<ActionEntity> getActionsByVendor(String vendor);

    @Query("SELECT * FROM Action where category_list CONTAINS ?")
    Result<ActionEntity> getActionsByCategory(String vendor);

    @Query("SELECT actionInvariantUuId FROM Action where name = ? limit 1")
    Result<ActionEntity> getInvIdByName(String name);

    @Query("SELECT * FROM ECOMPComponent")
    Result<EcompComponentEntity> getEcompComponents();

    @Query("SELECT * FROM Action where actionUUID = ?")
    Result<ActionEntity> actionInvariantUuId(String actionUuId);

  }

  class VersionPredicate {
    Version activeVersion;
    Version finalVersion;

    public boolean isIntermediateMinorVersion(Version version) {
      int activeMajorVersion = activeVersion.getMajor();
      int activeMinorVersion = activeVersion.getMinor();
      int currentMinorVersion = version.getMinor();
      int currentMajorVersion = version.getMajor();

      if (finalVersion != null) {
        if (finalVersion.getMajor() == activeMajorVersion
            && currentMajorVersion == finalVersion.getMajor()) {
          if (currentMinorVersion < activeMinorVersion && currentMinorVersion != 0) {
            return true;
          }
        } else {
          return false;
        }
      } else {
        if (!version.equals(activeVersion)) {
          return true;
        }
      }

      return false;
    }

  }
}
