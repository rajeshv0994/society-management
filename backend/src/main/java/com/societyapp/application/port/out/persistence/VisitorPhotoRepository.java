package com.societyapp.application.port.out.persistence;

import com.societyapp.domain.model.VisitorPhoto;
import java.util.*;

public interface VisitorPhotoRepository extends CrudRepositoryPort<VisitorPhoto, UUID> {
    List<VisitorPhoto> findByVisitorIdOrderByCreatedAt(UUID visitorId);

    long countByVisitorId(UUID visitorId);
}
