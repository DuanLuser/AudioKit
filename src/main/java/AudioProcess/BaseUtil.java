package AudioProcess;

import java.util.*;

/**
 * 信号处理常用的工具函数
 * 1. 峰值检测
 * 2. 互相关（由于数据结构为short[]，较double[]的结果在数值上存在一定差异，但原理、趋势一致）
 * 3. 加窗函数（hanning, hamming）
 */
public class BaseUtil {

    /**
     * find at most one peak in data
     *
     * @param data:      the 16-bit PCM data
     * @param threshold: peaks must be above the threshold
     * @return the index of the peak(if none, return -1)
     */
    public static int findOnePeak(short[] data, double threshold) {
        int peak = -1;
        int maxMag = -1;
        for (int i = 0; i < data.length; ++i) {
            if (data[i] > maxMag) {
                maxMag = data[i];
                peak = i;
            }
        }
        if (maxMag < threshold) {
            peak = -1;
        }
        return peak;
    }

    /**
     * find the target peaks in data——参照python的思想进行实现，效率不高？
     * https://blog.csdn.net/yfl_jybq/article/details/100114952
     *
     * @param data:      the 16-bit PCM data
     * @param threshold: peaks must be above the threshold
     * @param interval:  the min interval between two peaks
     * @return the index of peaks
     */
    public static ArrayList<Integer> findPeaks(short[] data, double threshold, int interval) {
        /* step 1 */
        ArrayList<Integer> outPeaks = new ArrayList<>();
        ArrayList<Integer> zeros = new ArrayList<>();
        int[] dataDiff = new int[data.length - 1];
        for (int i = 0; i < dataDiff.length; ++i) {
            dataDiff[i] = data[i + 1] - data[i];
            if (dataDiff[i] == 0) {
                zeros.add(i);
            }
        }
        /* step 2 : 对于有水平线的情况：去除首尾的0值对应的位置，补全中间水平线的差值（靠近原则） */
        if (zeros.size() > 0) {
            if (zeros.get(0) == 0) {
                int i = 0;
                int prior = -1;
                while (zeros.get(i) == prior + 1) {
                    prior = zeros.get(i);
                    zeros.remove(i);
                }
            }
            if (zeros.get(zeros.size() - 1) == data.length - 2) {
                int i = zeros.size() - 1;
                int later = data.length - 1;
                while (zeros.get(i) == later - 1) {
                    later = zeros.get(i);
                    zeros.remove(i);
                    --i;
                }
            }
            int lower = 0;
            int upper = lower + 1;
            int lowerVal = -1, upperVal = -1;
            int priorVal = zeros.get(lower);
            int mid = -1;
            while (lower < zeros.size()) {
                if (upper < zeros.size() && zeros.get(upper) == priorVal + 1) {
                    priorVal = zeros.get(upper);
                    ++upper;
                } else {
                    --upper;
                    mid = (zeros.get(lower) + zeros.get(upper)) / 2;
                    lowerVal = zeros.get(lower);
                    upperVal = zeros.get(upper);
                    for (int i = lowerVal; i <= mid; ++i) {
                        dataDiff[i] = dataDiff[lowerVal - 1];
                    }
                    for (int i = mid + 1; i <= upperVal; ++i) {
                        dataDiff[i] = dataDiff[upperVal + 1];
                    }
                    lower = upper + 1;
                    upper = lower + 1;
                    if (lower < zeros.size())
                        priorVal = zeros.get(lower);
                }
            }
        }
        /* step 3 : 按规定间隔，去除间隔内重叠的峰 */
        int index = 0;
        Map<Integer, Integer> peakIndexValue = new HashMap<>(); // 大小是65536，超过该大小会出错
        for (int i = 0; i < dataDiff.length - 1; ++i) {
            if (dataDiff[i] > 0 && dataDiff[i + 1] < 0 && data[i + 1] > threshold) {
                outPeaks.add(i + 1);
                peakIndexValue.put(index, (int) data[i + 1]);
                ++index;
            }
        }
        /* step 4 */
        List<Map.Entry<Integer, Integer>> origin = new ArrayList<>(peakIndexValue.entrySet());
        List<Map.Entry<Integer, Integer>> ordered = new ArrayList<>(peakIndexValue.entrySet());
        Collections.sort(ordered, new Comparator<Map.Entry<Integer, Integer>>() {
            @Override
            public int compare(Map.Entry<Integer, Integer> o1, Map.Entry<Integer, Integer> o2) {
                //按照value值，从大到小排序
                return o2.getValue() - o1.getValue();
            }
        });
        int p1, cur, p2;
        Map.Entry<Integer, Integer> obj;
        for (int i = 0; i < ordered.size(); ++i) {
            Map.Entry<Integer, Integer> pIndexVal = ordered.get(i);
            cur = pIndexVal.getKey();
            p1 = cur - 1;
            p2 = cur + 1;
            while (p1 >= 0 && outPeaks.get(cur) - outPeaks.get(p1) < interval) {
                obj = origin.get(p1);
                if (ordered.contains(obj))
                    ordered.remove(obj);
                --p1;
            }
            while (p2 < outPeaks.size() && outPeaks.get(p2) - outPeaks.get(cur) < interval) {
                obj = origin.get(p2);
                if (ordered.contains(obj))
                    ordered.remove(obj);
                ++p2;
            }
        }
        Collections.sort(ordered, new Comparator<Map.Entry<Integer, Integer>>() {
            @Override
            public int compare(Map.Entry<Integer, Integer> o1, Map.Entry<Integer, Integer> o2) {
                //按照key值，从小到大排序
                return o1.getKey() - o2.getKey();
            }
        });
        ArrayList<Integer> peaks = new ArrayList<>();
        for (Map.Entry<Integer, Integer> pIndexVal : ordered) {
            peaks.add(outPeaks.get(pIndexVal.getKey()));
        }
        return peaks;
    }

