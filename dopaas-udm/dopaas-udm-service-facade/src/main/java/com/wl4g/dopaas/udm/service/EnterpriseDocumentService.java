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

package com.wl4g.dopaas.udm.service;

import com.wl4g.component.core.page.PageHolder;
import com.wl4g.component.integration.feign.core.annotation.FeignConsumer;
import com.wl4g.dopaas.common.bean.udm.EnterpriseDocument;
import com.wl4g.dopaas.udm.service.model.EnterpriseDocumentPageRequest;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * {@link EnterpriseDocumentService}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @author vjay
 * @date 2020-01-14
 * @sine v1.0
 * @see
 */
@FeignConsumer(name = "${provider.serviceId.udm-facade:udm-facade}")
@RequestMapping("/enterpriseDocument-service")
public interface EnterpriseDocumentService {

	/**
	 * page query.
	 * 
	 * @return
	 */
	@RequestMapping(value = "/page", method = POST)
	PageHolder<EnterpriseDocument> page(@RequestBody EnterpriseDocumentPageRequest enterpriseDocumentPageRequest);

	/**
	 * save.
	 *
	 * @param enterpriseDocument
	 * @return
	 */
	@RequestMapping(value = "/save", method = POST)
	int save(@RequestBody EnterpriseDocument enterpriseDocument);

	/**
	 * detail query.
	 *
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "/detail", method = POST)
	EnterpriseDocument detail(@RequestParam(name = "id", required = false) Long id);

	/**
	 * delete.
	 *
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "/del", method = POST)
	int del(@RequestParam(name = "id", required = false) Long id);

}
