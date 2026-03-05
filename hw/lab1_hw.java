public class lab1_hw {
    private static final int BLACK = 0;
    private static final int WHITE = 255;
    // Avoid printing huge matrices; show timing instead.
    private static final int DISPLAY_THRESHOLD = 60;

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java lab1 <n> <rectangle|circle>");
            return;
        }

        int n = Integer.parseInt(args[0]);
        String shape = args[1].toLowerCase();

        // Measure only generation time, not printing time.
        long t1 = System.nanoTime();
        int[][] matrix;
        switch (shape) {
            case "rectangle", "rect" -> matrix = darkRectangleOnWhite(n);
            case "circle" -> matrix = whiteCircleOnDark(n);
            default -> {
                System.out.println("Unknown shape: " + shape);
                System.out.println("Use rectangle (rect) or circle");
                return;
            }
        }
        long t2 = System.nanoTime();

        // Pretty-print only for smaller sizes.
        if (n <= DISPLAY_THRESHOLD) {
            System.out.println(matrixToString(matrix));
        } else {
            double ms = (t2 - t1) / 1_000_000.0;
            System.out.println("Generated " + n + "x" + n + " in " + ms + " ms");
        }
    }

    private static int[][] darkRectangleOnWhite(int n) {
        int[][] m = new int[n][n];
        fill(m, WHITE);
        // Centered rectangle (middle half of the image).
        int start = n / 4;
        int end = (3 * n) / 4;
        for (int i = start; i < end; i++) {
            for (int j = start; j < end; j++) {
                m[i][j] = BLACK;
            }
        }
        return m;
    }

    private static int[][] whiteCircleOnDark(int n) {
        int[][] m = new int[n][n];
        fill(m, BLACK);
        int cx = n / 2;
        int cy = n / 2;
        double radius = n / 3.0;
        double r2 = radius * radius;
        // Fill pixels inside the circle radius.
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                double dx = i - cx;
                double dy = j - cy;
                if (dx * dx + dy * dy <= r2) {
                    m[i][j] = WHITE;
                }
            }
        }
        return m;
    }

    private static void fill(int[][] m, int value) {
        for (int[] row : m) {
            for (int j = 0; j < row.length; j++) {
                row[j] = value;
            }
        }
    }

    private static String matrixToString(int[][] m) {
        StringBuilder sb = new StringBuilder();
        for (int[] row : m) {
            for (int value : row) {
                sb.append(shade(value));
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    private static char shade(int value) {
        // BLACK -> '#', WHITE -> ' '
        return value < 128 ? '#' : ' ';
    }
}
