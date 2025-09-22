package com.csc.door.utils;

import com.alibaba.fastjson2.JSONObject;
import com.csc.door.dto.ControllerConfigDto;
import com.csc.door.dto.ControllerConfigsDto;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;

import java.io.File;

// XML工具类
public class XMLUtil {

    /**
     * 从XML文件解析为Java对象
     */
    public static ControllerConfigsDto parseXmlFromFile(File file)  {
        try {
            JAXBContext context = JAXBContext.newInstance(ControllerConfigsDto.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            return (ControllerConfigsDto) unmarshaller.unmarshal(file);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 将Java对象序列化为XML文件
     */
    public static void writeXmlToFile(File file, ControllerConfigsDto data)  {
        try {
            JAXBContext context = JAXBContext.newInstance(ControllerConfigsDto.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
            marshaller.marshal(data, file);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    // 使用示例
    public static void main(String[] args) {
        String FILE_PATH = "C:/AC/Config/ControlsConfig.xml";
        try {
            // 从文件解析XML
            ControllerConfigsDto data = parseXmlFromFile(new File(FILE_PATH));
            // 修改数据示例
            if (data.getControllers() != null && !data.getControllers().isEmpty()) {
                for (ControllerConfigDto controller : data.getControllers()) {
                    System.out.println(JSONObject.toJSONString(controller));
                }
            }

            // 写回文件
            writeXmlToFile(new File(FILE_PATH), data);
            System.out.println("XML文件更新成功！");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}