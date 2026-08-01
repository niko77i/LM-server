package com.lmserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lmserver.entity.common.*;
import com.lmserver.mapper.common.*;
import com.lmserver.service.OptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
@Slf4j
@Service
@RequiredArgsConstructor
public class OptionServiceImpl implements OptionService {

    private final AgentsMapper agentsMapper;
    private final AccountStatusesMapper statusesMapper;
    private final MccLevelsMapper mccLevelsMapper;
    private final SalesPersonsMapper salesPersonsMapper;
    private final RegionsMapper regionsMapper;

    @Override @SuppressWarnings("unchecked")
    public <T> List<T> list(String type, Long ownerId, String platform) {
        return (List<T>) switch (type) {
            case "agents" -> agentsMapper.selectList(new LambdaQueryWrapper<Agents>().eq(Agents::getOwnerId, ownerId));
            case "statuses" -> statusesMapper.selectList(new LambdaQueryWrapper<AccountStatuses>().eq(AccountStatuses::getOwnerId, ownerId).eq(AccountStatuses::getPlatform, platform));
            case "mcc-levels" -> mccLevelsMapper.selectList(new LambdaQueryWrapper<MccLevels>().eq(MccLevels::getOwnerId, ownerId));
            case "sales-persons" -> salesPersonsMapper.selectList(new LambdaQueryWrapper<SalesPersons>().eq(SalesPersons::getOwnerId, ownerId).eq(SalesPersons::getPlatform, platform));
            case "regions" -> regionsMapper.selectList(new LambdaQueryWrapper<Regions>().eq(Regions::getPlatform, platform));
            default -> throw new IllegalArgumentException("Unknown type: " + type);
        };
    }

    @Override public <T> T getById(String type, Long id) {
        return switch (type) {
            case "agents" -> (T) agentsMapper.selectById(id);
            case "statuses" -> (T) statusesMapper.selectById(id);
            case "mcc-levels" -> (T) mccLevelsMapper.selectById(id);
            case "sales-persons" -> (T) salesPersonsMapper.selectById(id);
            case "regions" -> (T) regionsMapper.selectById(id);
            default -> throw new IllegalArgumentException("Unknown type: " + type);
        };
    }

    @Override @SuppressWarnings("unchecked")
    public <T> T create(String type, String name, Long ownerId, String platform) {
        return (T) switch (type) {
            case "agents" -> { Agents a = new Agents(); a.setName(name); a.setOwnerId(ownerId); agentsMapper.insert(a); yield a; }
            case "statuses" -> { AccountStatuses s = new AccountStatuses(); s.setName(name); s.setOwnerId(ownerId); s.setPlatform(platform != null ? platform : "gg"); statusesMapper.insert(s); yield s; }
            case "mcc-levels" -> { MccLevels m = new MccLevels(); m.setName(name); m.setOwnerId(ownerId); mccLevelsMapper.insert(m); yield m; }
            case "sales-persons" -> { SalesPersons sp = new SalesPersons(); sp.setName(name); sp.setOwnerId(ownerId); sp.setPlatform(platform != null ? platform : "gg"); salesPersonsMapper.insert(sp); yield sp; }
            case "regions" -> { Regions r = new Regions(); r.setName(name); r.setPlatform(platform != null ? platform : "gg"); regionsMapper.insert(r); yield r; }
            default -> throw new IllegalArgumentException("Unknown type: " + type);
        };
    }

    @Override @SuppressWarnings("unchecked")
    public <T> T update(String type, Long id, String name) {
        switch (type) {
            case "agents" -> { Agents a = agentsMapper.selectById(id); if (a != null) { a.setName(name); agentsMapper.updateById(a); return (T) a; } }
            case "statuses" -> { AccountStatuses s = statusesMapper.selectById(id); if (s != null) { s.setName(name); statusesMapper.updateById(s); return (T) s; } }
            case "mcc-levels" -> { MccLevels m = mccLevelsMapper.selectById(id); if (m != null) { m.setName(name); mccLevelsMapper.updateById(m); return (T) m; } }
            case "sales-persons" -> { SalesPersons sp = salesPersonsMapper.selectById(id); if (sp != null) { sp.setName(name); salesPersonsMapper.updateById(sp); return (T) sp; } }
            case "regions" -> { Regions r = regionsMapper.selectById(id); if (r != null) { r.setName(name); regionsMapper.updateById(r); return (T) r; } }
        }
        return null;
    }

    /** 删除记录 */
    @Override public void delete(String type, Long id) {
        switch (type) {
            case "agents" -> agentsMapper.deleteById(id);
            case "statuses" -> statusesMapper.deleteById(id);
            case "mcc-levels" -> mccLevelsMapper.deleteById(id);
            case "sales-persons" -> salesPersonsMapper.deleteById(id);
            case "regions" -> regionsMapper.deleteById(id);
        }
    }
}
