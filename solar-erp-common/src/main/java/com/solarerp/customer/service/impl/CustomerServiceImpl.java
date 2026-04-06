package com.solarerp.customer.service.impl;

import com.solarerp.customer.dto.CustomerRequest;
import com.solarerp.customer.dto.CustomerResponse;
import com.solarerp.customer.dto.CustomerSiteResponse;
import com.solarerp.customer.entity.Customer;
import com.solarerp.customer.entity.CustomerSite;
import com.solarerp.customer.repository.CustomerRepository;
import com.solarerp.customer.service.CustomerService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository repository;

    public CustomerServiceImpl(CustomerRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<CustomerResponse> getAll() {
        return repository.findAllByActiveTrueOrderByNameAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public CustomerResponse getById(UUID id) {
        return toResponse(findOrThrow(id));
    }

    @Override
    public List<CustomerResponse> search(String name) {
        return repository.findByNameContainingIgnoreCaseAndActiveTrue(name)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public CustomerResponse create(CustomerRequest request, UUID userId) {
        Customer customer = new Customer();
        mapRequestToEntity(request, customer);
        customer.setCreatedBy(userId);

        if (request.sites() != null) {
            request.sites().forEach(siteReq -> {
                CustomerSite site = new CustomerSite();
                site.setSiteLabel(siteReq.siteLabel());
                site.setAddress(siteReq.address());
                site.setCity(siteReq.city());
                site.setState(siteReq.state());
                site.setPincode(siteReq.pincode());
                site.setDefault(siteReq.isDefault());
                site.setCustomer(customer);
                customer.getSites().add(site);
            });
        }

        return toResponse(repository.save(customer));
    }

    @Override
    public CustomerResponse update(UUID id, CustomerRequest request) {
        Customer customer = findOrThrow(id);
        mapRequestToEntity(request, customer);

        if (request.sites() != null) {
            customer.getSites().clear();
            request.sites().forEach(siteReq -> {
                CustomerSite site = new CustomerSite();
                site.setSiteLabel(siteReq.siteLabel());
                site.setAddress(siteReq.address());
                site.setCity(siteReq.city());
                site.setState(siteReq.state());
                site.setPincode(siteReq.pincode());
                site.setDefault(siteReq.isDefault());
                site.setCustomer(customer);
                customer.getSites().add(site);
            });
        }

        return toResponse(repository.save(customer));
    }

    @Override
    public void deactivate(UUID id) {
        Customer customer = findOrThrow(id);
        customer.setActive(false);
        repository.save(customer);
    }

    private Customer findOrThrow(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Customer not found: " + id));
    }

    private void mapRequestToEntity(CustomerRequest request, Customer customer) {
        customer.setCustomerType(request.customerType());
        customer.setName(request.name());
        customer.setCompanyName(request.companyName());
        customer.setPhone(request.phone());
        customer.setEmail(request.email());
        customer.setAddress(request.address());
        customer.setCity(request.city());
        customer.setState(request.state());
        customer.setPincode(request.pincode());
        customer.setGstNumber(request.gstNumber());
    }

    private CustomerResponse toResponse(Customer customer) {
        List<CustomerSiteResponse> sites = customer.getSites().stream()
                .map(site -> new CustomerSiteResponse(
                        site.getId(),
                        site.getSiteLabel(),
                        site.getAddress(),
                        site.getCity(),
                        site.getState(),
                        site.getPincode(),
                        site.isDefault(),
                        site.getCreatedAt()
                ))
                .toList();

        return new CustomerResponse(
                customer.getId(),
                customer.getCustomerType(),
                customer.getName(),
                customer.getCompanyName(),
                customer.getPhone(),
                customer.getEmail(),
                customer.getAddress(),
                customer.getCity(),
                customer.getState(),
                customer.getPincode(),
                customer.getGstNumber(),
                customer.isActive(),
                customer.getCreatedAt(),
                customer.getCreatedBy(),
                sites
        );
    }
}
