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
package com.wl4g.dopaas.cmdb.initializer.installer.javaenv;

import com.wl4g.dopaas.cmdb.initializer.installer.InstallerConfiguration;
import com.wl4g.dopaas.cmdb.initializer.installer.RemovableSoftInstaller;
import com.wl4g.dopaas.cmdb.initializer.installer.javaenv.OpenJdkV11xSoftInstaller.OpenJdkV11xConfiguration;

/**
 * {@link OpenJdkV8xSoftInstaller}
 *
 * @author Wangl.sir <wanglsir@gmail.com, 983708408@qq.com>
 * @version v1.0 2020-07-23
 * @since
 */
public abstract class OpenJdkV8xSoftInstaller extends RemovableSoftInstaller<OpenJdkV8xConfiguration> {

	/**
	 * {@link OpenJdkV8xConfiguration}
	 * 
	 * @since
	 */
	public static class OpenJdkV8xConfiguration extends InstallerConfiguration {

		public OpenJdkV8xConfiguration(SoftVersion version) {
			super(version);
		}

	}

}