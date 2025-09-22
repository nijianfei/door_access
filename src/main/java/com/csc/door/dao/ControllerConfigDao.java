package com.csc.door.dao;

import com.csc.door.dto.ControllerConfigDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.util.List;

@Repository
public class ControllerConfigDao {

    @Value("${local-server.node.ident}")
    private String nodeIdent;
    @Value("${local-server.sql}")
    private String querySql;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public ControllerConfigDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<ControllerConfigDto> findByServerIdente() {
        String sql = String.format(querySql, nodeIdent);
        return jdbcTemplate.query(sql, rowMapper());
    }

    // 结果映射
    private RowMapper<ControllerConfigDto> rowMapper() {
        return (ResultSet rs, int rowNum) -> {
            ControllerConfigDto configDto = new ControllerConfigDto();
            configDto.setControllerNo(rs.getString("控制器编号"));
            configDto.setSn(Long.parseLong(rs.getString("控制器序列号")));
            configDto.setIp(rs.getString("控制器IP"));
            configDto.setSubnetMask(rs.getString("子网掩码"));
            configDto.setGateway(rs.getString("网关"));
            configDto.setServerIp(rs.getString("服务器IP"));
            configDto.setServerPort(rs.getString("服务器端口"));
            configDto.setDoor1(rs.getString("门1"));
            configDto.setDoor2(rs.getString("门2"));
            configDto.setDoor3(rs.getString("门3"));
            configDto.setDoor4(rs.getString("门4"));
            return configDto;
        };
    }
}
