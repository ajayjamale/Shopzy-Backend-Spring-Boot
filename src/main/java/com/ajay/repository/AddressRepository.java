package com.ajay.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ajay.model.Address;

public interface AddressRepository extends JpaRepository<Address, Long> {

}
