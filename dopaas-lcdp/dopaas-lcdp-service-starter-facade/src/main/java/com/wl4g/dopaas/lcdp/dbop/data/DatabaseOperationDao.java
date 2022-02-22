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
package com.wl4g.dopaas.lcdp.dbop.data;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.wl4g.dopaas.common.bean.lcdp.dbop.DatabaseOperation;

/**
 * {@link DatabaseOperationDao}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2021-08-15 v1.0.0
 * @since v1.0.0
 */
public interface DatabaseOperationDao {

    int deleteByPrimaryKey(Long id);

    int insert(DatabaseOperation record);

    int insertSelective(DatabaseOperation record);

    DatabaseOperation selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(DatabaseOperation record);

    int updateByPrimaryKey(DatabaseOperation record);

    List<DatabaseOperation> list(@Param("name") String name);

}
