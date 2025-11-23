package com.example.demo.services;

import com.example.demo.dtos.DeviceDTO;
import com.example.demo.dtos.DeviceDetailsDTO;
import com.example.demo.dtos.builders.DeviceBuilder;
import com.example.demo.entities.Device;
import com.example.demo.entities.DeviceStatus;
import com.example.demo.handlers.exceptions.model.CustomException;
import com.example.demo.handlers.exceptions.model.ResourceNotFoundException;
import com.example.demo.repositories.DeviceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import java.util.HashMap;
import java.util.Map;
import com.example.demo.config.RabbitConfig;

@Service
public class DeviceService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceService.class);
    private final DeviceRepository deviceRepository;
    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public DeviceService(DeviceRepository deviceRepository, RabbitTemplate rabbitTemplate) {
        this.deviceRepository = deviceRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    public List<DeviceDTO> findDevices() {
        List<Device> deviceList = deviceRepository.findAll();
        return deviceList.stream()
                .map(DeviceBuilder::toDeviceDTO)
                .collect(Collectors.toList());
    }

    public DeviceDetailsDTO findDeviceById(UUID id) {
        Optional<Device> deviceOptional = deviceRepository.findById(id);
        if (!deviceOptional.isPresent()) {
            LOGGER.error("Device with id {} was not found in db", id);
            throw new ResourceNotFoundException(Device.class.getSimpleName() + " with id: " + id);
        }
        return DeviceBuilder.toDeviceDetailsDTO(deviceOptional.get());
    }

    public List<DeviceDTO> findDevicesByUserId(UUID userId) {
        List<Device> deviceList = deviceRepository.findByUserId(userId);
        LOGGER.debug("Found {} devices for user {}", deviceList.size(), userId);
        return deviceList.stream()
                .map(DeviceBuilder::toDeviceDTO)
                .collect(Collectors.toList());
    }

    public List<DeviceDTO> findDevicesByStatus(DeviceStatus status) {
        List<Device> deviceList = deviceRepository.findByStatus(status);
        return deviceList.stream()
                .map(DeviceBuilder::toDeviceDTO)
                .collect(Collectors.toList());
    }

    public List<DeviceDTO> findUnassignedDevices() {
        List<Device> deviceList = deviceRepository.findByUserIdIsNull();
        LOGGER.debug("Found {} unassigned devices", deviceList.size());
        return deviceList.stream()
                .map(DeviceBuilder::toDeviceDTO)
                .collect(Collectors.toList());
    }

    public UUID insert(DeviceDetailsDTO deviceDTO) {
        if (deviceRepository.existsByName(deviceDTO.getName())) {
            LOGGER.error("Device name {} already exists", deviceDTO.getName());
            throw new CustomException(
                    "Device name already exists",
                    HttpStatus.CONFLICT,
                    Device.class.getSimpleName(),
                    List.of("Device name " + deviceDTO.getName() + " is already taken")
            );
        }

        Device device = DeviceBuilder.toEntity(deviceDTO);
        device = deviceRepository.save(device);

        // --- SYNC LOGIC START ---
        try {
            Map<String, Object> syncMsg = new HashMap<>();
            syncMsg.put("action", "create_device");
            syncMsg.put("deviceId", device.getId());
            syncMsg.put("userId", device.getUserId());
            syncMsg.put("maxConsumption", device.getMaxConsumption());

            rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE_NAME, "", syncMsg);
            LOGGER.debug("Sent sync message for device creation: {}", device.getId());
        } catch (Exception e) {
            LOGGER.error("Failed to send sync message", e);
        }
        // --- SYNC LOGIC END ---
        LOGGER.debug("Device with id {} was inserted in db", device.getId());
        return device.getId();
    }

    public void update(UUID id, DeviceDetailsDTO deviceDTO) {
        Optional<Device> deviceOptional = deviceRepository.findById(id);
        if (!deviceOptional.isPresent()) {
            LOGGER.error("Device with id {} was not found in db", id);
            throw new ResourceNotFoundException(Device.class.getSimpleName() + " with id: " + id);
        }

        Device device = deviceOptional.get();

        if (!device.getName().equals(deviceDTO.getName()) &&
                deviceRepository.existsByName(deviceDTO.getName())) {
            LOGGER.error("Device name {} already exists", deviceDTO.getName());
            throw new CustomException(
                    "Device name already exists",
                    HttpStatus.CONFLICT,
                    Device.class.getSimpleName(),
                    List.of("Device name " + deviceDTO.getName() + " is already taken")
            );
        }

        device.setName(deviceDTO.getName());
        device.setDescription(deviceDTO.getDescription());
        device.setAddress(deviceDTO.getAddress());
        device.setMaxConsumption(deviceDTO.getMaxConsumption());
        device.setStatus(deviceDTO.getStatus());
        device.setUserId(deviceDTO.getUserId());

        deviceRepository.save(device);
        LOGGER.debug("Device with id {} was updated in db", id);
    }

    public void delete(UUID id) {
        Optional<Device> deviceOptional = deviceRepository.findById(id);
        if (!deviceOptional.isPresent()) {
            LOGGER.error("Device with id {} was not found in db", id);
            throw new ResourceNotFoundException(Device.class.getSimpleName() + " with id: " + id);
        }
        deviceRepository.deleteById(id);

        // --- SYNC LOGIC START ---
        try {
            Map<String, Object> syncMsg = new HashMap<>();
            syncMsg.put("action", "delete_device");
            syncMsg.put("deviceId", id);

            rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE_NAME, "", syncMsg);
        } catch (Exception e) {
             LOGGER.error("Failed to send sync message", e);
        }
        // --- SYNC LOGIC END ---

        LOGGER.debug("Device with id {} was deleted from db", id);
    }

    public void assignDeviceToUser(UUID deviceId, UUID userId) {
        Optional<Device> deviceOptional = deviceRepository.findById(deviceId);
        if (!deviceOptional.isPresent()) {
            LOGGER.error("Device with id {} was not found in db", deviceId);
            throw new ResourceNotFoundException(Device.class.getSimpleName() + " with id: " + deviceId);
        }

        Device device = deviceOptional.get();
        device.setUserId(userId);
        deviceRepository.save(device);
        LOGGER.debug("Device with id {} was assigned to user {}", deviceId, userId);
    }

    public void unassignDevice(UUID deviceId) {
        Optional<Device> deviceOptional = deviceRepository.findById(deviceId);
        if (!deviceOptional.isPresent()) {
            LOGGER.error("Device with id {} was not found in db", deviceId);
            throw new ResourceNotFoundException(Device.class.getSimpleName() + " with id: " + deviceId);
        }

        Device device = deviceOptional.get();
        device.setUserId(null);
        deviceRepository.save(device);
        LOGGER.debug("Device with id {} was unassigned", deviceId);
    }

    public long countDevicesByUser(UUID userId) {
        return deviceRepository.countByUserId(userId);
    }
}