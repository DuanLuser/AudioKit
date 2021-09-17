package WavUtil;

import AudioProcess.BaseUtil;
import AudioProcess.FilterUtil;
import AudioProcess.SignalUtil;

import java.io.File;
import java.util.ArrayList;

public class Instance {

    /**
     * test: read data from specific wav file
     *
     * @param filename
     * @return the data in that file
     */
    public static short[] ReadDataFromWav(String filename) {
        short[] buffer = null;
        try {
            WavFile wavFile = WavFile.openWavFile(new File(filename));
            wavFile.display();
            int numChannels = wavFile.getNumChannels();
            long numFrames = wavFile.getNumFrames();
            buffer = new short[(int) (numFrames * numChannels)];
            int framesRead = wavFile.readFrames(buffer, (int) (numFrames * numChannels));
            System.out.println("size:" + framesRead);
            wavFile.close();
        } catch (Exception e) {
            System.err.println(e);
        }
        return buffer;
    }

    public static void main(String[] args) {
        System.out.println("testing in Instance: ");

        short[] data = ReadDataFromWav("D:/Project/Maven/AudioKit/file/test.wav");
        short[] left = new short[data.length / 2];
        short[] right = new short[data.length / 2];
        for (int i = 0; i < data.length / 2; i++) {
            left[i] = data[2 * i];
            right[i] = data[2 * i + 1];
        }

        short[] checkRight = new short[100];
        System.arraycopy(right, 10000, checkRight, 0, 100);
        left = FilterUtil.BandPass(left, 5, 16000, 23000, 48000);
        /*short[] checkLeft = new short[100];
        System.arraycopy(left, 10000, checkLeft, 0, 100);
        ArrayList<Integer> peaks = BaseUtil.findPeaks(checkLeft, 500, 4000);
        System.out.println(peaks);*/
        short[] chirp = SignalUtil.UpChirp(48000,16000,23000,0.05);
        long[] cof = BaseUtil.Correlate(left, chirp);

        System.out.println("EOT in Instance");
    }
}
