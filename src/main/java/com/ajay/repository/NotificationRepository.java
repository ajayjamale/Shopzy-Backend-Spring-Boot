package com.ajay.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ajay.model.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {



}
