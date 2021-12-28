package dialog.logger;

import clojure.lang.Keyword;


/**
 * Simple enumeration with some utility methods.
 */
public enum Level {

    // Level member constants
    TRACE, DEBUG, INFO, WARN, ERROR, FATAL, OFF;


    /** Clojure keyword corresponding to this level. */
    public final Keyword keyword;


    /**
     * Construct a new level enum value.
     */
    private Level() {
        this.keyword = Keyword.intern(this.name().toLowerCase());
    }


    /**
     * Find a level value matching the given keyword.
     *
     * @param k  level keyword to match.
     * @return the matching level, if any
     */
    public static Level ofKeyword(Keyword k) {
        for (Level level : Level.values()) {
            if (level.keyword.equals(k)) {
                return level;
            }
        }
        throw new IllegalArgumentException("Not a valid level keyword: " + k);
    }


    /**
     * Determine whether the given keyword is a valid level.
     *
     * @param k  level keyword to check
     * @return true if the keyword is valid, otherwise false
     */
    public static boolean isValid(Keyword k) {
        for (Level level : Level.values()) {
            if (level.keyword.equals(k)) {
                return true;
            }
        }
        return false;
    }


    /**
     * Determine whether the given level meets or exceeds the threshold.
     *
     * @param threshold  level threshold
     * @param level      level value to test
     * @return true if the level meets the threshold
     */
    public static boolean isAllowed(Level threshold, Level level) {
        // OFF is always an un-meetable level.
        if (threshold.equals(OFF)) {
            return false;
        }

        return 0 <= level.compareTo(threshold);
    }

}
