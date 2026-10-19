package com.winwin.auth.process;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.winwin.auth.users.User;

@Repository
public interface ProcessingLogRepository extends JpaRepository<ProcessingLog, UUID> {

    List<ProcessingLog> findByUser(User user);

    List<ProcessingLog> findByUserId(UUID userId);

}

