package com.societyapp.infrastructure.persistence.repository;

import com.societyapp.domain.model.VisitorPhoto;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataVisitorPhotoRepository
        extends JpaRepository<VisitorPhoto, UUID>,
                com.societyapp.application.port.out.persistence.VisitorPhotoRepository {
    List<VisitorPhoto> findByVisitorIdOrderByCreatedAt(UUID visitorId);

    long countByVisitorId(UUID visitorId);
}
