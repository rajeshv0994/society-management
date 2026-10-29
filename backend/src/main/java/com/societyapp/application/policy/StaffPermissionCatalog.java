package com.societyapp.application.policy;

import com.societyapp.domain.type.StaffPermissionCode;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class StaffPermissionCatalog {
    private static final Map<String, PermissionDefinition> DEFINITIONS = definitions();

    private StaffPermissionCatalog() {}

    public static Map<String, PermissionDefinition> definitionsFor(String enabledModulesJson) {
        var result = new LinkedHashMap<String, PermissionDefinition>();
        DEFINITIONS.forEach(
                (code, definition) -> {
                    if (moduleEnabled(enabledModulesJson, definition.requiredModule())) {
                        result.put(code, definition);
                    }
                });
        return result;
    }

    public static Set<String> validate(Set<String> codes, String enabledModulesJson) {
        if (codes == null) throw new IllegalArgumentException("Permissions are required");
        var available = definitionsFor(enabledModulesJson).keySet();
        var invalid = codes.stream().filter(code -> !available.contains(code)).sorted().toList();
        if (!invalid.isEmpty()) {
            throw new IllegalArgumentException(
                    "Unknown or disabled staff permissions: " + String.join(", ", invalid));
        }
        return Set.copyOf(codes);
    }

    public static boolean moduleEnabled(String enabledModulesJson, String module) {
        return enabledModulesJson != null && enabledModulesJson.contains("\"" + module + "\"");
    }

    private static Map<String, PermissionDefinition> definitions() {
        var result = new LinkedHashMap<String, PermissionDefinition>();
        for (var permission : StaffPermissionCode.values()) {
            result.put(
                    permission.code(),
                    new PermissionDefinition(
                            permission.label(), permission.requiredModule().code()));
        }
        return Map.copyOf(result);
    }

    public record PermissionDefinition(String label, String requiredModule) {}
}
