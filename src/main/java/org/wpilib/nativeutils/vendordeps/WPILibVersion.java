package org.wpilib.nativeutils.vendordeps;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parsed subset of a WPILib version string.
 *
 * <p>Format: {@code <year>.<imageBreaking>-<WPILibBreaking>[.<WPILibNonBreaking>]}.
 * Only the first three parts are a part of compatibility checks. WPILibNonBreaking is ignored.
 */
final class WPILibVersion {
    private static final Pattern PATTERN = Pattern.compile("^(\\d+)\\.(\\d+)-(\\d+)(?:\\.(\\d+))?$");

    private final int year;
    private final int imageBreaking;
    private final int wpilibBreaking;

    private WPILibVersion(int year, int imageBreaking, int wpilibBreaking) {
        this.year = year;
        this.imageBreaking = imageBreaking;
        this.wpilibBreaking = wpilibBreaking;
    }

    static WPILibVersion parse(String value) {
        if (value == null) {
            throw new InvalidWPILibVersionException(null, null);
        }
        Matcher matcher = PATTERN.matcher(value);
        if (!matcher.matches()) {
            throw new InvalidWPILibVersionException(value, null);
        }
        try {
            return new WPILibVersion(
                    Integer.parseInt(matcher.group(1)),
                    Integer.parseInt(matcher.group(2)),
                    Integer.parseInt(matcher.group(3)));
        } catch (NumberFormatException e) {
            throw new InvalidWPILibVersionException(value, e);
        }
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof WPILibVersion that)) {
            return false;
        }
        return year == that.year
                && imageBreaking == that.imageBreaking
                && wpilibBreaking == that.wpilibBreaking;
    }

    @Override
    public int hashCode() {
        return Objects.hash(year, imageBreaking, wpilibBreaking);
    }

    @Override
    public String toString() {
        return year + "." + imageBreaking + "-" + wpilibBreaking;
    }
}
