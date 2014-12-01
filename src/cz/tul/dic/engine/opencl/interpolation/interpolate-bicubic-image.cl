inline int getValue(float p0, float p1, float p2, float p3, float x) {
    return p1 + 0.5 * x*(p2 - p0 + x*(2.0*p0 - 5.0*p1 + 4.0*p2 - p3 + x*(3.0*(p1 - p2) + p3 - p0)));    
}

inline int interpolate(const float x, const float y, read_only image2d_t image) {
    const float ix = floor(x);
    const float dx = x - ix;
    
    const float iy = floor(y);
    const float dy = y - iy;            
    
    const float arr0 = getValue(read_imageui(image, sampler, (float2)(ix-1, iy-1)).x, read_imageui(image, sampler, (float2)(ix, iy-1)).x, read_imageui(image, sampler, (float2)(ix+1, iy-1)).x, read_imageui(image, sampler, (float2)(ix+2, iy-1)).x, dy);
    const float arr1 = getValue(read_imageui(image, sampler, (float2)(ix-1, iy)).x, read_imageui(image, sampler, (float2)(ix, iy)).x, read_imageui(image, sampler, (float2)(ix+1, iy)).x, read_imageui(image, sampler, (float2)(ix+2, iy)).x, dy);
    const float arr2 = getValue(read_imageui(image, sampler, (float2)(ix-1, iy+1)).x, read_imageui(image, sampler, (float2)(ix, iy+1)).x, read_imageui(image, sampler, (float2)(ix+1, iy+1)).x, read_imageui(image, sampler, (float2)(ix+2, iy+1)).x, dy);
    const float arr3 = getValue(read_imageui(image, sampler, (float2)(ix-1, iy+2)).x, read_imageui(image, sampler, (float2)(ix, iy+2)).x, read_imageui(image, sampler, (float2)(ix+1, iy+2)).x, read_imageui(image, sampler, (float2)(ix+2, iy+2)).x, dy);
    
    return getValue(arr0, arr1, arr2, arr3, dx);
}