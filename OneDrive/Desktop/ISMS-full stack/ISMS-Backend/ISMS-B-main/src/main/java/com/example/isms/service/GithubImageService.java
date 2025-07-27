package com.example.isms.service;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@Service
public class GithubImageService {

    private String githubToken = "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";

    private String owner = "developer4949-code";
    private String repo = "user_images";

    private final RestTemplate restTemplate = new RestTemplate();

    private HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(githubToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        return headers;
    }

    private static final Logger logger = LoggerFactory.getLogger(GithubImageService.class);

    public void uploadStudentImage(String department, String batch, String programme, byte[] imageBytes, String filename) {
        String path = "user_images/" + programme + "_" + department + "_" + batch + "/students/student_images/" + filename;
        String url = "https://api.github.com/repos/" + owner + "/" + repo + "/contents/" + path;

        String base64 = Base64.getEncoder().encodeToString(imageBytes);

        Map<String, Object> body = new HashMap<>();
        body.put("message", "Processed upload " + filename);
        body.put("content", base64);
        body.put("branch", "main");

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, getHeaders());
        restTemplate.exchange(url, HttpMethod.PUT, request, String.class);
    }

    public void deleteStudentImage(String department, String batch, String programme, String fileName) {
        String path = "user_images/" + programme + "_" + department + "_" + batch + "/students/student_images/student_images/" + fileName;
        String url = "https://api.github.com/repos/" + owner + "/" + repo + "/contents/" + path;

        try {
            logger.info("Attempting to fetch SHA for file: {}", path);

            ResponseEntity<Map> res = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(getHeaders()), Map.class);
            Map responseBody = res.getBody();

            if (responseBody == null || !responseBody.containsKey("sha")) {
                logger.error("Failed to retrieve SHA. Response body: {}", responseBody);
                throw new RuntimeException("SHA not found for file: " + path);
            }

            String sha = (String) responseBody.get("sha");
            logger.info("SHA for file {}: {}", path, sha);

            Map<String, Object> body = new HashMap<>();
            body.put("message", "Delete " + fileName);
            body.put("sha", sha);
            body.put("branch", "main");

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, getHeaders());

            logger.info("Sending DELETE request to: {}", url);
            ResponseEntity<String> deleteResponse = restTemplate.exchange(url, HttpMethod.DELETE, request, String.class);
            logger.info("File deleted successfully. GitHub response: {}", deleteResponse.getBody());

        } catch (Exception e) {
            logger.error("Error while deleting file: {}", path, e);
            throw new RuntimeException("Failed to delete file: " + fileName, e);
        }
    }

    // ✅ FIXED: Removed extra "student_images/" from the path
    public String getStudentImageUrl(String department, String batch, String programme, String fileName) {
        return "https://raw.githubusercontent.com/" + owner + "/" + repo + "/main/user_images/" +
                programme + "_" + department + "_" + batch + "/students/student_images/student_images/" + fileName;
    }

    public void uploadDirectorImage(byte[] imageBytes, String filename) {
        String path = "user_images/director/" + filename;
        String url = "https://api.github.com/repos/" + owner + "/" + repo + "/contents/" + path;

        String base64 = Base64.getEncoder().encodeToString(imageBytes);

        Map<String, Object> body = new HashMap<>();
        body.put("message", "Upload director image " + filename);
        body.put("content", base64);
        body.put("branch", "main");

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, getHeaders());
        restTemplate.exchange(url, HttpMethod.PUT, request, String.class);
    }

    public void deleteDirectorImage() {
        String path = "user_images/director/director.jpg";
        String url = "https://api.github.com/repos/" + owner + "/" + repo + "/contents/" + path;

        try {
            ResponseEntity<Map> res = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(getHeaders()), Map.class);
            Map responseBody = res.getBody();

            if (responseBody == null || !responseBody.containsKey("sha")) {
                return;
            }

            String sha = (String) responseBody.get("sha");

            Map<String, Object> body = new HashMap<>();
            body.put("message", "Delete director image");
            body.put("sha", sha);
            body.put("branch", "main");

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, getHeaders());
            restTemplate.exchange(url, HttpMethod.DELETE, request, String.class);

        } catch (Exception e) {
            throw new RuntimeException("Failed to delete director image", e);
        }
    }

    public void deleteStudentFolder(String department, String batch, String programme) {
        String folderPath = "user_images/" + programme + "_" + department + "_" + batch + "/students/student_images/";
        String url = "https://api.github.com/repos/" + owner + "/" + repo + "/contents/" + folderPath;

        HttpEntity<String> request = new HttpEntity<>(getHeaders());

        try {
            ResponseEntity<Object[]> response = restTemplate.exchange(url, HttpMethod.GET, request, Object[].class);
            Object[] files = response.getBody();

            if (files == null || files.length == 0) {
                logger.warn("No files found in folder: {}", folderPath);
                return;
            }

            for (Object file : files) {
                LinkedHashMap<String, Object> fileMap = (LinkedHashMap<String, Object>) file;
                String filePath = (String) fileMap.get("path");
                String sha = (String) fileMap.get("sha");

                Map<String, Object> body = new HashMap<>();
                body.put("message", "Delete " + filePath);
                body.put("sha", sha);
                body.put("branch", "main");

                String deleteUrl = "https://api.github.com/repos/" + owner + "/" + repo + "/contents/" + filePath;

                HttpEntity<Map<String, Object>> deleteRequest = new HttpEntity<>(body, getHeaders());
                restTemplate.exchange(deleteUrl, HttpMethod.DELETE, deleteRequest, String.class);

                logger.info("Deleted: {}", filePath);
            }
        } catch (Exception e) {
            logger.error("Error deleting folder contents at: {}", folderPath, e);
            throw new RuntimeException("Failed to delete folder contents: " + folderPath, e);
        }
    }



    public void uploadFile(String folderPath, byte[] content, String fileName) {
        String path = folderPath + "/" + fileName;
        String url = "https://api.github.com/repos/" + owner + "/" + repo + "/contents/" + path;

        String base64 = Base64.getEncoder().encodeToString(content);

        Map<String, Object> body = new HashMap<>();
        body.put("message", "Upload " + fileName);
        body.put("content", base64);
        body.put("branch", "main");

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, getHeaders());
        restTemplate.exchange(url, HttpMethod.PUT, request, String.class);
    }



    public void deleteFile(String folderPath, String fileName) {
        String path = folderPath + "/" + fileName;
        String url = "https://api.github.com/repos/" + owner + "/" + repo + "/contents/" + path;

        try {
            logger.info("Fetching SHA for file: {}", path);

            ResponseEntity<Map> res = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(getHeaders()), Map.class);
            Map responseBody = res.getBody();

            if (responseBody == null || !responseBody.containsKey("sha")) {
                logger.error("Failed to retrieve SHA. Response body: {}", responseBody);
                throw new RuntimeException("SHA not found for file: " + path);
            }

            String sha = (String) responseBody.get("sha");

            Map<String, Object> body = new HashMap<>();
            body.put("message", "Delete " + fileName);
            body.put("sha", sha);
            body.put("branch", "main");

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, getHeaders());
            restTemplate.exchange(url, HttpMethod.DELETE, request, String.class);

            logger.info("Deleted file: {}", path);
        } catch (Exception e) {
            logger.error("Error while deleting file: {}", path, e);
            throw new RuntimeException("Failed to delete file: " + fileName, e);
        }
    }


    public String getFileUrl(String folderPath, String fileName) {
        return "https://raw.githubusercontent.com/" + owner + "/" + repo + "/main/" + folderPath + "/" + fileName;
    }



    /**
     * Upload one processed faculty photo to
     *   user_images/faculty/imagepath/{FACULTYID}.jpg
     *
     * @param facultyId  the ID without extension (already uppercase)
     * @param imageBytes processed JPG (cropped, resized) as byte[]
     */
    public void uploadFacultyImage(String facultyId, byte[] imageBytes) {
        String path = "user_images/faculty/" + facultyId + ".jpg";
        String url = "https://api.github.com/repos/" + owner + "/" + repo + "/contents/" + path;

        try {
            String sha = null;

            // 1. Try to get existing file SHA (optional, only if it exists)
            try {
                ResponseEntity<Map> getResponse = restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        new HttpEntity<>(getHeaders()),
                        Map.class
                );

                Map<String, Object> responseBody = getResponse.getBody();
                if (responseBody != null && responseBody.containsKey("sha")) {
                    sha = (String) responseBody.get("sha");
                    logger.info("Existing file SHA found for {}: {}", path, sha);
                }
            } catch (HttpClientErrorException.NotFound e) {
                // Expected when file doesn't exist (first-time upload)
                logger.info("No existing file found for {}, proceeding to create new file.", path);
            }

            // 2. Prepare upload body
            String base64 = Base64.getEncoder().encodeToString(imageBytes);

            Map<String, Object> body = new HashMap<>();
            body.put("message", "Upload faculty image " + facultyId + ".jpg");
            body.put("content", base64);
            body.put("branch", "main");

            // If updating existing file, include SHA
            if (sha != null) {
                body.put("sha", sha);
            }

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, getHeaders());

            // 3. PUT request to create or update the file
            restTemplate.exchange(url, HttpMethod.PUT, request, String.class);

        } catch (Exception e) {
            logger.error("Error uploading faculty image {}: {}", path, e.getMessage(), e);
            throw new RuntimeException("Failed to upload faculty image " + facultyId + ".jpg", e);
        }
    }

    /**
     * Get the raw-content URL for a faculty photo stored at
     *   user_images/faculty/imagepath/{FACULTYID}.jpg
     *
     * @param facultyId faculty ID (uppercase or any case – converted to upper)
     * @return          public GitHub raw URL
     */
    public String getFacultyImageUrl(String facultyId) {
        return "https://raw.githubusercontent.com/" + owner + "/" + repo +
                "/main/user_images/faculty/FACULTY_IMAGES/" + facultyId.toUpperCase() + ".jpg";
    }


}
