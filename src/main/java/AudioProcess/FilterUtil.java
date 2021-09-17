package AudioProcess;

import biz.source_code.dsp.filter.FilterPassType;
import biz.source_code.dsp.filter.IirFilterCoefficients;
import biz.source_code.dsp.filter.IirFilterDesignExstrom;

/**
 * 信号滤波器
 * 效果等同于 matlab中的 filter函数（已验证），而非matlab的 filtfilt/ python的 signal.filtfilt函数
 * 1. 带通滤波
 * 2. 高通滤波
 * 3. 低通滤波
 */

public class FilterUtil {
    /* other static variables */
    private static double[] in;
    private static double[] out;
    private static short[] outData;

    /**
     * bandpss for the raw 16-bit PCM data, using IirFilterCoefficients class
     *
     * @param data:       the raw 16-bit PCM data
     * @param order:      the order of filter
     * @param fmin:       lower bound frequency
     * @param fmax:       upper bound frequency
     * @param sampleRate: the sampling rate of signal
     * @return the filtered data
     */
    public static short[] BandPass(short[] data, int order, int fmin, int fmax, int sampleRate) {
        IirFilterCoefficients iirFilterCoefficients;
        iirFilterCoefficients = IirFilterDesignExstrom.design(FilterPassType.bandpass, order,
                (double) fmin / sampleRate, (double) fmax / sampleRate);
        return Filter(data, iirFilterCoefficients.a, iirFilterCoefficients.b);
    }

    /**
     * highpss for the raw 16-bit PCM data, using IirFilterCoefficients class
     *
     * @param data:       the raw 16-bit PCM data
     * @param order:      the order of filter
     * @param fmin:       lower bound frequency
     * @param sampleRate: the sampling rate of signal
     * @return the filtered data
     */
    public static short[] HighPass(short[] data, int order, int fmin, int sampleRate) {
        IirFilterCoefficients iirFilterCoefficients;
        iirFilterCoefficients = IirFilterDesignExstrom.design(FilterPassType.highpass, order,
                (double) fmin / sampleRate, (double) fmin / sampleRate); // v1:Ignored for lowpass/highpass
        return Filter(data, iirFilterCoefficients.a, iirFilterCoefficients.b);
    }

    /**
     * lowpss for the raw 16-bit PCM data, using IirFilterCoefficients class
     *
     * @param data:       the raw 16-bit PCM data
     * @param order:      the order of filter
     * @param fmax:       lower bound frequency
     * @param sampleRate: the sampling rate of signal
     * @return the filtered data
     */
    public static short[] LowPass(short[] data, int order, int fmax, int sampleRate) {
        IirFilterCoefficients iirFilterCoefficients;
        iirFilterCoefficients = IirFilterDesignExstrom.design(FilterPassType.highpass, order,
                (double) fmax / sampleRate, (double) fmax / sampleRate); // v1:Ignored for lowpass/highpass
        return Filter(data, iirFilterCoefficients.a, iirFilterCoefficients.b);
    }

    /**
     * filter the raw 16-bit PCM data
     *
     * @param data: an array containing the raw 16-bit PCM data
     * @param a:    the transfer function coefficients-a
     * @param b     the transfer function coefficients-b
     * @return the filtered data
     */
    private static short[] Filter(short[] data, double[] a, double[] b) {
        in = new double[b.length];
        out = new double[a.length - 1];
        outData = new short[data.length];
        for (int i = 0; i < data.length; i++) {
            System.arraycopy(in, 0, in, 1, in.length - 1);  //in[1]=in[0],in[2]=in[1]...
            in[0] = data[i];

            /* calculate y based on a and b coefficients and in and out. */
            double y = 0.0;
            for (int j = 0; j < b.length; j++) {
                y += b[j] * in[j];
            }
            for (int j = 0; j < a.length - 1; j++) {
                y -= a[j + 1] * out[j];
            }

            /* shift the out array */
            System.arraycopy(out, 0, out, 1, out.length - 1);
            out[0] = y;
            outData[i] = (short) Math.round(y);
        }
        return outData;
    }

    public static void main(String[] args) {
        System.out.println("testing in FilterUtil");
    }
}
