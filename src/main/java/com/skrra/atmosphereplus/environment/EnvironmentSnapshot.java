package com.skrra.atmosphereplus.environment;

public record EnvironmentSnapshot(
        EnvironmentType type,
        boolean canSeeSky,
        boolean surface,
        boolean underground,
        boolean caveLike,
        boolean nether,
        boolean end
) {
    public static EnvironmentSnapshot unknown() {
        return new EnvironmentSnapshot(EnvironmentType.UNKNOWN, false, false, false, false, false, false);
    }
}
