package com.example.isms.controller;

import com.example.isms.service.GithubImageService;
import com.example.isms.service.imageProcessingService;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.opencv_core.Mat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
@CrossOrigin(origins = "http://127.0.0.1:5500")
@RestController
@RequestMapping("/api/github")
public class GithubImageController {


    private imageProcessingService imageProcessingService;

    private final GithubImageService githubService;

    @Autowired
    public GithubImageController(imageProcessingService imageProcessingService, GithubImageService githubService) {
        this.imageProcessingService=imageProcessingService;
        this.githubService = githubService;
    }

    @PostMapping("/upload-processed")
    public ResponseEntity<?> uploadProcessedImages(
            @RequestParam("department") String department,
            @RequestParam("programme") String programme,
            @RequestParam("batch") String batch,
            @RequestParam("files") List<MultipartFile> files) throws IOException {

        List<String> failed = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
                // 1. Read and decode image
                byte[] fileBytes = file.getBytes();
                BytePointer bytePointer = new BytePointer(fileBytes);
                Mat mob = new Mat(bytePointer);
                Mat original = opencv_imgcodecs.imdecode(mob, opencv_imgcodecs.IMREAD_COLOR);

                if (original == null || original.empty()) {
                    failed.add(file.getOriginalFilename() + " (could not decode image)");
                    continue;
                }

                // 2. Crop to circular
                Mat circle = imageProcessingService.cropToCircle(original);

                // 3. Resize to 200x200
                Mat resized = imageProcessingService.resizeImage(circle, 200, 200);

                // 4. Encode to JPEG
                BytePointer buffer = new BytePointer();
                boolean success = opencv_imgcodecs.imencode(".jpg", resized, buffer);

                if (!success) {
                    failed.add(file.getOriginalFilename() + " (encoding failed)");
                    continue;
                }

                byte[] finalImage = new byte[(int) buffer.limit()];
                buffer.get(finalImage);

                // 5. Upload to GitHub
                githubService.uploadStudentImage(department, batch,programme, finalImage, file.getOriginalFilename());

            } catch (Exception e) {
                failed.add(file.getOriginalFilename() + " (error: " + e.getMessage() + ")");
            }
        }

        if (failed.isEmpty()) {
            return ResponseEntity.ok("All images processed and uploaded successfully.");
        } else {
            return ResponseEntity.status(207).body("Some images failed: " + failed);
        }
    }


    @PostMapping("/delete")
    public ResponseEntity<?> deleteImages(
            @RequestParam("department") String department,
            @RequestParam("batch") String batch,
            @RequestParam("programme") String programme,
            @RequestBody List<String> fileNames) {

        List<String> failedDeletions = new ArrayList<>();

        for (String fileName : fileNames) {
            try {
                githubService.deleteStudentImage(department, batch,programme, fileName);
            } catch (Exception e) {
                failedDeletions.add(fileName);
            }
        }

        if (failedDeletions.isEmpty()) {
            return ResponseEntity.ok("All files deleted successfully.");
        } else {
            return ResponseEntity.status(207).body("Some files couldn't be deleted: " + failedDeletions);
        }
    }

    @DeleteMapping("/delete-folder")
    public ResponseEntity<?> deleteFolder(
            @RequestParam("department") String department,
            @RequestParam("programme") String programme,
            @RequestParam("batch") String batch) {

        githubService.deleteStudentFolder(department, batch,programme);
        return ResponseEntity.ok("All images deleted for " + department + " " + batch);
    }


    @GetMapping("/link/{programme}/{department}/{batch}/{fileName}")
    public ResponseEntity<String> getLink(
            @PathVariable String department,
            @PathVariable String batch,
            @PathVariable String programme,
            @PathVariable String fileName) {

        return ResponseEntity.ok(githubService.getStudentImageUrl(department, batch,programme, fileName));
    }


    @PostMapping("/upload-director-image")
    public ResponseEntity<?> uploadDirectorImage(@RequestParam("file") MultipartFile file) {
        try {
            String fileName = "director.jpg";

            // Try deleting old image (if it exists)
            try {
                githubService.deleteDirectorImage();
            } catch (Exception ignored) {
                // Optionally log: old image not found or delete failed, but continue
            }

            // Read and decode image
            byte[] fileBytes = file.getBytes();
            BytePointer bytePointer = new BytePointer(fileBytes);
            Mat mob = new Mat(bytePointer);
            Mat original = opencv_imgcodecs.imdecode(mob, opencv_imgcodecs.IMREAD_COLOR);

            if (original == null || original.empty()) {
                return ResponseEntity.badRequest().body("Could not decode image.");
            }

            // Process: crop to circle and resize
            Mat circle = imageProcessingService.cropToCircle(original);
            Mat resized = imageProcessingService.resizeImage(circle, 200, 200);

            // Encode to JPEG
            BytePointer buffer = new BytePointer();
            if (!opencv_imgcodecs.imencode(".jpg", resized, buffer)) {
                return ResponseEntity.internalServerError().body("Encoding failed.");
            }

            byte[] finalImage = new byte[(int) buffer.limit()];
            buffer.get(finalImage);

            // Upload to GitHub under "user_images/director/director.jpg"
            githubService.uploadDirectorImage(finalImage, fileName);

            return ResponseEntity.ok("Director image uploaded successfully.");

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Upload failed: " + e.getMessage());
        }
    }


    @GetMapping("/get-director-image")
    public ResponseEntity<String> getDirectorImageUrl() {
        String githubUsername = "developer4949-code";
        String repoName = "user_images";
        String branch = "main";
        String filePath = "user_images/director/director.jpg";

        String imageUrl = String.format(
                "https://raw.githubusercontent.com/%s/%s/%s/%s",
                githubUsername, repoName, branch, filePath
        );

        return ResponseEntity.ok(imageUrl);
    }





    @PostMapping("/upload_timetable")
    public ResponseEntity<?> uploadTimetable(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty() || !file.getOriginalFilename().endsWith(".pdf")) {
                return ResponseEntity.badRequest().body("Only PDF files are allowed.");
            }

            byte[] fileBytes = file.getBytes();
            String fileName = "timetable.pdf";

            // Try deleting old timetable (optional)
            try {
                githubService.deleteFile("user_images/timetable", fileName);
            } catch (Exception ignored) {
                // Log if needed
            }

            githubService.uploadFile("user_images/timetable", fileBytes, fileName);

            return ResponseEntity.ok("Timetable uploaded successfully.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Upload failed: " + e.getMessage());
        }
    }




    @GetMapping("/proxy_timetable")
    public ResponseEntity<byte[]> proxyTimetable() {
        String githubRawUrl = "https://raw.githubusercontent.com/developer4949-code/user_images/main/user_images/timetable/timetable.pdf";

        try (InputStream inputStream = new URL(githubRawUrl).openStream()) {
            byte[] pdfBytes = inputStream.readAllBytes();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(ContentDisposition.inline().filename("timetable.pdf").build());

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }


    @PostMapping("/upload-faculty-images")
    public ResponseEntity<?> uploadFacultyImages(
            @RequestParam("files") List<MultipartFile> files) {

        List<String> failed = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
                String fileName = file.getOriginalFilename();
                if (fileName == null || !fileName.toUpperCase().endsWith(".JPG")) {
                    failed.add(fileName + " (invalid format)");
                    continue;
                }

                // Extract faculty ID from file name (e.g., 123.jpg -> 123)
                String facultyId = fileName.substring(0, fileName.lastIndexOf('.')).toUpperCase();

                // 1. Read and decode image bytes to Mat
                byte[] fileBytes = file.getBytes();
                BytePointer bytePointer = new BytePointer(fileBytes);
                Mat mob = new Mat(bytePointer);
                Mat original = opencv_imgcodecs.imdecode(mob, opencv_imgcodecs.IMREAD_COLOR);

                if (original == null || original.empty()) {
                    failed.add(fileName + " (could not decode image)");
                    continue;
                }

                // 2. Crop to circular shape
                Mat circle = imageProcessingService.cropToCircle(original);

                // 3. Resize to 200x200
                Mat resized = imageProcessingService.resizeImage(circle, 200, 200);

                // 4. Encode processed image to JPEG
                BytePointer buffer = new BytePointer();
                boolean success = opencv_imgcodecs.imencode(".jpg", resized, buffer);

                if (!success) {
                    failed.add(fileName + " (encoding failed)");
                    continue;
                }

                byte[] finalImage = new byte[(int) buffer.limit()];
                buffer.get(finalImage);

                // 5. Upload processed image to GitHub
                githubService.uploadFacultyImage(facultyId, finalImage);

            } catch (Exception e) {
                failed.add(file.getOriginalFilename() + " (error: " + e.getMessage() + ")");
            }
        }

        if (failed.isEmpty()) {
            return ResponseEntity.ok("All faculty images processed and uploaded successfully.");
        } else {
            return ResponseEntity.status(207).body("Some images failed: " + failed);
        }
    }

    @GetMapping("/faculty-image-url/{facultyId}")
    public ResponseEntity<String> getFacultyImageUrl(@PathVariable String facultyId) {
        try {
            String url = githubService.getFacultyImageUrl(facultyId.toUpperCase());
            return ResponseEntity.ok(url);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving image URL: " + e.getMessage());
        }
    }

}
