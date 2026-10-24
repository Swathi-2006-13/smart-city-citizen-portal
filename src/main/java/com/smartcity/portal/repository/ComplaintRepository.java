
package com.smartcity.portal.repository;

import com.smartcity.portal.entity.Complaint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ComplaintRepository
        extends JpaRepository<Complaint, Long> {

    Optional<Complaint> findByComplaintId(String complaintId);

    List<Complaint> findByCitizenUsernameOrderByCreatedAtDesc(
            String username
    );
}
