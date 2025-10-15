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

@Service
public class DeviceService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceService.class);
    private final DeviceRepository deviceRepository;

    @Autowired
    public DeviceService(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
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
        // Check if device name already exists
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

        // Check if device name is being changed and if it's already taken
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