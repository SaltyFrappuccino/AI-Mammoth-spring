package ru.sber.agent.webservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class Dictionary {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public Map<String, String> getDictionary() {
        String sql = "SELECT key, value FROM hac.dictionary";
        Map<String, String> map = new HashMap<>();
        jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
            map.put(rs.getString("key"), rs.getString("value"));
            return map;
        });
        return map;
    }

    public void saveDict(@RequestBody Map<String, String> dict) {
        log.info("/saveDict, {}", dict);
        for (Map.Entry<String, String> entry : dict.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            String sql = "INSERT INTO hac.dictionary (key, value) VALUES (?, ?) ON CONFLICT (key) DO UPDATE SET value = ?";
            jdbcTemplate.update(sql, key, value, value);
        }
    }
}
