package com.csc.door.config;


import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import com.csc.door.dao.ControllerConfigDao;
import com.csc.door.dto.ControllerConfigDto;
import com.csc.door.dto.ControllerConfigsDto;
import com.csc.door.utils.XMLUtil;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Unmarshaller;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.util.List;

@Slf4j
@Configuration
public class ControllerConfig {

    @Autowired
    private ControllerConfigDao controllerConfigDao;

    @Bean(name = "controllerConfigDto")
    public ControllerConfigsDto loadControllerConfig() throws Exception {
        ClassPathResource resource = new ClassPathResource("ControlsConfig.xml");
        File file = resource.getFile();
        //db可用 使用db数据,并更新xml
        List<ControllerConfigDto> configDtos = null;
        try {
            configDtos = controllerConfigDao.findByServerIdente();
        } catch (Exception e) {
            log.warn("查询DB异常,error:{}", e.getMessage(), e);
        }
        if (configDtos == null) {
            JAXBContext context = JAXBContext.newInstance(ControllerConfigsDto.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            ControllerConfigsDto unmarshal = (ControllerConfigsDto) unmarshaller.unmarshal(resource.getInputStream());
            log.warn("查询DB异常,使用本地xml({})配置:{}{}",file.getAbsolutePath(),System.lineSeparator(), JSONObject.toJSONString(unmarshal, JSONWriter.Feature.PrettyFormat));
            return unmarshal;
        }
        ControllerConfigsDto dto = new ControllerConfigsDto();
        dto.setControllers(configDtos);
        try {
            log.info("查询db控制器配置写入本地xml文件_start");
            XMLUtil.writeXmlToFile(file, dto);
            log.info("查询db控制器配置写入本地xml文件_end");
        } catch (Exception e) {
            log.warn("查询db控制器配置写入本地xml文件失败:{}", e.getMessage(), e);
        }
        log.info("查询DB正常,使用远程DB配置:{}{}",System.lineSeparator(), JSONObject.toJSONString(dto, JSONWriter.Feature.PrettyFormat));
        return dto;
    }
}
