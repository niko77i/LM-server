package com.lmserver.service.impl;

import com.lmserver.entity.common.*;
import com.lmserver.repository.common.*;
import com.lmserver.service.OptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OptionServiceImpl implements OptionService {

    private final AgentsRepository agentsRepository;
    private final AccountStatusesRepository accountStatusesRepository;
    private final MccLevelsRepository mccLevelsRepository;
    private final SalesPersonsRepository salesPersonsRepository;
    private final RegionsRepository regionsRepository;

    @Override
    @SuppressWarnings("unchecked")
    public <T> List<T> list(String type, Long ownerId, String platform) {
        return (List<T>) switch (type) {
            case "agents" -> agentsRepository.findByOwnerIdOrderByCreatedAtDesc(ownerId);
            case "statuses" -> accountStatusesRepository.findByOwnerIdAndPlatformOrderByCreatedAtDesc(ownerId, platform);
            case "mcc-levels" -> mccLevelsRepository.findByOwnerIdOrderByCreatedAtDesc(ownerId);
            case "sales-persons" -> salesPersonsRepository.findByOwnerIdAndPlatformOrderByCreatedAtDesc(ownerId, platform);
            case "regions" -> regionsRepository.findByPlatform(platform);
            default -> throw new IllegalArgumentException("Unknown option type: " + type);
        };
    }

    @Override
    public <T> T getById(String type, Long id) {
        return switch (type) {
            case "agents" -> (T) agentsRepository.findById(id).orElse(null);
            case "statuses" -> (T) accountStatusesRepository.findById(id).orElse(null);
            case "mcc-levels" -> (T) mccLevelsRepository.findById(id).orElse(null);
            case "sales-persons" -> (T) salesPersonsRepository.findById(id).orElse(null);
            case "regions" -> (T) regionsRepository.findById(id).orElse(null);
            default -> throw new IllegalArgumentException("Unknown option type: " + type);
        };
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T create(String type, String name, Long ownerId, String platform) {
        return (T) switch (type) {
            case "agents" -> {
                Agents a = new Agents();
                a.setName(name);
                a.setOwnerId(ownerId);
                a.setCreatedAt(LocalDateTime.now());
                yield agentsRepository.save(a);
            }
            case "statuses" -> {
                AccountStatuses s = new AccountStatuses();
                s.setName(name);
                s.setOwnerId(ownerId);
                s.setPlatform(platform != null ? platform : "gg");
                s.setCreatedAt(LocalDateTime.now());
                yield accountStatusesRepository.save(s);
            }
            case "mcc-levels" -> {
                MccLevels m = new MccLevels();
                m.setName(name);
                m.setOwnerId(ownerId);
                m.setCreatedAt(LocalDateTime.now());
                yield mccLevelsRepository.save(m);
            }
            case "sales-persons" -> {
                SalesPersons sp = new SalesPersons();
                sp.setName(name);
                sp.setOwnerId(ownerId);
                sp.setPlatform(platform != null ? platform : "gg");
                sp.setCreatedAt(LocalDateTime.now());
                yield salesPersonsRepository.save(sp);
            }
            case "regions" -> {
                Regions r = new Regions();
                r.setName(name);
                r.setPlatform(platform != null ? platform : "gg");
                yield regionsRepository.save(r);
            }
            default -> throw new IllegalArgumentException("Unknown option type: " + type);
        };
    }

    @Override
    public <T> T update(String type, Long id, String name) {
        switch (type) {
            case "agents" -> {
                Agents a = agentsRepository.findById(id).orElse(null);
                if (a != null) { a.setName(name); return (T) agentsRepository.save(a); }
            }
            case "statuses" -> {
                AccountStatuses s = accountStatusesRepository.findById(id).orElse(null);
                if (s != null) { s.setName(name); return (T) accountStatusesRepository.save(s); }
            }
            case "mcc-levels" -> {
                MccLevels m = mccLevelsRepository.findById(id).orElse(null);
                if (m != null) { m.setName(name); return (T) mccLevelsRepository.save(m); }
            }
            case "sales-persons" -> {
                SalesPersons sp = salesPersonsRepository.findById(id).orElse(null);
                if (sp != null) { sp.setName(name); return (T) salesPersonsRepository.save(sp); }
            }
            case "regions" -> {
                Regions r = regionsRepository.findById(id).orElse(null);
                if (r != null) { r.setName(name); return (T) regionsRepository.save(r); }
            }
        }
        return null;
    }

    @Override
    public void delete(String type, Long id) {
        switch (type) {
            case "agents" -> agentsRepository.deleteById(id);
            case "statuses" -> accountStatusesRepository.deleteById(id);
            case "mcc-levels" -> mccLevelsRepository.deleteById(id);
            case "sales-persons" -> salesPersonsRepository.deleteById(id);
            case "regions" -> regionsRepository.deleteById(id);
        }
    }
}
