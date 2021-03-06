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

package org.openecomp.sdc.model.impl;

import org.openecomp.core.model.dao.ServiceArtifactDaoInter;
import org.openecomp.core.model.dao.ServiceTemplateDaoInter;
import org.openecomp.core.model.types.ServiceArtifact;
import org.openecomp.core.model.types.ServiceElement;
import org.openecomp.core.model.types.ServiceTemplate;
import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.sdc.tosca.datatypes.ToscaServiceModel;
import org.openecomp.sdc.tosca.services.yamlutil.ToscaExtensionYamlUtil;
import org.openecomp.sdc.versioning.dao.VersionableDao;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AbstractServiceModelDao implements VersionableDao {

  protected ServiceTemplateDaoInter templateDao;
  protected ServiceArtifactDaoInter artifactDao;

  @Override
  public void registerVersioning(String versionableEntityType) {
    templateDao.registerVersioning(versionableEntityType);
    artifactDao.registerVersioning(versionableEntityType);
  }


  /**
   * Gets service model.
   *
   * @param vspId   the vsp id
   * @param version the version
   * @return the service model
   */
  public ToscaServiceModel getServiceModel(String vspId, Version version) {
    if (vspId == null || version == null) {
      //throw new CoreException()
      throw new RuntimeException("missing service model key");
    }


    FileContentHandler artifactFiles = getArtifacts(vspId, version);
    Map<String, org.openecomp.sdc.tosca.datatypes.model.ServiceTemplate> serviceTemplates =
        getTemplates(vspId, version);
    String entryDefinitionServiceTemplate = getServiceBase(vspId, version);
    return new ToscaServiceModel(artifactFiles, serviceTemplates, entryDefinitionServiceTemplate);
  }


  public void storeExternalArtifact(ServiceArtifact serviceArtifact) {
    artifactDao.create(serviceArtifact);
    //TODO: update last modification time
  }


  /**
   * Store service model.
   *
   * @param vspId             the vsp id
   * @param version           the version
   * @param toscaServiceModel the tosca service model
   */
  public void storeServiceModel(String vspId, Version version,
                                ToscaServiceModel toscaServiceModel) {
    ServiceArtifact entityArt;

    for (String fileName : toscaServiceModel.getArtifactFiles().getFileList()) {
      entityArt = new ServiceArtifact();
      entityArt.setContentData(
          FileUtils.toByteArray(toscaServiceModel.getArtifactFiles().getFileContent(fileName)));
      entityArt.setVspId(vspId);
      entityArt.setVersion(version);
      entityArt.setName(fileName);

      artifactDao.create(entityArt);
    }

    ServiceTemplate entityTmp;
    String yaml;
    for (Map.Entry<String, org.openecomp.sdc.tosca.datatypes.model.ServiceTemplate>
            entryTemplate : toscaServiceModel
        .getServiceTemplates().entrySet()) {
      entityTmp = new ServiceTemplate();

      yaml = new ToscaExtensionYamlUtil().objectToYaml(entryTemplate.getValue());
      entityTmp.setContentData(yaml.getBytes());
      entityTmp.setVspId(vspId);
      entityTmp.setVersion(version);
      entityTmp.setName(entryTemplate.getKey());
      entityTmp.setBaseName(toscaServiceModel.getEntryDefinitionServiceTemplate());

      templateDao.create(entityTmp);
    }

    //TODO: update last modification time
  }


  /**
   * Gets service model info.
   *
   * @param vspId   the vsp id
   * @param version the version
   * @param name    the name
   * @return the service model info
   */
  public ServiceElement getServiceModelInfo(String vspId, Version version, String name) {
    ServiceElement element = templateDao.getTemplateInfo(vspId, version, name);
    if (element != null) {
      return element;
    }

    element = artifactDao.getArtifactInfo(vspId, version, name);
    if (element != null) {
      return element;
    }
    return null;
  }


  /**
   * Gets service model content names.
   *
   * @return the service model content names
   */
  public List<String> getServiceModelContentNames() {


    return null;
  }


  private String getServiceBase(String vspId, Version version) {
    return templateDao.getBase(vspId, version);
  }

  private Map<String, org.openecomp.sdc.tosca.datatypes.model.ServiceTemplate> getTemplates(
      String vspId, Version version) {

    Collection<ServiceTemplate> templates = templateDao.list(vspId, version);
    if (templates == null) {
      return null;
    }
    return templates.stream().collect(Collectors.toMap(template -> template.getName(),
        template -> getServiceTemplate(template.getContent())));
  }

  private org.openecomp.sdc.tosca.datatypes.model.ServiceTemplate getServiceTemplate(
      InputStream content) {
    return new ToscaExtensionYamlUtil()
        .yamlToObject(content, org.openecomp.sdc.tosca.datatypes.model.ServiceTemplate.class);
  }

  private FileContentHandler getArtifacts(String vspId, Version version) {
    Collection<ServiceArtifact> templates = artifactDao.list(vspId, version);
    if (templates == null) {
      return null;
    }

    FileContentHandler fileContentHandler = new FileContentHandler();
    templates.stream().forEach(serviceArtifact -> fileContentHandler
        .addFile(serviceArtifact.getName(), serviceArtifact.getContent()));

    return fileContentHandler;
  }
}
