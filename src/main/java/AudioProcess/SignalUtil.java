package AudioProcess;

public class SignalUtil {

    private static final double PI = Math.PI;

    /**
     * generate the specific chirp
     *
     * @param sampleRate: sampling rate
     * @param fmin:       the lower bound of signal frequency
     * @param fmax:       the upper bound of signal frequency
     * @param T:          the duration of signal
     * @return the chirp signal
     */
    public static short[] UpChirp(int sampleRate, int fmin, int fmax, double T) {
        int len = (int) (T * sampleRate);
        double[] t = new double[len];
        short[] chirp = new short[len];
        for (int n = 0; n < len; n++) {
            double k = (fmax - fmin) / T;
            t[n] = (double) n / sampleRate;
            chirp[n] = (short) (Math.sin(2 * PI * fmin * t[n] + PI * k * t[n] * t[n]) * 32767);
        }
        return chirp;
    }

}
