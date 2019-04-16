package hb.kg.msg.dao;

import org.springframework.stereotype.Repository;

import hb.kg.common.dao.BaseMongoDao;
import hb.kg.msg.bean.mongo.HBMessageText;

@Repository("messageTexDao")
public class MessageTextDao extends BaseMongoDao<HBMessageText> {
}
