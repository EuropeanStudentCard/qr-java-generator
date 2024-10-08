package eu.europeanstudentcard.esc.constants;

/**
 * Constant class for the ESC qr generator
 *
 */
public class QRConstants {

    /**
     * The Error Level enum
     */
    public enum ErrorLevel {
        L(1),
        M(0),
        Q(3),
        H(2);

        private final int bits;

        public int getBits() {
            return bits;
        }

        ErrorLevel(int bits) {
            this.bits = bits;
        }

        public static int getBitsFromName(String name) {
            for (ErrorLevel level : ErrorLevel.values()) {
                if (level.name().equals(name)) {
                    return level.getBits();
                }
            }

            return 1;
        }
    }

    public static final String VERTICAL_ORIENTATION = "vertical";
    public static final String HORIZONTAL_ORIENTATION = "horizontal";
    public static final String NORMAL_COLOUR = "normal";
    public static final String INVERTED_COLOUR = "inverted";

    public static final String EXTRA_SMALL_SIZE = "XS";
    public static final String SMALL_SIZE = "S";
    public static final String MEDIUM_SIZE = "M";
    public static final String SVG_EXTENSION = ".svg";
}
