// ${watermark}

${javaSpecs.escapeCopyright(copyright)}

<#assign aDateTime = .now>
<#assign aDate = aDateTime?date>
<#--package name-->
package ${packageName}.service.${moduleName}.impl;

<#--import-->
import com.wl4g.components.data.page.PageModel;
import com.github.pagehelper.PageHelper;
import com.wl4g.components.core.bean.BaseBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import ${packageName}.common.bean.${moduleName}.${entityName?cap_first};
import ${packageName}.dao.${moduleName}.${entityName?cap_first}Dao;
import ${packageName}.service.${moduleName}.${entityName?cap_first}Service;

import static java.util.Objects.isNull;

/**
* {@link ${entityName?cap_first}}
*
* @author ${author}
* @version ${version}
* @Date ${now}
* @since ${since}
*/
@Service
public class ${entityName}ServiceImpl implements ${entityName}Service {

    @Autowired
    private ${entityName}Dao ${entityName?uncap_first}Dao;

    @Override
    public PageModel page(PageModel pm, ${entityName} ${entityName?uncap_first}) {
        pm.page(PageHelper.startPage(pm.getPageNum(), pm.getPageSize(), true));
        pm.setRecords(${entityName?uncap_first}Dao.list(${entityName?uncap_first}));
        return pm;
    }

    public void save(${entityName} ${entityName?uncap_first}) {
        if (isNull(${entityName?uncap_first}.getId())) {
        ${entityName?uncap_first}.preInsert();
        insert(${entityName?uncap_first});
        } else {
        ${entityName?uncap_first}.preUpdate();
        update(${entityName?uncap_first});
        }
    }

    private void insert(${entityName} ${entityName?uncap_first}) {
        ${entityName?uncap_first}Dao.insertSelective(${entityName?uncap_first});
    }

    private void update(${entityName} ${entityName?uncap_first}) {
        ${entityName?uncap_first}Dao.updateByPrimaryKeySelective(${entityName?uncap_first});
    }

    public ${entityName} detail(Integer id) {
        Assert.notNull(id, "id is null");
        return ${entityName?uncap_first}Dao.selectByPrimaryKey(id);
    }

    public void del(Integer id) {
        Assert.notNull(id, "id is null");
        ${entityName} ${entityName?uncap_first} = new ${entityName}();
        ${entityName?uncap_first}.setId(id);
        ${entityName?uncap_first}.setDelFlag(BaseBean.DEL_FLAG_DELETE);
        ${entityName?uncap_first}Dao.updateByPrimaryKeySelective(${entityName?uncap_first});
    }
}