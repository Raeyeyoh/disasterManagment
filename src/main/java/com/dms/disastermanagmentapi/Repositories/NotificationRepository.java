package com.dms.disastermanagmentapi.Repositories;

import java.util.List;

import com.dms.disastermanagmentapi.entities.Notification;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    List<Notification> findByUserUserIdAndIsReadFalseOrderByCreatedAtDesc(Integer userId);

}