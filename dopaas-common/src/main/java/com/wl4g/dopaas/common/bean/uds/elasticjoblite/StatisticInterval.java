/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wl4g.dopaas.common.bean.uds.elasticjoblite;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Statistic interval.
 */
@Getter
@RequiredArgsConstructor
public enum StatisticInterval {

	MINUTE("0 * * * * ?"),

	HOUR("0 0 * * * ?"),

	DAY("0 0 0 * * ?");

	private final String cron;
}
