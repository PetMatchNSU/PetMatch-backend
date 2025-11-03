package org.nsu.users.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.nsu.testutils.TestDataFactory;
import org.nsu.users.dto.requests.UpdateUserRequest;
import org.nsu.users.dto.requests.GenderRequest;
import org.nsu.users.entity.*;
import org.nsu.users.mappers.BondTimeMapper;
import org.nsu.users.mappers.ContactMapper;
import org.nsu.users.repositories.ContactTypeRepository;
import org.nsu.users.core.repositories.UserRepository;
import org.nsu.users.repositories.RegionRepository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RegionRepository regionRepository;

    @Mock
    private ContactTypeRepository contactTypeRepository;

    @Mock
    private BondTimeMapper bondTimeMapper;

    @Mock
    private ContactMapper contactMapper;

    @InjectMocks
    private UserProfileService userProfileService;

    @Test
    void shouldUpdateUserAggregate_whenValidDto() {
        User user = TestDataFactory.createTestUserWithoutBondTimeAndAuthorities();

        UpdateUserRequest dto = new UpdateUserRequest();
        dto.setFirstName("A");
        dto.setSecondName("B");
        dto.setLastName("C");
        dto.setGender(GenderRequest.M);
        dto.setLocationId(1L);

        UpdateUserRequest.BondTimeDto b = new UpdateUserRequest.BondTimeDto();
        b.setBondTimeStart(LocalTime.of(10, 0));
        b.setBondTimeEnd(LocalTime.of(12, 0));
        dto.setBondTime(List.of(b));

        UpdateUserRequest.ContactInfoDto ci = new UpdateUserRequest.ContactInfoDto();
        ci.setType("Phone");
        ci.setContact("+7900123456");
        ci.setVisible(true);
        dto.setContactInfo(List.of(ci));

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(regionRepository.findById(1L)).thenReturn(Optional.of(TestDataFactory.createTestRegion()));
        ContactType ct = TestDataFactory.createTestContactType();
        when(contactTypeRepository.findAll()).thenReturn(List.of(ct));

        BondTime btEntity = new BondTime();
        btEntity.setStartContactTime(b.getBondTimeStart());
        btEntity.setEndContactTime(b.getBondTimeEnd());
        when(bondTimeMapper.toEntity(b)).thenReturn(btEntity);

        Contact contactEntity = new Contact();
        contactEntity.setLink(ci.getContact());
        contactEntity.setIsVisible(ci.getVisible());
        when(contactMapper.toEntity(ci)).thenReturn(contactEntity);

        userProfileService.updateProfile("test@example.com", dto);

        verify(userRepository).save(user);
        assertEquals(1, user.getBondTimes().size());
        assertEquals(1, user.getContacts().size());
        assertEquals(ct, user.getContacts().get(0).getType());
    }

    @Test
    void shouldThrowWhenContactTypeUnknown() {
        User user = TestDataFactory.createTestUserWithoutBondTimeAndAuthorities();
        UpdateUserRequest dto = new UpdateUserRequest();
        dto.setFirstName("A");
        dto.setSecondName("B");
        dto.setLastName("C");
        dto.setGender(GenderRequest.M);
        dto.setLocationId(1L);

        UpdateUserRequest.ContactInfoDto ci = new UpdateUserRequest.ContactInfoDto();
        ci.setType("Unknown");
        ci.setContact("+1");
        ci.setVisible(true);
        dto.setContactInfo(List.of(ci));

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(regionRepository.findById(1L)).thenReturn(Optional.of(TestDataFactory.createTestRegion()));
        when(contactTypeRepository.findAll()).thenReturn(List.of());

        assertThrows(IllegalArgumentException.class, () -> userProfileService.updateProfile("test@example.com", dto));
        verify(userRepository, never()).save(any());
    }
}
