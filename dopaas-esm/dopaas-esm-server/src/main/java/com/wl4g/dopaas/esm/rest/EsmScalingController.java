/*
 * Copyright 2017 ~ 2050 the original author or authors <Wanglsir@gmail.com, 983708408@qq.com>.
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
package com.wl4g.dopaas.esm.rest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.wl4g.dopaas.common.web.BaseController;
import com.wl4g.dopaas.common.web.RespBase;

@Controller
@RequestMapping("/api/")
public class EsmScalingController extends BaseController {

	@RequestMapping("scaling")
	public RespBase<?> scaling() {
		RespBase<?> resp = RespBase.create();
		// TODO
		//
		return resp;
	}

}