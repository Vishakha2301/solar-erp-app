package com.solarerp.customer.service;

import com.solarerp.customer.dto.CustomerRequest;
import com.solarerp.customer.dto.CustomerResponse;
import com.solarerp.customer.dto.CustomerSiteRequest;
import com.solarerp.customer.entity.Customer;
import com.solarerp.customer.entity.CustomerType;
import com.solarerp.customer.repository.CustomerRepository;
import com.solarerp.customer.service.impl.CustomerServiceImpl;
import com.solarerp.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomerServiceImpl Tests")
class CustomerServiceImplTest {

    @Mock
    private CustomerRepository repository;

    @InjectMocks
    private CustomerServiceImpl customerService;

    private UUID customerId;
    private UUID userId;
    private Customer customer;
    private CustomerRequest request;

    @BeforeEach
    void setUp() {
        customerId = UUID.randomUUID();
        userId = UUID.randomUUID();

        customer = new Customer();
        customer.setCustomerType(CustomerType.INDIVIDUAL);
        customer.setName("John Doe");
        customer.setPhone("9876543210");
        customer.setEmail("john@example.com");
        customer.setCity("Mumbai");
        customer.setState("Maharashtra");
        customer.setActive(true);
        customer.setCreatedBy(userId);
        customer.setSites(new ArrayList<>());

        request = new CustomerRequest(
                CustomerType.INDIVIDUAL,
                "John Doe",
                null,
                "9876543210",
                "john@example.com",
                "123 Main Street",
                "Mumbai",
                "Maharashtra",
                "400001",
                null,
                List.of(new CustomerSiteRequest(
                        "Main Roof",
                        "123 Main Street",
                        "Mumbai",
                        "Maharashtra",
                        "400001",
                        true))
        );
    }

    @Nested
    @DisplayName("getAll()")
    class GetAllTests {

        @Test
        @DisplayName("Returns all active customers ordered by name")
        void getAll_returnsActiveCustomers() {
            when(repository.findAllByActiveTrueOrderByNameAsc())
                    .thenReturn(List.of(customer));

            List<CustomerResponse> result = customerService.getAll();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).name()).isEqualTo("John Doe");
            verify(repository, times(1))
                    .findAllByActiveTrueOrderByNameAsc();
        }

        @Test
        @DisplayName("Returns empty list when no active customers")
        void getAll_noCustomers_returnsEmptyList() {
            when(repository.findAllByActiveTrueOrderByNameAsc())
                    .thenReturn(List.of());

            List<CustomerResponse> result = customerService.getAll();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getById()")
    class GetByIdTests {

        @Test
        @DisplayName("Returns customer when found")
        void getById_existingId_returnsCustomer() {
            when(repository.findById(customerId))
                    .thenReturn(Optional.of(customer));

            CustomerResponse result =
                    customerService.getById(customerId);

            assertThat(result).isNotNull();
            assertThat(result.name()).isEqualTo("John Doe");
            assertThat(result.phone()).isEqualTo("9876543210");
        }

        @Test
        @DisplayName("Throws ResourceNotFoundException when not found")
        void getById_nonExistentId_throwsNotFound() {
            when(repository.findById(customerId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    customerService.getById(customerId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Customer");
        }
    }

    @Nested
    @DisplayName("search()")
    class SearchTests {

        @Test
        @DisplayName("Returns customers matching name")
        void search_matchingName_returnsCustomers() {
            when(repository.findByNameContainingIgnoreCaseAndActiveTrue(
                    "John"))
                    .thenReturn(List.of(customer));

            List<CustomerResponse> result =
                    customerService.search("John");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).name()).isEqualTo("John Doe");
        }

        @Test
        @DisplayName("Returns empty list when no match")
        void search_noMatch_returnsEmptyList() {
            when(repository.findByNameContainingIgnoreCaseAndActiveTrue(
                    "Unknown"))
                    .thenReturn(List.of());

            List<CustomerResponse> result =
                    customerService.search("Unknown");

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("create()")
    class CreateTests {

        @Test
        @DisplayName("Creates customer with sites")
        void create_validRequest_createsCustomerWithSites() {
            when(repository.save(any())).thenReturn(customer);

            CustomerResponse result =
                    customerService.create(request, userId);

            assertThat(result).isNotNull();
            verify(repository, times(1)).save(any());
        }

        @Test
        @DisplayName("Sets createdBy from userId")
        void create_setsCreatedByFromUserId() {
            when(repository.save(any())).thenAnswer(inv -> {
                Customer saved = inv.getArgument(0);
                assertThat(saved.getCreatedBy()).isEqualTo(userId);
                return customer;
            });

            customerService.create(request, userId);

            verify(repository, times(1)).save(any());
        }

        @Test
        @DisplayName("Creates customer without sites when sites null")
        void create_nullSites_createsCustomerWithoutSites() {
            CustomerRequest requestNoSites = new CustomerRequest(
                    CustomerType.INDIVIDUAL,
                    "Jane Doe",
                    null,
                    "9876543211",
                    null, null, null, null, null, null, null
            );
            when(repository.save(any())).thenReturn(customer);

            CustomerResponse result =
                    customerService.create(requestNoSites, userId);

            assertThat(result).isNotNull();
            verify(repository, times(1)).save(any());
        }
    }

    @Nested
    @DisplayName("update()")
    class UpdateTests {

        @Test
        @DisplayName("Updates customer successfully")
        void update_existingCustomer_updatesSuccessfully() {
            when(repository.findById(customerId))
                    .thenReturn(Optional.of(customer));
            when(repository.save(any())).thenReturn(customer);

            CustomerResponse result =
                    customerService.update(customerId, request);

            assertThat(result).isNotNull();
            verify(repository, times(1)).save(any());
        }

        @Test
        @DisplayName("Throws ResourceNotFoundException when not found")
        void update_nonExistentId_throwsNotFound() {
            when(repository.findById(customerId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    customerService.update(customerId, request))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Clears and replaces sites on update")
        void update_withNewSites_replacesSites() {
            when(repository.findById(customerId))
                    .thenReturn(Optional.of(customer));
            when(repository.save(any())).thenReturn(customer);

            customerService.update(customerId, request);

            verify(repository, times(1)).save(any());
        }
    }

    @Nested
    @DisplayName("deactivate()")
    class DeactivateTests {

        @Test
        @DisplayName("Deactivates active customer")
        void deactivate_activeCustomer_setsActiveFalse() {
            when(repository.findById(customerId))
                    .thenReturn(Optional.of(customer));
            when(repository.save(any())).thenReturn(customer);

            customerService.deactivate(customerId);

            assertThat(customer.isActive()).isFalse();
            verify(repository, times(1)).save(customer);
        }

        @Test
        @DisplayName("Throws ResourceNotFoundException when not found")
        void deactivate_nonExistentId_throwsNotFound() {
            when(repository.findById(customerId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    customerService.deactivate(customerId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
