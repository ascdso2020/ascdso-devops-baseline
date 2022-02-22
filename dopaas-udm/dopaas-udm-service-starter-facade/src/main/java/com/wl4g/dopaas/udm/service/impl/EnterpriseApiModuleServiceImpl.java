// Generated by DoPaaS for Codegen, refer: http://dts.devops.wl4g.com

/*
 * Copyright 2017 ~ 2025 the original author or authors. <wanglsir@gmail.com, 983708408@qq.com>
 *
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
 */

package com.wl4g.dopaas.udm.service.impl;

import static com.wl4g.component.common.lang.Assert2.notNullOf;
import static java.util.Objects.isNull;

import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wl4g.component.core.page.PageHolder;
import com.wl4g.dopaas.common.bean.udm.EnterpriseApiModule;
import com.wl4g.dopaas.udm.data.EnterpriseApiDao;
import com.wl4g.dopaas.udm.data.EnterpriseApiModuleDao;
import com.wl4g.dopaas.udm.service.EnterpriseApiModuleService;
import com.wl4g.dopaas.udm.service.model.EnterpriseApiModulePageRequest;

/**
 * service implements of {@link EnterpriseApiModule}
 *
 * @author root
 * @version 0.0.1-SNAPSHOT
 * @Date
 * @since v1.0
 */
@Service
public class EnterpriseApiModuleServiceImpl implements EnterpriseApiModuleService {

	private @Autowired EnterpriseApiModuleDao enterpriseApiModuleDao;
	private @Autowired EnterpriseApiDao enterpriseApiDao;

	@Override
	public PageHolder<EnterpriseApiModule> page(EnterpriseApiModulePageRequest enterpriseApiModulePageRequest) {
		PageHolder<EnterpriseApiModule> pm = enterpriseApiModulePageRequest.getPm();
		pm.useCount().bind();
		EnterpriseApiModule enterpriseApiModule = new EnterpriseApiModule();
		BeanUtils.copyProperties(enterpriseApiModulePageRequest, enterpriseApiModule);
		pm.setRecords(enterpriseApiModuleDao.list(enterpriseApiModule));
		return pm;
	}

	@Override
	public List<EnterpriseApiModule> getByVersionIdAndParentId(Long versionId, Long parentId) {
		return enterpriseApiModuleDao.getByVersionIdAndParentId(versionId, parentId);
	}

	@Override
	public int save(EnterpriseApiModule enterpriseApiModule) {
		if (isNull(enterpriseApiModule.getId())) {
			enterpriseApiModule.preInsert();
			return enterpriseApiModuleDao.insertSelective(enterpriseApiModule);
		} else {
			enterpriseApiModule.preUpdate();
			return enterpriseApiModuleDao.updateByPrimaryKeySelective(enterpriseApiModule);
		}
	}

	@Override
	public EnterpriseApiModule detail(Long id) {
		notNullOf(id, "id");
		return enterpriseApiModuleDao.selectByPrimaryKey(id);
	}

	@Override
	public int del(Long id) {
		notNullOf(id, "id");
		enterpriseApiDao.deleteByModuleId(id);
		return enterpriseApiModuleDao.deleteByPrimaryKey(id);
	}

}
