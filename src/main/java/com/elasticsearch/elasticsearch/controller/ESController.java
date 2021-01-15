package com.elasticsearch.elasticsearch.controller;

import com.elasticsearch.elasticsearch.document.MessageInfo;
import com.elasticsearch.elasticsearch.document.MessageTo;
import com.elasticsearch.elasticsearch.service.ESService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequestMapping("/es")
@RequiredArgsConstructor
public class ESController {
    private final ESService esService;
    private static Long id = 1256748967462588L;

    @RequestMapping("/createIndexAndCreateMapping")
    public boolean createIndexAndCreateMapping() {
        return esService.createIndexAndCreateMapping("messageinfo", new MessageInfo().getClass(), true, 1, 0);
    }

    @RequestMapping("/deleteIndex")
    public boolean deleteIndex() {
        return esService.deleteIndex("messageinfo");
    }

    @RequestMapping("/createDocument")
    public boolean createDocument(@RequestBody MessageInfo messageInfo) throws Exception {
//        MessageInfo messageInfo = new MessageInfo(156748968789887L, "嘎尔尕尔哥v阿尔高个人好吧", new Date(), 12567489657L, "保卫华北不同人物人头人肉", 1, "测试新增文档1", 25674657684L, "发送人", 156748654L, "患者姓名", 1567489674896L, 15674865879867986L, "第一个添加的文档", null, null, null, null);
        return esService.createDocument("messageinfo", messageInfo.getMessageId(), messageInfo);
    }

    @RequestMapping("/updateDocument")
    public boolean updateDocument(@RequestParam("id") Long id, @RequestBody Map map) {
        return esService.updateDocument("messageinfo", id, map);
    }

    @RequestMapping("deleteDocumentById")
    public boolean deleteDocumentById(@RequestParam("id") Long id) {
        return esService.deleteDocumentById("messageinfo", id);
    }

    @RequestMapping("/getDocumentById")
    public Map getDocumentById(@RequestParam("id") String id) {
        return esService.getDocumentById("messageinfo", id);
    }

    @RequestMapping("/bulkCreateDocument")
    public boolean bulkCreateDocument() {
        Map<Long, MessageInfo> map = new HashMap<>();
        for (int i = 0; i < 5; i++) {
            List<MessageTo> messageTos = new ArrayList<>();
            for (int j = 0; j < 2; j++) {
                messageTos.add(new MessageTo(265465415465L, id, 5467467464986L, 1, new Date(), 0, 0, "新增messageTo大约还好" + j));
            }
            MessageInfo messageInfo = new MessageInfo(id, "haohaoaa" + id, new Date(), id, "保卫华北buhao不同bu人物人头人肉" + id, 1, "测试新增文档1" + id, id, "发送人" + id, id, "患者姓名" + id, id, id, "第一个添加的文档" + id, null, null, null, messageTos);
            map.put(id, messageInfo);
            id++;
        }
        return esService.bulkCreateDocument("messageinfo", map);
    }

    @RequestMapping("/bulkUpdateDocument")
    public boolean bulkUpdateDocument() {
        Map<Long, MessageInfo> map = new HashMap<>();
        Long id1 = 1256748997462688L;
        for (int i = 0; i < 2; i++) {
            MessageInfo messageInfo = new MessageInfo(id1, "嘎尔尕尔哥v阿尔高个人好吧" + id1, new Date(), id1, "保卫华北不同人物人头人肉" + id1, 1, "测试新增文档1" + id1, id1, "发送人" + id1, id1, "患者姓名" + id1, id1, id1, "第一个添加的文档" + id1, null, null, null, null);
            map.put(id1, messageInfo);
            id1++;
        }
        for (int j = 3; j >= 0; j--) {
            MessageInfo messageInfo = new MessageInfo(id, id + "嘎尔尕尔哥v阿尔高个人好吧", new Date(), id, id + "保卫华北不同人物人头人肉", 1, id + "测试新增文档1", id, id + "发送人", id, id + "患者姓名", id, id, id + "第一个添加的文档", null, null, null, null);
            map.put(id, messageInfo);
            id++;
        }
        return esService.bulkUpdateDocument("messageinfo", map);
    }

    @RequestMapping("/bulkDeleteDocumentByIds")
    public boolean bulkDeleteDocumentByIds() {
        ArrayList<Long> longs = new ArrayList<>();
        Long id1 = 1256748967463688L;
        for (int i = 0; i < 2; i++) {
            longs.add(id1);
            id1++;
        }
        return esService.bulkDeleteDocumentByIds("messageinfo", longs);
    }

    @RequestMapping("/textSearch")
    public List<Map<String, Object>> textSearch(@RequestParam("value") String value, @RequestParam(value = "current", defaultValue = "1") int current, @RequestParam(value = "size", defaultValue = "10") int size) {
        return esService.textSearch("messageinfo", "createTime", current, size, value, "bizCondition");
    }

    @RequestMapping("/rangeMathQuery")
    public List<Map<String, Object>> rangeMathQuery(@RequestParam("value") String value, @RequestParam(value = "start") String start, @RequestParam(value = "end") String end) {
        List<Map<String, Map<String, String>>> list = new ArrayList<>();
        Map<String, Map<String, String>> hashMap = new HashMap<>();
        Map<String, String> values = new HashMap<>();
        values.put("start", start);
        values.put("end", end);
        hashMap.put("createTime", values);
        list.add(hashMap);
        return esService.rangeMathQuery("messageinfo", "createTime", 1, 10, list, value, "bizCondition");
    }

    @RequestMapping("/textCount")
    public List<Map<String, Object>> textCount(@RequestParam("value") String value) {
        return esService.textSearch("messageinfo", "createTime", 1, 100, value, "fromPersonName");
    }
}

