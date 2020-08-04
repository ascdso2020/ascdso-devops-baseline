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
package com.wl4g.devops.erm.initializer.installer.golangenv;

import com.wl4g.devops.erm.initializer.installer.InstallerConfiguration;
import com.wl4g.devops.erm.initializer.installer.RemovableSoftInstaller;
import com.wl4g.devops.erm.initializer.installer.k8s.DockerCEK8sClusterV1xSoftInstaller.DockerCEK8sClusterV1xConfiguration;

/**
 * {@link GolangV13xSoftInstaller}
 *
 * @author Wangl.sir <wanglsir@gmail.com, 983708408@qq.com>
 * @version v1.0 2020-07-23
 * @since
 */
public abstract class GolangV13xSoftInstaller extends RemovableSoftInstaller {

	/**
	 * {@link GolangV13xConfiguration}
	 * 
	 * @since
	 */
	public static class GolangV13xConfiguration extends InstallerConfiguration {

	}

}