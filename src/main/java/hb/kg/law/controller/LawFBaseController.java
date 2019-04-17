package hb.kg.law.controller;

import org.apache.commons.lang3.StringUtils;

import hb.kg.common.bean.mongo.BaseMgBean;
import hb.kg.common.controller.BaseCRUDController;

public abstract class LawFBaseController<T extends BaseMgBean<T>> extends BaseCRUDController<T> {
    @Override
    protected String getSortKey(T object) {
        return StringUtils.isNotEmpty(object.getSortKey()) ? object.getSortKey() : "sequence";
    }
}
