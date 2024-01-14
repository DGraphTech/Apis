package com.dgraphtech.repositories;

import com.dgraphtech.entities.Subscriber;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubscriberRepository extends JpaRepository<Subscriber, Long> {
    Optional<Subscriber> findByCallbackUrl(String callbackUrl);
}
