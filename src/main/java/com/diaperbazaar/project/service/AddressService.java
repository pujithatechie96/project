package com.diaperbazaar.project.service;

import com.diaperbazaar.project.dto.AddressDTO;
import com.diaperbazaar.project.entity.Address;
import com.diaperbazaar.project.repository.AddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;
    private static final int MAX_ADDRESSES = 15;

    public List<AddressDTO> getAddressesByUserId(Long userId) {
        return addressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public AddressDTO getAddressById(Long id, Long userId) {
        Address address = addressRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Address not found"));
        return toDTO(address);
    }

    @Transactional
    public AddressDTO createAddress(AddressDTO dto, Long userId) {
        // Check max addresses limit
        long count = addressRepository.countByUserId(userId);
        if (count >= MAX_ADDRESSES) {
            throw new RuntimeException("Maximum " + MAX_ADDRESSES + " addresses allowed");
        }

        Address address = new Address();
        updateAddressFromDTO(address, dto);
        address.setUserId(userId);

        // If this is the first address or marked as default, set as default
        if (count == 0 || Boolean.TRUE.equals(dto.getIsDefault())) {
            addressRepository.clearDefaultForUser(userId);
            address.setIsDefault(true);
        }

        return toDTO(addressRepository.save(address));
    }

    @Transactional
    public AddressDTO updateAddress(Long id, AddressDTO dto, Long userId) {
        Address address = addressRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Address not found"));

        updateAddressFromDTO(address, dto);

        if (Boolean.TRUE.equals(dto.getIsDefault())) {
            addressRepository.clearDefaultForUser(userId);
            address.setIsDefault(true);
        }

        return toDTO(addressRepository.save(address));
    }

    @Transactional
    public void deleteAddress(Long id, Long userId) {
        Address address = addressRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Address not found"));

        boolean wasDefault = Boolean.TRUE.equals(address.getIsDefault());
        addressRepository.delete(address);

        // If deleted address was default, set another as default
        if (wasDefault) {
            List<Address> remaining = addressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId);
            if (!remaining.isEmpty()) {
                Address newDefault = remaining.get(0);
                newDefault.setIsDefault(true);
                addressRepository.save(newDefault);
            }
        }
    }

    @Transactional
    public AddressDTO setDefaultAddress(Long id, Long userId) {
        Address address = addressRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Address not found"));

        addressRepository.clearDefaultForUser(userId);
        address.setIsDefault(true);

        return toDTO(addressRepository.save(address));
    }

    private void updateAddressFromDTO(Address address, AddressDTO dto) {
        address.setLabel(dto.getLabel());
        address.setFullName(dto.getFullName());
        address.setPhone(dto.getPhone());
        address.setAddressLine1(dto.getAddressLine1());
        address.setAddressLine2(dto.getAddressLine2());
        address.setCity(dto.getCity());
        address.setState(dto.getState());
        address.setPincode(dto.getPincode());
    }

    private AddressDTO toDTO(Address address) {
        AddressDTO dto = new AddressDTO();
        dto.setId(address.getId());
        dto.setUserId(address.getUserId());
        dto.setLabel(address.getLabel());
        dto.setFullName(address.getFullName());
        dto.setPhone(address.getPhone());
        dto.setAddressLine1(address.getAddressLine1());
        dto.setAddressLine2(address.getAddressLine2());
        dto.setCity(address.getCity());
        dto.setState(address.getState());
        dto.setPincode(address.getPincode());
        dto.setIsDefault(address.getIsDefault());
        dto.setCreatedAt(address.getCreatedAt() != null ? address.getCreatedAt().toString() : null);
        dto.setUpdatedAt(address.getUpdatedAt() != null ? address.getUpdatedAt().toString() : null);
        return dto;
    }
}