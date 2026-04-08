package com.ajay.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ajay.model.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {



}

