package com.societyapp.application.port.out.persistence;

import com.societyapp.domain.model.ComplaintUpdate;
import java.util.*;

public interface ComplaintUpdateRepository extends CrudRepositoryPort<ComplaintUpdate, UUID> {
    List<ComplaintUpdate> findByComplaintIdOrderByCreatedAt(UUID complaintId);
}
