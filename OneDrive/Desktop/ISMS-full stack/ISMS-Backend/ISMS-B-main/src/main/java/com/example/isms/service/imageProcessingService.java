package com.example.isms.service;

import org.bytedeco.opencv.global.opencv_core;
import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.springframework.stereotype.Service;

@Service
public class imageProcessingService {

    public Mat cropToCircle(Mat input) {
        // Ensure input has 3 channels (BGR)
        if (input.channels() != 3) {
            throw new IllegalArgumentException("Input image must have 3 channels (BGR).");
        }

        int width = input.cols();
        int height = input.rows();
        int size = Math.min(width, height); // make it square for circular crop
        int x = (width - size) / 2;
        int y = (height - size) / 2;

        // Crop the image to a square
        Rect squareROI = new Rect(x, y, size, size);
        Mat square = new Mat(input, squareROI);

        // Create circular mask
        Mat mask = new Mat(square.size(), opencv_core.CV_8UC1, new Scalar(0.0));
        Point center = new Point(size / 2, size / 2);
        int radius = size / 2;
        opencv_imgproc.circle(mask, center, radius, new Scalar(255.0), -1, 8, 0); // draw white filled circle

        // Output image
        Mat output = new Mat();
        square.copyTo(output, mask); // copy with mask applied

        return output;
    }

    public Mat resizeImage(Mat input, int width, int height) {
        Mat resized = new Mat();
        opencv_imgproc.resize(input, resized, new Size(width, height));
        return resized;
    }
}
