package org.broad.igv.hic.track;

import org.broad.igv.feature.LocusScore;
import org.broad.igv.hic.HiC;
import org.broad.igv.hic.data.MatrixZoomData;
import org.broad.igv.renderer.DataRange;
import org.broad.igv.track.DataTrack;

import java.awt.*;
import java.util.List;

/**
 * An adapter class to serve as a bridge between an IGV data source and a HiC track.  HiC tracks differ from
 * IGV tracks in that the coordinate system is based on "bins", each of which can correspond to a variable
 * genomic length.
 *
 * @author jrobinso
 *         Date: 9/10/12
 */
public abstract class HiCDataAdapter {

    HiC hic;

    LoadedDataInterval loadedDataInterval;

    protected HiCDataAdapter(HiC hic) {
        this.hic = hic;
    }


    public abstract String getName();

    public abstract Color getColor();

    public abstract boolean isLogScale();

    public abstract Color getAltColor();

    public abstract DataRange getDataRange();

    public WeightedSum[] getData(String chr, int startBin, int endBin) {

        int resolution = hic.zd.getBinSize();
        HiC.Unit unit = hic.getUnit();
        if (loadedDataInterval != null && loadedDataInterval.contains(resolution, unit, chr, startBin, endBin)) {
            return loadedDataInterval.getData();
        } else {

            // Expand starBin and endBin by 50% to facilitate panning
            int f = (endBin - startBin) / 2;
            System.out.println("Loading " + startBin + " " + endBin + "  " + f);
            startBin = Math.max(0, startBin - f);
            endBin = endBin + f ;

            WeightedSum[] data = new WeightedSum[endBin - startBin + 1];
            HiCGridAxis gridAxis = hic.zd.getxGridAxis();
            int zoom = gridAxis.getIGVZoom();
            int gStart = gridAxis.getGenomicStart(startBin);
            int gEnd = gridAxis.getGenomicEnd(endBin);

            List<LocusScore> scores = getLocusScores(chr, zoom, gStart, gEnd);
            for (LocusScore locusScore : scores) {

                int bs = gridAxis.getBinNumberForGenomicPosition(locusScore.getStart());
                int be = gridAxis.getBinNumberForGenomicPosition(locusScore.getEnd());

                if (bs > endBin) {
                    break;
                } else if (be < startBin) {
                    continue;
                }

                for (int b = Math.max(startBin, bs); b <= Math.min(endBin, be); b++) {

                    int bStart = gridAxis.getGenomicStart(b);
                    int bEnd = gridAxis.getGenomicEnd(b);
                    WeightedSum dataBin = data[b - startBin];
                    if (dataBin == null) {
                        dataBin = new WeightedSum(b, bStart, bEnd);
                        data[b - startBin] = dataBin;
                    }
                    dataBin.addScore(locusScore);
                }
            }

            loadedDataInterval = new LoadedDataInterval(resolution, unit, chr, startBin, endBin, data);

            return data;
        }
    }

    protected abstract List<LocusScore> getLocusScores(String chr, int zoom, int gStart, int gEnd);


    public static class WeightedSum {
        int binNumber;
        int nPts = 0;
        double weightedSum = 0;
        int genomicStart;
        int genomicEnd;

        public WeightedSum(int binNumber, int genomicStart, int genomicEnd) {
            this.binNumber = binNumber;
            this.genomicStart = genomicStart;
            this.genomicEnd = genomicEnd;
        }

        public int getBinNumber() {
            return binNumber;
        }

        void addScore(LocusScore ls) {
            if (ls.getStart() >= genomicEnd || ls.getEnd() < genomicStart) return;

            double weight = ((double) (Math.min(genomicEnd, ls.getEnd()) - Math.max(genomicStart, ls.getStart()))) /
                    (genomicEnd - genomicStart);
            weight = 1;
            weightedSum += weight * ls.getScore();
            nPts++;
        }


        public double getValue() {
            return nPts == 0 ? 0 : (float) (weightedSum / nPts);
        }
    }


    class LoadedDataInterval {

        int resolution;
        HiC.Unit unit;
        String chr;
        int startBin;
        int endBin;
        WeightedSum[] data;

        LoadedDataInterval(int resolution, HiC.Unit unit, String chr, int startBin, int endBin, WeightedSum[] data) {
            this.resolution = resolution;
            this.unit = unit;
            this.chr = chr;
            this.startBin = startBin;
            this.endBin = endBin;
            this.data = data;
        }

        boolean contains(int resolution, HiC.Unit unit, String chr, int startBin, int endBin) {
            boolean contains = resolution == this.resolution &&
                    unit == this.unit &&
                    chr.equals(this.chr) &&
                    startBin >= this.startBin &&
                    endBin <= this.endBin;
            if(!contains) {
                System.out.println("Cache miss");
            }
            return contains;
        }

        WeightedSum[] getData() {
            return data;
        }
    }
}
