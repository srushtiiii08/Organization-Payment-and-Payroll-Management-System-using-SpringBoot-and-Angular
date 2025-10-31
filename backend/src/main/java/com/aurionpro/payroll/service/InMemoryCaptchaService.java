package com.aurionpro.payroll.service;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.imageio.ImageIO;

import org.springframework.stereotype.Service;

import com.aurionpro.payroll.exception.BadRequestException;

import lombok.Data;

/**
 * In-Memory CAPTCHA Service - No database required!
 * Stores captchas in memory using ConcurrentHashMap
 */
@Service
public class InMemoryCaptchaService {
    
	
	private final Map<String, CaptchaData> captchaStore = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();
    
    // CAPTCHA configuration
    private static final int IMAGE_WIDTH = 200;
    private static final int IMAGE_HEIGHT = 70;
    private static final int TEXT_LENGTH = 6;
    private static final String CHARACTERS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // Excluding confusing chars like O, 0, I, 1
    
    @Data
    static class CaptchaData {
        String answer;
        LocalDateTime expiresAt;
    }
    
    /**
     * Generate a new image-based CAPTCHA
     * @return String in format "sessionId:base64Image"
     */
    public String generateCaptcha() {
        try {
            // Generate random text
            String captchaText = generateRandomText();
            
            // Create unique session ID
            String sessionId = UUID.randomUUID().toString();
            
            // Store captcha data
            CaptchaData data = new CaptchaData();
            data.setAnswer(captchaText);
            data.setExpiresAt(LocalDateTime.now().plusMinutes(5));
            captchaStore.put(sessionId, data);
            
            // Generate image
            BufferedImage image = createCaptchaImage(captchaText);
            
            // Convert image to Base64
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            byte[] imageBytes = baos.toByteArray();
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);
            
            // Clean up old captchas
            cleanExpired();
            
            // Return format: "sessionId:data:image/png;base64,<base64Image>"
            return sessionId + ":data:image/png;base64," + base64Image;
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate CAPTCHA: " + e.getMessage());
        }
    }
    
    /**
     * Generate random alphanumeric text for CAPTCHA
     */
    private String generateRandomText() {
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < TEXT_LENGTH; i++) {
            int index = random.nextInt(CHARACTERS.length());
            text.append(CHARACTERS.charAt(index));
        }
        return text.toString();
    }
    
    /**
     * Create CAPTCHA image with distortions and noise
     */
    private BufferedImage createCaptchaImage(String text) {
        BufferedImage image = new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        
        // Enable anti-aliasing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Background - Gradient
        Color color1 = new Color(240, 240, 255);
        Color color2 = new Color(220, 220, 240);
        for (int i = 0; i < IMAGE_HEIGHT; i++) {
            float ratio = (float) i / IMAGE_HEIGHT;
            int red = (int) (color1.getRed() * (1 - ratio) + color2.getRed() * ratio);
            int green = (int) (color1.getGreen() * (1 - ratio) + color2.getGreen() * ratio);
            int blue = (int) (color1.getBlue() * (1 - ratio) + color2.getBlue() * ratio);
            g2d.setColor(new Color(red, green, blue));
            g2d.drawLine(0, i, IMAGE_WIDTH, i);
        }
        
        // Add noise lines
        g2d.setColor(new Color(200, 200, 220));
        for (int i = 0; i < 8; i++) {
            int x1 = random.nextInt(IMAGE_WIDTH);
            int y1 = random.nextInt(IMAGE_HEIGHT);
            int x2 = random.nextInt(IMAGE_WIDTH);
            int y2 = random.nextInt(IMAGE_HEIGHT);
            g2d.drawLine(x1, y1, x2, y2);
        }
        
        // Add noise dots
        for (int i = 0; i < 100; i++) {
            int x = random.nextInt(IMAGE_WIDTH);
            int y = random.nextInt(IMAGE_HEIGHT);
            g2d.setColor(new Color(random.nextInt(255), random.nextInt(255), random.nextInt(255), 100));
            g2d.fillOval(x, y, 2, 2);
        }
        
        // Draw text with variations
        g2d.setFont(new Font("Arial", Font.BOLD, 32));
        int startX = 20;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            
            // Random color for each character
            g2d.setColor(new Color(
                random.nextInt(100), 
                random.nextInt(100), 
                random.nextInt(150)
            ));
            
            // Random rotation
            int angle = random.nextInt(30) - 15; // -15 to +15 degrees
            g2d.rotate(Math.toRadians(angle), startX, IMAGE_HEIGHT / 2);
            
            // Draw character with slight vertical offset
            int yOffset = random.nextInt(10) - 5;
            g2d.drawString(String.valueOf(c), startX, IMAGE_HEIGHT / 2 + yOffset);
            
            // Reset rotation
            g2d.rotate(-Math.toRadians(angle), startX, IMAGE_HEIGHT / 2);
            
            startX += 28;
        }
        
        g2d.dispose();
        return image;
    }
    
    /**
     * Validate user's CAPTCHA answer (case-insensitive)
     */
    public boolean validateCaptcha(String sessionId, String userAnswer) {
        CaptchaData data = captchaStore.get(sessionId);
        if (data == null) {
            throw new BadRequestException("Invalid or expired CAPTCHA session. Please refresh and try again.");
        }
        
        if (LocalDateTime.now().isAfter(data.getExpiresAt())) {
            captchaStore.remove(sessionId);
            throw new BadRequestException("CAPTCHA has expired. Please request a new one.");
        }
        
        // Remove after use (one-time use)
        captchaStore.remove(sessionId);
        
        // Case-insensitive comparison
        return data.getAnswer().equalsIgnoreCase(userAnswer.trim());
    }
    
    /**
     * Clean up expired captchas
     */
    private void cleanExpired() {
        captchaStore.entrySet().removeIf(entry -> 
            LocalDateTime.now().isAfter(entry.getValue().getExpiresAt())
        );
    }
    
    /**
     * Get active captcha count (for debugging)
     */
    public int getActiveCount() {
        cleanExpired();
        return captchaStore.size();
    }
}
	
	
	
	
	
	
	
	
	
	
//    // Store captchas in memory (thread-safe)
//    private final Map<String, CaptchaData> captchaStore = new ConcurrentHashMap<>();
//    private final SecureRandom random = new SecureRandom();
//    
//    /**
//     * Internal class to store captcha data
//     */
//    @Data
//    static class CaptchaData {
//        String answer;
//        LocalDateTime expiresAt;
//    }
//    
//    /**
//     * Generate a new math captcha
//     * @return String in format "sessionId:question" (e.g., "uuid123:5+3")
//     */
//    public String generateCaptcha() {
//        // Generate random numbers (1-10)
//        int num1 = random.nextInt(10) + 1;
//        int num2 = random.nextInt(10) + 1;
//        int answer = num1 + num2;
//        
//        // Create unique session ID
//        String sessionId = UUID.randomUUID().toString();
//        
//        // Store captcha data
//        CaptchaData data = new CaptchaData();
//        data.setAnswer(String.valueOf(answer));
//        data.setExpiresAt(LocalDateTime.now().plusMinutes(5)); // 5-minute expiry
//        
//        captchaStore.put(sessionId, data);
//        
//        // Clean up old captchas (optional, runs on each generation)
//        cleanExpired();
//        
//        // Return format: "sessionId:question"
//        return sessionId + ":" + num1 + "+" + num2;
//    }
//    
//    /**
//     * Validate user's captcha answer
//     * @param sessionId The captcha session ID
//     * @param userAnswer The user's answer
//     * @return true if correct, false otherwise
//     */
//    public boolean validateCaptcha(String sessionId, String userAnswer) {
//        
//        // Check if captcha exists
//        CaptchaData data = captchaStore.get(sessionId);
//        if (data == null) {
//            throw new BadRequestException("Invalid or expired CAPTCHA session. Please refresh and try again.");
//        }
//        
//        // Check if expired
//        if (LocalDateTime.now().isAfter(data.getExpiresAt())) {
//            captchaStore.remove(sessionId);
//            throw new BadRequestException("CAPTCHA has expired. Please request a new one.");
//        }
//        
//        // Remove after use (one-time use only)
//        captchaStore.remove(sessionId);
//        
//        // Validate answer
//        return data.getAnswer().equals(userAnswer.trim());
//    }
//    
//    /**
//     * Clean up expired captchas from memory
//     */
//    private void cleanExpired() {
//        captchaStore.entrySet().removeIf(entry -> 
//            LocalDateTime.now().isAfter(entry.getValue().getExpiresAt())
//        );
//    }
//    
//    /**
//     * Get current captcha store size (for debugging)
//     */
//    public int getActiveCount() {
//        cleanExpired();
//        return captchaStore.size();
//    }
//}
