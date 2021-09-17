package AudioProcess;

import org.jtransforms.fft.DoubleFFT_1D;

/**
 * 与频率相关的处理函数
 * 1. 提取一段信号的主导频率
 * 2.
 */
public class FrequencyUtil {

    /**
     * extract the dominant frequency from 16-bit PCM data
     *
     * @param sampleData: an array containing the raw 16-bit PCM data
     * @param sampleRate: the sampling rate(Hz) of sampleData
     * @return an approximation of the dominant frequency in sampleData
     */
    public double ExtractFrequency(short[] sampleData, int sampleRate) {
        /* sampleData + zero padding */
        int fftLen = sampleData.length + 20 * sampleData.length;
        DoubleFFT_1D fft = new DoubleFFT_1D(fftLen);
        double[] a = new double[fftLen]; // 不应该是*2？
        System.arraycopy(BaseUtil.HammingWindow(sampleData, sampleData.length), 0,
                a, 0, sampleData.length);
        fft.realForward(a);

        /* find the peak magnitude and it's index */
        double maxMag = Double.NEGATIVE_INFINITY;
        int maxIndex = -1;
        for (int i = 0; i < a.length; ++i) {
            double mag = Math.sqrt(a[2 * i] * a[2 * i] + a[2 * i + 1] * a[2 * i + 1]);
            if (mag > maxMag) {
                maxMag = mag;
                maxIndex = i;
            }
        }

        /* calculate the frequency */
        double outFreq = 0;
        if (maxMag > 6000) { // 暂定， 幅值超过一定阈值视为该频率有效
            outFreq = (double) sampleRate * maxIndex / a.length;
        }
        return outFreq;
    }
}
