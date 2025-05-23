package com.leon.datalink.web.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.leon.datalink.core.exception.KvStorageException;
import com.leon.datalink.core.storage.DatalinkKvStorage;
import com.leon.datalink.core.storage.KvStorage;
import com.leon.datalink.core.evn.EnvUtil;
import com.leon.datalink.core.utils.JacksonUtils;
import com.leon.datalink.core.utils.SnowflakeIdWorker;
import com.leon.datalink.core.utils.StringUtils;
import com.leon.datalink.transform.script.Script;
import com.leon.datalink.web.service.ScriptService;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @ClassNameResourceManager
 * @Description
 * @Author Leon
 * @Date2022/4/2 15:03
 * @Version V1.0
 **/
@Service
public class ScriptServiceImpl implements ScriptService {

    /**
     * 资源列表
     */
    private final ConcurrentHashMap<String, Script> scriptList = new ConcurrentHashMap<>();

    /**
     * key value storage
     */
    private final KvStorage kvStorage;

    /**
     * 脚本持久化路径
     */
    private final static String SCRIPT_PATH = "/script";

    public ScriptServiceImpl() throws Exception {

        // init storage
        this.kvStorage = new DatalinkKvStorage(EnvUtil.getStoragePath() + SCRIPT_PATH);

        // read script list form storage
        if (this.kvStorage.allKeys().size() <= 0) return;
        for (byte[] key : this.kvStorage.allKeys()) {
            String scriptId = new String(key);
            byte[] value = this.kvStorage.get(key);
            Script script = JacksonUtils.toObj(value, Script.class);
            scriptList.put(scriptId, script);
        }

    }

    @Override
    public Script get(String scriptId) {
        return scriptList.get(scriptId);
    }

    @Override
    public void add(Script script) throws KvStorageException {
        if (StringUtils.isEmpty(script.getScriptId())) script.setScriptId(SnowflakeIdWorker.getId());
        this.kvStorage.put(script.getScriptId().getBytes(), JacksonUtils.toJsonBytes(script));
        scriptList.put(script.getScriptId(), script);
    }

    @Override
    public void remove(String scriptId) throws KvStorageException {
        this.kvStorage.delete(scriptId.getBytes());
        scriptList.remove(scriptId);
    }

    @Override
    public void update(Script script) throws KvStorageException {
        this.kvStorage.put(script.getScriptId().getBytes(), JacksonUtils.toJsonBytes(script));
        scriptList.put(script.getScriptId(), script);
    }

    @Override
    public List<Script> list(Script script) {
        Stream<Script> stream = scriptList.values().stream();
        if (null != script) {
            if (!StringUtils.isEmpty(script.getScriptName())) {
                stream = stream.filter(s -> s.getScriptName().contains(script.getScriptName()));
            }
            if (!StringUtils.isEmpty(script.getScriptLanguage())) {
                stream = stream.filter(s -> s.getScriptLanguage().equals(script.getScriptLanguage()));
            }
        }
        return CollectionUtil.reverse(stream.sorted(Comparator.comparingLong(item -> Long.parseLong(item.getScriptId()))).collect(Collectors.toList()));
    }

    @Override
    public int getCount(Script script) {
        return this.list(script).size();
    }


}
