/*
 * Copyright (c) 2007-2011 by The Broad Institute of MIT and Harvard.  All Rights Reserved.
 *
 * This software is licensed under the terms of the GNU Lesser General Public License (LGPL),
 * Version 2.1 which is available at http://www.opensource.org/licenses/lgpl-2.1.php.
 *
 * THE SOFTWARE IS PROVIDED "AS IS." THE BROAD AND MIT MAKE NO REPRESENTATIONS OR
 * WARRANTES OF ANY KIND CONCERNING THE SOFTWARE, EXPRESS OR IMPLIED, INCLUDING,
 * WITHOUT LIMITATION, WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE, NONINFRINGEMENT, OR THE ABSENCE OF LATENT OR OTHER DEFECTS, WHETHER
 * OR NOT DISCOVERABLE.  IN NO EVENT SHALL THE BROAD OR MIT, OR THEIR RESPECTIVE
 * TRUSTEES, DIRECTORS, OFFICERS, EMPLOYEES, AND AFFILIATES BE LIABLE FOR ANY DAMAGES
 * OF ANY KIND, INCLUDING, WITHOUT LIMITATION, INCIDENTAL OR CONSEQUENTIAL DAMAGES,
 * ECONOMIC DAMAGES OR INJURY TO PROPERTY AND LOST PROFITS, REGARDLESS OF WHETHER
 * THE BROAD OR MIT SHALL BE ADVISED, SHALL HAVE OTHER REASON TO KNOW, OR IN FACT
 * SHALL KNOW OF THE POSSIBILITY OF THE FOREGOING.
 */

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.broad.igv.feature.genome;

import org.broad.igv.Globals;
import org.broad.igv.PreferenceManager;
import org.broad.igv.ui.IGV;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

/**
 * @author jrobinso
 */
public class SequenceHelperTest {

    static String seqPath = "http://igv.broadinstitute.org/genomes/seq/hg18/";

    static Genome genome;

    static SequenceHelper helper;


    public SequenceHelperTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        helper = new SequenceHelper(seqPath);
        Globals.setHeadless(true);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        helper.clearCache();
    }

    /**
     * Test cached vs uncached sequence reads.
     */
    @Test
    public void testCache() {
        try {
            String chr = "chr7";
            int start = 55054464;
            int end = start + 10000;


            SequenceHelper helper = new SequenceHelper(seqPath);

            byte[] cachedSeq = helper.getSequence(chr, start, end);

            SequenceHelper.setCacheSequences(false);

            byte[] uncachedSeq = helper.getSequence(chr, start, end);

            assertEquals(uncachedSeq.length, cachedSeq.length);

            for (int i = 0; i < cachedSeq.length; i++) {
                assertEquals("i=" + i, uncachedSeq[i], cachedSeq[i]);
            }

        } finally {
            SequenceHelper.setCacheSequences(true);
        }

    }

    @Test
    public void testNonCloudGenome() throws IOException {

        String genome = "spur_2.1";
        String chr = "scaffold_v2_26164";
        int start = 5;
        int end = 10;
        String expSequence = "ATTGC";
        IGV.getInstance().getGenomeManager().loadGenomeByID(genome);

        byte[] seq = helper.getSequence(chr, start, end);
        assertEquals(expSequence, new String(seq));

    }

    /**
     * Test known sequence (start of EGFR).
     */
    @Test
    public void readEGFRSequence() {
        String genome = "hg18";
        String chr = "chr7";
        int start = 55054464;
        int end = start + 20;
        String expSequence = "ATGCGACCCTCCGGGACGGC";
        byte[] seq = helper.getSequence(chr, start, end);
        assertEquals(expSequence, new String(seq));
    }

    @Test
    public void testURL1() {

        GenomeDescriptor descriptor = IGV.getInstance().getGenomeManager().getGenomeDescriptor("hg18");
        descriptor.setSequenceLocation("http://www.broadinstitute.org/igv/SequenceServlet/hg18");
        readEGFRSequence();
    }

    @Test
    public void testURL2() {

        GenomeDescriptor descriptor = IGV.getInstance().getGenomeManager().getGenomeDescriptor("hg18");
        descriptor.setSequenceLocation("http://www.broadinstitute.org/igv/sequence/hg18");
        readEGFRSequence();
    }

    @Test
    public void testURL3() {

        GenomeDescriptor descriptor = IGV.getInstance().getGenomeManager().getGenomeDescriptor("hg18");
        descriptor.setSequenceLocation("http://igv.broadinstitute.org/genomes/seq/hg18");
        readEGFRSequence();
    }

    @Test
    public void testURL4() {

        GenomeDescriptor descriptor = IGV.getInstance().getGenomeManager().getGenomeDescriptor("hg18");
        descriptor.setSequenceLocation("http://igvdata.broadinstitute.org/genomes/seq/hg18");
        readEGFRSequence();
    }

    @Test
    public void testByteRangePref() {
        final PreferenceManager preferenceManager = PreferenceManager.getInstance();
        boolean useByteRange = preferenceManager.getAsBoolean(PreferenceManager.USE_BYTE_RANGE);
        try {
            preferenceManager.put(PreferenceManager.USE_BYTE_RANGE, String.valueOf(!useByteRange));

            testURL1();
            helper.clearCache();

            testURL2();
            helper.clearCache();

            testURL3();
            helper.clearCache();

            testURL4();
            helper.clearCache();


        }
        finally {
            preferenceManager.put(PreferenceManager.USE_BYTE_RANGE, String.valueOf(useByteRange));
        }
    }

}