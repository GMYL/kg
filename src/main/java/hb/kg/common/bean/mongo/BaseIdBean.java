package hb.kg.common.bean.mongo;

public interface BaseIdBean {
    public String getId();

    public void prepareHBBean();

    public void increaseVisit();
}