    /**
     * compute the correlation between the two sequences
     *
     * @param origin: the original sequence
     * @param target: the target sequence, such as chirp
     * @return the correlation between them
     */
    public static long[] correlate(short[] origin, short[] target) {
        if (origin.length < target.length)
            return null;
        int cofLen = origin.length - target.length + 1;
        long[] cof = new long[cofLen];
        for (int i = 0; i < cofLen; ++i) {
            cof[i] = 0;
            for (int j = 0; j < target.length; ++j) {
                cof[i] += target[j] * origin[j + i];
            }
        }
        return cof;
    }

    /**
     * add hanning window to signal(see hamming window in FrequencyUtil)
     *
     * @param signal: the data needed to add window
     * @param pos:    the start site of signal
     * @param size:   the size of signal
     * @return the signal with window
     */
    public static double[] hanningWindow(short[] signal, int pos, int size) {
        double[] res = new double[signal.length];
        for (int i = pos; i < pos + size; i++) {
            int j = i - pos;
            res[i] = signal[i] * 0.5 * (1.0 - Math.cos(2.0 * Math.PI * j / size));
        }
        return res;

    }

    /**
     * apply a Hamming window filter to raw input data
     * See http://www.labbookpages.co.uk/audio/firWindowing.html#windows
     *
     * @param data: an array containing unfiltered input data
     * @return a doube array containing the filtered data
     */
    public static double[] hammingWindow(short[] data, int size) {
        double[] res = new double[data.length];
        /* create window function */
        double[] window = new double[size];
        for (int i = 0; i < size; ++i) {
            window[i] = .54 - .46 * Math.cos(2 * Math.PI * i / (size - 1.0));
        }
        /* apply window function */
        for (int i = 0; i < data.length; ++i) {
            res[i] = (double) data[i] * window[i];
        }
        return res;
    }

    public static void main(String[] args) {
        System.out.println("testing in BaseUtil: ");

        /*short[] data = {1,2,3,4,5,6,7,6,5,4,3,2,1};
        ArrayList<Integer> peaks = findPeaks(data, 1, 10);
        System.out.println(peaks);*/
        short[] origin = {1, 2, 3};
        short[] target = {1, 2, 3};
        long[] out = correlate(origin, target);
        System.out.println(out);

        System.out.println("EOT in BaseUtil");
    }
}
