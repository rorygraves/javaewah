package com.googlecode.javaewah;

import com.googlecode.javaewah.synth.ClusteredDataGenerator;
import org.junit.Test;

import java.util.Iterator;

import static org.junit.Assert.assertTrue;

/*
 * Copyright 2009-2014, Daniel Lemire, Cliff Moon, David McIntosh, Robert Becho, Google Inc., Veronika Zenz, Owen Kaser, gssiyankai
 * Licensed under the Apache License, Version 2.0.
 */

/**
 * Tests specifically for iterators.
 */
public class IteratorAggregationTest {

    /**
     * @param N   Number of bitmaps to generate in each set
     * @param nbr parameter determining the size of the arrays (in a log
     *            scale)
     * @return an iterator over sets of bitmaps
     */
    public static Iterator<EWAHCompressedBitmap[]> getCollections(
            final int N, final int nbr) {
        final ClusteredDataGenerator cdg = new ClusteredDataGenerator(123);
        return new Iterator<EWAHCompressedBitmap[]>() {
            int sparsity = 1;

            @Override
            public boolean hasNext() {
                return this.sparsity < 5;
            }

            @Override
            public EWAHCompressedBitmap[] next() {
                int[][] data = new int[N][];
                int Max = (1 << (nbr + this.sparsity));
                for (int k = 0; k < N; ++k)
                    data[k] = cdg.generateClustered(
                            1 << nbr, Max);
                EWAHCompressedBitmap[] ewah = new EWAHCompressedBitmap[N];
                for (int k = 0; k < N; ++k) {
                    ewah[k] = new EWAHCompressedBitmap();
                    for (int x = 0; x < data[k].length; ++x) {
                        ewah[k].set(data[k][x]);
                    }
                    data[k] = null;
                }
                this.sparsity += 3;
                return ewah;
            }

            @Override
            public void remove() {
                // unimplemented
            }

        };

    }

    /**
     *
     */
    @Test
    public void testAnd() {
        for (int N = 1; N < 10; ++N) {
            System.out.println("testAnd N = " + N);
            Iterator<EWAHCompressedBitmap[]> i = getCollections(N,
                    3);
            while (i.hasNext()) {
                EWAHCompressedBitmap[] x = i.next();
                EWAHCompressedBitmap tanswer = EWAHCompressedBitmap.and(x);
                EWAHCompressedBitmap x1 = IteratorUtil
                        .materialize(IteratorAggregation
                                .bufferedand(IteratorUtil
                                        .toIterators(x)));
                assertTrue(x1.equals(tanswer));
            }
            System.gc();
        }

    }

    /**
     *
     */
    @Test
    public void testOr() {
        for (int N = 1; N < 10; ++N) {
            System.out.println("testOr N = " + N);
            Iterator<EWAHCompressedBitmap[]> i = getCollections(N,
                    3);
            while (i.hasNext()) {
                EWAHCompressedBitmap[] x = i.next();
                EWAHCompressedBitmap tanswer = EWAHCompressedBitmap.or(x);
                EWAHCompressedBitmap x1 = IteratorUtil
                        .materialize(IteratorAggregation
                                .bufferedor(IteratorUtil
                                        .toIterators(x)));
                assertTrue(x1.equals(tanswer));
            }
            System.gc();
        }
    }

    /**
     *
     */
    @SuppressWarnings("deprecation")
    @Test
    public void testWideOr() {
        for (int nbr = 3; nbr <= 24; nbr += 3) {
            for (int N = 100; N < 1000; N += 100) {
                System.out.println("testWideOr N = " + N);
                Iterator<EWAHCompressedBitmap[]> i = getCollections(
                        N, 3);
                while (i.hasNext()) {
                    EWAHCompressedBitmap[] x = i.next();
                    EWAHCompressedBitmap tanswer = EWAHCompressedBitmap
                            .or(x);
                    EWAHCompressedBitmap container = new EWAHCompressedBitmap();
                    FastAggregation.legacy_orWithContainer(container, x);
                    assertTrue(container.equals(tanswer));
                    EWAHCompressedBitmap x1 = IteratorUtil
                            .materialize(IteratorAggregation
                                    .bufferedor(IteratorUtil
                                            .toIterators(x)));
                    assertTrue(x1.equals(tanswer));
                }
                System.gc();
            }
        }
    }

    /**
     *
     */
    @Test
    public void testXor() {
        System.out.println("testXor ");
        Iterator<EWAHCompressedBitmap[]> i = getCollections(2, 3);
        while (i.hasNext()) {
            EWAHCompressedBitmap[] x = i.next();
            EWAHCompressedBitmap tanswer = x[0].xor(x[1]);
            EWAHCompressedBitmap x1 = IteratorUtil
                    .materialize(IteratorAggregation.bufferedxor(
                            x[0].getIteratingRLW(),
                            x[1].getIteratingRLW()));
            assertTrue(x1.equals(tanswer));
        }
        System.gc();
    }

}
