package hb.kg.msg.service;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import hb.kg.common.dao.BaseDao;
import hb.kg.common.service.BaseCRUDService;
import hb.kg.msg.bean.mongo.HBMessageText;
import hb.kg.msg.dao.MessageTextDao;

@Service
public class MessageTextService extends BaseCRUDService<HBMessageText> {
    @Resource
    private MessageTextDao messageTexDao;

    @Override
    public BaseDao<HBMessageText> dao() {
        return messageTexDao;
    }
}
