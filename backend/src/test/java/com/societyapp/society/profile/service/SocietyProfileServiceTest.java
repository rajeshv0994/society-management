package com.societyapp.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.societyapp.application.model.SocietyProfileUpdate;
import com.societyapp.application.model.SocietyProfileView;
import com.societyapp.application.port.out.DocumentStorage;
import com.societyapp.application.port.out.persistence.SocietyProfileStore;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class SocietyProfileServiceTest {
    private final SocietyProfileStore repository = mock(SocietyProfileStore.class);
    private final DocumentStorage storage = mock(DocumentStorage.class);
    private final SocietyProfileService service = new SocietyProfileService(repository, storage);

    @Test
    void updateDelegatesPersistenceAndReturnsFreshProfile() {
        UUID societyId = UUID.randomUUID();
        SocietyProfileUpdate request =
                new SocietyProfileUpdate(
                        "Green Valley",
                        "A residential community",
                        "https://green.example",
                        "Society Manager",
                        "manager@green.example",
                        "9000000000",
                        "Main Road",
                        "Pune",
                        "Maharashtra",
                        "411001");
        SocietyProfileView stored = mock(SocietyProfileView.class);
        when(repository.findById(societyId)).thenReturn(stored);

        SocietyProfileView result = service.update(societyId, request);

        verify(repository).update(societyId, request);
        assertThat(result).isEqualTo(stored);
    }
}
