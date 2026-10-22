package com.company.cms.repository;

import com.company.cms.entity.CustomerFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerFeedbackRepository extends JpaRepository<CustomerFeedback, Long> {
    List<CustomerFeedback> findByCustomerId(Long customerId);
    List<CustomerFeedback> findByProductId(Long productId);
    List<CustomerFeedback> findByRating(Integer rating);
}
