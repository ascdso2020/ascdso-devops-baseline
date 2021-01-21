// Generated by XCloud DevOps for Codegen, refer: http://dts.devops.wl4g.com

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

package com.wl4g.devops.doc.service;

import com.wl4g.component.core.bean.model.PageHolder;
import com.wl4g.component.rpc.springboot.feign.annotation.SpringBootFeignClient;
import com.wl4g.devops.common.bean.doc.EnterpriseApiModule;
import com.wl4g.devops.doc.service.dto.EnterpriseApiModulePageRequest;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * {@link EnterpriseApiModuleService}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @author vjay
 * @date 2020-01-14
 * @sine v1.0
 * @see
 */
@SpringBootFeignClient(name = "${provider.serviceId:enterpriseApiModuleService}")
@RequestMapping("/enterpriseApiModule")
public interface EnterpriseApiModuleService {

	@RequestMapping(value = "/PageHolder", method = POST)
	PageHolder<EnterpriseApiModule> page(@RequestBody EnterpriseApiModulePageRequest enterpriseApiModulePageRequest);

	@RequestMapping(value = "/getByVersionIdAndParentId", method = POST)
	List<EnterpriseApiModule> getByVersionIdAndParentId(@RequestParam(name = "versionId", required = false) Long versionId,
			@RequestParam(name = "parentId", required = false) Long parentId);

	/**
	 * save.
	 *
	 * @param enterpriseApiModule
	 * @return
	 */
	@RequestMapping(value = "/save", method = POST)
	int save(@RequestBody EnterpriseApiModule enterpriseApiModule);

	/**
	 * detail query.
	 *
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "/detail", method = POST)
	EnterpriseApiModule detail(@RequestParam(name = "id", required = false) Long id);

	/**
	 * delete.
	 *
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "/del", method = POST)
	int del(@RequestParam(name = "id", required = false) Long id);

}
