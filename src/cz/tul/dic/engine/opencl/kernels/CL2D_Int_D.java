/* Copyright (C) LENAM, s.r.o. - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Petr Jecmen <petr.jecmen@tul.cz>, 2015
 */
package cz.tul.dic.engine.opencl.kernels;

import cz.tul.dic.engine.opencl.memory.AbstractOpenCLMemoryManager;
import com.jogamp.opencl.CLBuffer;
import com.jogamp.opencl.CLEvent;
import com.jogamp.opencl.CLEventList;
import com.jogamp.opencl.CLMemory;
import cz.tul.dic.engine.opencl.WorkSizeManager;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class CL2D_Int_D extends Kernel {

    private static final int ARGUMENT_INDEX_G_COUNT = 11;
    private static final int ARGUMENT_INDEX_F_COUNT = 12;
    private static final int ARGUMENT_INDEX_F_BASE = 13;
    private static final int ARGUMENT_INDEX_D_COUNT = 14;
    private static final int ARGUMENT_INDEX_D_BASE = 15;
    private static final int LWS0_BASE = 1;
    private static final int LWS1_BASE = 64;
    private final WorkSizeManager wsm;
    private boolean stop;

    public CL2D_Int_D(final AbstractOpenCLMemoryManager memManager) {
        super("CL2D_Int_D", memManager);
        wsm = new WorkSizeManager(KernelType.CL2D_Int_D);
    }

    @Override
    void runKernel(final CLMemory<IntBuffer> imgA, final CLMemory<IntBuffer> imgB,
            final CLBuffer<IntBuffer> facetData,
            final CLBuffer<FloatBuffer> facetCenters,
            final CLBuffer<FloatBuffer> deformationLimits, final CLBuffer<IntBuffer> defStepCounts,
            final CLBuffer<FloatBuffer> results,
            final int deformationCount, final int imageWidth,
            final int facetSize, final int facetCount) {
        stop = false;
        final int facetArea = facetSize * facetSize;

        final int lws0 = calculateLws0();
        int lws1 = Kernel.roundUp(calculateLws1Base(), facetArea);
        lws1 = Math.min(lws1, getMaxWorkItemSize());

        kernelDIC.rewind();
        kernelDIC.putArgs(imgA, imgB, facetData, facetCenters, deformationLimits, defStepCounts, results)
                .putArg(imageWidth)
                .putArg(deformationCount)
                .putArg(facetSize)
                .putArg(facetCount)
                .putArg(0)
                .putArg(0)
                .putArg(0)
                .putArg(0)
                .putArg(0);
        kernelDIC.rewind();
        // copy data and execute kernel
        wsm.setMaxFacetCount(facetCount);
        wsm.setMaxDeformationCount(deformationCount);
        wsm.reset();
        int facetGlobalWorkSize, deformationGlobalWorkSize, facetSubCount = 1, deformationSubCount;
        long time;
        CLEvent event;
        int currentBaseFacet = 0, currentBaseDeformation;
        int groupCountPerFacet, counter = 0;
        CLEventList eventList = new CLEventList(facetCount);
        while (currentBaseFacet < facetCount) {
            currentBaseDeformation = 0;

            while (currentBaseDeformation < deformationCount) {
                if (counter == eventList.capacity()) {
                    eventList = new CLEventList(facetCount);
                    counter = 0;
                }
                if (stop) {
                    return;
                }

                facetSubCount = Math.min(wsm.getFacetCount(), facetCount - currentBaseFacet);                
                deformationSubCount = Math.min(wsm.getDeformationCount(), deformationCount - currentBaseDeformation);

                facetGlobalWorkSize = Kernel.roundUp(lws0, facetSubCount);
                deformationGlobalWorkSize = Kernel.roundUp(lws1, deformationSubCount);

                groupCountPerFacet = deformationSubCount / lws1;
                if (deformationCount % lws1 > 0) {
                    groupCountPerFacet++;
                }

                kernelDIC.setArg(ARGUMENT_INDEX_G_COUNT, groupCountPerFacet);
                kernelDIC.setArg(ARGUMENT_INDEX_F_COUNT, facetSubCount);
                kernelDIC.setArg(ARGUMENT_INDEX_F_BASE, currentBaseFacet);
                kernelDIC.setArg(ARGUMENT_INDEX_D_COUNT, deformationSubCount);
                kernelDIC.setArg(ARGUMENT_INDEX_D_BASE, currentBaseDeformation);
                queue.put2DRangeKernel(kernelDIC, 0, 0, facetGlobalWorkSize, deformationGlobalWorkSize, lws0, lws1, eventList);

                queue.putWaitForEvent(eventList, counter, true);
                event = eventList.getEvent(counter);
                time = event.getProfilingInfo(CLEvent.ProfilingCommand.END) - event.getProfilingInfo(CLEvent.ProfilingCommand.START);
                wsm.storeTime(facetSubCount, deformationSubCount, time);

                currentBaseDeformation += deformationSubCount;
                counter++;
            }

            currentBaseFacet += facetSubCount;
        }

        eventList.release();
    }

    private int calculateLws1Base() {
        return LWS1_BASE;
    }

    private int calculateLws0() {
        return LWS0_BASE;
    }

    @Override
    public void stopComputation() {
        stop = true;
    }

}
