package com.example.demo.dtos.builders;

import com.example.demo.dtos.DeviceDTO;
import com.example.demo.dtos.DeviceDetailsDTO;
import com.example.demo.entities.Device;

public class DeviceBuilder {

    private DeviceBuilder() {
    }

    public static DeviceDTO toDeviceDTO(Device device) {
        return new DeviceDTO(
                device.getId(),
                device.getName(),
                device.getDescription(),
                device.getAddress(),
                device.getMaxConsumption(),
                device.getUserId(),
                device.getStatus(),
                device.getCreatedAt(),
                device.getUpdatedAt()
        );
    }

    public static DeviceDetailsDTO toDeviceDetailsDTO(Device device) {
        return new DeviceDetailsDTO(
                device.getId(),
                device.getName(),
                device.getDescription(),
                device.getAddress(),
                device.getMaxConsumption(),
                device.getUserId(),
                device.getStatus(),
                device.getCreatedAt(),
                device.getUpdatedAt()
        );
    }

    public static Device toEntity(DeviceDetailsDTO deviceDetailsDTO) {
        Device device = new Device(
                deviceDetailsDTO.getName(),
                deviceDetailsDTO.getDescription(),
                deviceDetailsDTO.getAddress(),
                deviceDetailsDTO.getMaxConsumption(),
                deviceDetailsDTO.getStatus()
        );
        device.setUserId(deviceDetailsDTO.getUserId());
        return device;
    }
}