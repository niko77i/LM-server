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
            case "agents" -> agentsMapper.selectList(
                    new LambdaQueryWrapper<Agents>().eq(Agents::getOwnerId, ownerId));
            case "statuses" -> statusesMapper.selectList(
                    new LambdaQueryWrapper<AccountStatuses>().eq(AccountStatuses::getPlatform, platform));
            case "mcc-levels" -> mccLevelsMapper.selectList(
                    new LambdaQueryWrapper<MccLevels>().eq(MccLevels::getOwnerId, ownerId));
            case "sales-persons" -> salesPersonsMapper.selectList(
                    new LambdaQueryWrapper<SalesPersons>().eq(SalesPersons::getPlatform, platform));
            case "regions" -> regionsMapper.selectList(
                    new LambdaQueryWrapper<Regions>().eq(Regions::getPlatform, platform));
            default -> throw new IllegalArgumentException("Unknown type: " + type);
        };
    }

    @Override @SuppressWarnings("unchecked")
    public <T> T create(String type, String name, Long ownerId, String platform) {
        // 检查重名
        checkDuplicate(type, name, ownerId, platform, null);
        return (T) switch (type) {
            case "agents" -> { Agents a = new Agents(); a.setName(name); a.setOwnerId(ownerId); agentsMapper.insert(a); yield a; }
            case "statuses" -> { AccountStatuses s = new AccountStatuses(); s.setName(name); s.setPlatform(platform != null ? platform : "gg"); statusesMapper.insert(s); yield s; }
            case "mcc-levels" -> { MccLevels m = new MccLevels(); m.setName(name); m.setOwnerId(ownerId); mccLevelsMapper.insert(m); yield m; }
            case "sales-persons" -> { SalesPersons sp = new SalesPersons(); sp.setName(name); sp.setPlatform(platform != null ? platform : "gg"); salesPersonsMapper.insert(sp); yield sp; }
            case "regions" -> { Regions r = new Regions(); r.setName(name); r.setPlatform(platform != null ? platform : "gg"); regionsMapper.insert(r); yield r; }
            default -> throw new IllegalArgumentException("Unknown type: " + type);
        };
    }

    @Override @SuppressWarnings("unchecked")
    public <T> T update(String type, Long id, String name, Long userId) {
        // 检查重名（排除自己）
        Object existing = getById(type, id);
        if (existing == null) return null;
        if (!isOwner(existing, userId)) {
            log.warn("权限拒绝: 用户{} 尝试修改 {} id={}", userId, type, id);
            return null;
        }
        checkDuplicate(type, name, null, null, id);

        switch (type) {
            case "agents" -> { Agents a = (Agents) existing; a.setName(name); agentsMapper.updateById(a); return (T) a; }
            case "statuses" -> { AccountStatuses s = (AccountStatuses) existing; s.setName(name); statusesMapper.updateById(s); return (T) s; }
            case "mcc-levels" -> { MccLevels m = (MccLevels) existing; m.setName(name); mccLevelsMapper.updateById(m); return (T) m; }
            case "sales-persons" -> { SalesPersons sp = (SalesPersons) existing; sp.setName(name); salesPersonsMapper.updateById(sp); return (T) sp; }
            case "regions" -> { Regions r = (Regions) existing; r.setName(name); regionsMapper.updateById(r); return (T) r; }
        }
        return null;
    }

    @Override
    public void delete(String type, Long id, Long userId) {
        Object existing = getById(type, id);
        if (existing == null) return;
        if (!isOwner(existing, userId)) {
            log.warn("权限拒绝: 用户{} 尝试删除 {} id={}", userId, type, id);
            return;
        }
        switch (type) {
            case "agents" -> agentsMapper.deleteById(id);
            case "statuses" -> statusesMapper.deleteById(id);
            case "mcc-levels" -> mccLevelsMapper.deleteById(id);
            case "sales-persons" -> salesPersonsMapper.deleteById(id);
            case "regions" -> regionsMapper.deleteById(id);
        }
    }

    @Override @SuppressWarnings("unchecked")
    public <T> T getById(String type, Long id) {
        return (T) switch (type) {
            case "agents" -> agentsMapper.selectById(id);
            case "statuses" -> statusesMapper.selectById(id);
            case "mcc-levels" -> mccLevelsMapper.selectById(id);
            case "sales-persons" -> salesPersonsMapper.selectById(id);
            case "regions" -> regionsMapper.selectById(id);
            default -> throw new IllegalArgumentException("Unknown type: " + type);
        };
    }

    // ── 辅助方法 ──

    /** 检查是否为记录所有者。agents/mcc-levels 按 owner_id，statuses/sales-persons/regions 按平台隔离 */
    private boolean isOwner(Object entity, Long userId) {
        if (userId == null) return false;
        if (entity instanceof Agents a) return a.getOwnerId() == null || a.getOwnerId().equals(userId);
        if (entity instanceof MccLevels m) return m.getOwnerId() == null || m.getOwnerId().equals(userId);
        // statuses/sales-persons/regions 按平台隔离，允许操作
        return true;
    }

    /** 检查同类型下是否存在同名记录 */
    private void checkDuplicate(String type, String name, Long ownerId, String platform, Long excludeId) {
        boolean exists = switch (type) {
            case "agents" -> agentsMapper.selectCount(
                    new LambdaQueryWrapper<Agents>().eq(Agents::getName, name)
                            .eq(ownerId != null, Agents::getOwnerId, ownerId)
                            .ne(excludeId != null, Agents::getId, excludeId)) > 0;
            case "statuses" -> statusesMapper.selectCount(
                    new LambdaQueryWrapper<AccountStatuses>().eq(AccountStatuses::getName, name)
                            .eq(platform != null, AccountStatuses::getPlatform, platform)
                            .ne(excludeId != null, AccountStatuses::getId, excludeId)) > 0;
            case "mcc-levels" -> mccLevelsMapper.selectCount(
                    new LambdaQueryWrapper<MccLevels>().eq(MccLevels::getName, name)
                            .eq(ownerId != null, MccLevels::getOwnerId, ownerId)
                            .ne(excludeId != null, MccLevels::getId, excludeId)) > 0;
            case "sales-persons" -> salesPersonsMapper.selectCount(
                    new LambdaQueryWrapper<SalesPersons>().eq(SalesPersons::getName, name)
                            .eq(platform != null, SalesPersons::getPlatform, platform)
                            .ne(excludeId != null, SalesPersons::getId, excludeId)) > 0;
            case "regions" -> regionsMapper.selectCount(
                    new LambdaQueryWrapper<Regions>().eq(Regions::getName, name)
                            .eq(platform != null, Regions::getPlatform, platform)
                            .ne(excludeId != null, Regions::getId, excludeId)) > 0;
            default -> false;
        };
        if (exists) throw new IllegalArgumentException("名称「" + name + "」已存在");
    }
}
