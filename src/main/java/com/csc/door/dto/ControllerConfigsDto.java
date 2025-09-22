package com.csc.door.dto;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@XmlRootElement(name = "NewDataSet")
public class ControllerConfigsDto {
    private List<ControllerConfigDto> controllers;
    private Map<String,ControllerConfigDto> controllerMap;

    @XmlElement(name = "控制器表")
    public List<ControllerConfigDto> getControllers() {
        return controllers;
    }

    public void setControllers(List<ControllerConfigDto> controllers) {
        this.controllers = controllers;
    }

    public ControllerConfigDto get(String ip){
        if (controllerMap == null) {
            synchronized (ControllerConfigsDto.class){
                if (controllerMap == null) {
                    controllerMap = controllers.stream().collect(Collectors.toMap(ControllerConfigDto::getIp, Function.identity()));
                }
            }
        }
        return controllerMap.get(ip);
    }
}