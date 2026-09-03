package org.example.jubjubapi.payment.repository;

import org.example.jubjubapi.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
