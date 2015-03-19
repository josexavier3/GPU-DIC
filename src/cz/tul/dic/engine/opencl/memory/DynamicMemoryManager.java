/* Copyright (C) LENAM, s.r.o. - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Petr Jecmen <petr.jecmen@tul.cz>, 2015
 */
package cz.tul.dic.engine.opencl.memory;

import com.jogamp.opencl.CLBuffer;
import com.jogamp.opencl.CLImage2d;
import com.jogamp.opencl.CLMemory;
import cz.tul.dic.ComputationException;
import cz.tul.dic.ComputationExceptionCause;
import cz.tul.dic.data.Facet;
import cz.tul.dic.data.Image;
import cz.tul.dic.data.deformation.DeformationUtils;
import cz.tul.dic.engine.opencl.kernels.Kernel;
import java.nio.IntBuffer;
import java.util.List;

public class DynamicMemoryManager extends OpenCLMemoryManager {

    private Image imageA, imageB;
    private List<Facet> facets;
    private List<double[]> deformationLimits;
    private List<int[]> deformationCounts;

    DynamicMemoryManager() {
    }

    @Override
    public void assignDataToGPU(Image imageA, Image imageB, List<Facet> facets, List<double[]> deformationLimits, Kernel kernel) throws ComputationException {
        try {
            if (imageA != this.imageA || clImageA.isReleased()) {
                release(clImageA);
                this.imageA = imageA;

                if (imageA == this.imageB) {
                    clImageA = clImageB;
                } else {
                    if (kernel.usesImage()) {
                        clImageA = generateImage2d_t(imageA);
                        queue.putWriteImage((CLImage2d<IntBuffer>) clImageA, false);
                    } else {
                        clImageA = generateImageArray(imageA);
                        queue.putWriteBuffer((CLBuffer<IntBuffer>) clImageA, false);
                    }
                }
            }
            if (imageB != this.imageB || clImageB.isReleased()) {
                if (clImageA != clImageB) {
                    release(clImageB);
                }
                this.imageB = imageB;

                if (kernel.usesImage()) {
                    clImageB = generateImage2d_t(imageB);
                    queue.putWriteImage((CLImage2d<IntBuffer>) clImageB, false);
                } else {
                    clImageB = generateImageArray(imageB);
                    queue.putWriteBuffer((CLBuffer<IntBuffer>) clImageB, false);
                }
            }

            boolean changedResults = false;
            if (facets != this.facets || !facets.equals(this.facets) || clFacetData.isReleased()) {
                release(clFacetData);
                release(clFacetCenters);
                this.facets = facets;

                clFacetData = generateFacetData(facets, kernel.usesMemoryCoalescing());
                queue.putWriteBuffer(clFacetData, false);

                clFacetCenters = generateFacetCenters(facets);
                queue.putWriteBuffer(clFacetCenters, false);

                changedResults = true;
            }
            if (deformationLimits != this.deformationLimits || !deformationLimits.equals(this.deformationLimits) || clDeformationLimits.isReleased()) {
                release(clDeformationLimits);
                release(clDefStepCount);
                this.deformationLimits = deformationLimits;

                clDeformationLimits = generateDeformationLimits(deformationLimits);
                queue.putWriteBuffer(clDeformationLimits, false);

                deformationCounts = DeformationUtils.generateDeformationCounts(deformationLimits);
                clDefStepCount = generateDeformationStepCounts(deformationCounts);
                queue.putWriteBuffer(clDefStepCount, false);

                changedResults = true;
            }

            if (changedResults || clResults.isReleased()) {
                release(clResults);

                maxDeformationCount = DeformationUtils.findMaxDeformationCount(deformationCounts);
                final long size = facets.size() * maxDeformationCount;
                if (size <= 0 || size >= Integer.MAX_VALUE) {
                    throw new ComputationException(ComputationExceptionCause.OPENCL_ERROR, "Illegal size of resulting array - " + size);
                }
                clResults = context.createFloatBuffer((int) size, CLMemory.Mem.READ_WRITE);
            }
        } catch (OutOfMemoryError e) {
            throw new ComputationException(ComputationExceptionCause.MEMORY_ERROR, e.getLocalizedMessage());
        }
    }

}
