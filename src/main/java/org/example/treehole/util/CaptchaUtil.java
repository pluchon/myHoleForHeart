package org.example.treehole.util;

import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.util.RandomUtil;
import org.springframework.core.io.ClassPathResource;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class CaptchaUtil {

    // 滑动拼图常量
    private static final int JIGSAW_WIDTH = 300;
    private static final int JIGSAW_HEIGHT = 150;
    private static final int BLOCK_WIDTH = 50;
    private static final int BLOCK_HEIGHT = 50;
    private static final int CIRCLE_R = 6; // 凸出部分的半径

    // 文字点选常量
    private static final int TEXT_WIDTH = 300;
    private static final int TEXT_HEIGHT = 150;
    private static final int FONT_SIZE = 24;

    /**
     * 生成滑动拼图验证码
     * @param imagePath relative path in classpath (e.g. static/chatPicture/1.jpg)
     * @return Map containing: backgroundImage (base64), blockImage (base64), x (int), y (int)
     */
    public static Map<String, Object> createJigsawCaptcha(String imagePath) throws IOException {
        // 加载并调整图片大小
        BufferedImage originalImage = ImageIO.read(new ClassPathResource(imagePath).getInputStream());
        BufferedImage bgImage = new BufferedImage(JIGSAW_WIDTH, JIGSAW_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = bgImage.createGraphics();
        g.drawImage(originalImage, 0, 0, JIGSAW_WIDTH, JIGSAW_HEIGHT, null);
        g.dispose();

        // 创建拼图块图像（透明背景）
        BufferedImage blockImage = new BufferedImage(BLOCK_WIDTH, BLOCK_HEIGHT, BufferedImage.TYPE_INT_ARGB);

        // 随机位置
        int x = RandomUtil.randomInt(BLOCK_WIDTH + 20, JIGSAW_WIDTH - BLOCK_WIDTH - 10);
        int y = RandomUtil.randomInt(10, JIGSAW_HEIGHT - BLOCK_HEIGHT - 10);

        // 生成拼图形状
        GeneralPath puzzleShape = getPuzzlePath();

        // 1. 绘制拼图块图像
        Graphics2D g2 = blockImage.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // 裁剪为拼图形状
        g2.setClip(puzzleShape);
        g2.drawImage(bgImage, -x, -y, null);
        g2.setClip(null);
        
        // 绘制白色描边
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(3.0f)); // 粗白色描边
        g2.draw(puzzleShape);
        g2.dispose();

        // 2. 在背景上绘制缺口（黑色蒙版）
        Graphics2D gBg = bgImage.createGraphics();
        gBg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        gBg.translate(x, y);
        gBg.setColor(new Color(0, 0, 0, 150)); // 半透明黑色
        gBg.fill(puzzleShape);
        
        // 可选：为缺口添加微弱描边以增强辨识度
        gBg.setColor(new Color(255, 255, 255, 100));
        gBg.setStroke(new BasicStroke(1.0f));
        gBg.draw(puzzleShape);
        
        gBg.dispose();

        // 转换为Base64
        String bgBase64 = ImgUtil.toBase64(bgImage, "png");
        String blockBase64 = ImgUtil.toBase64(blockImage, "png");

        Map<String, Object> result = new HashMap<>();
        result.put("backgroundImage", "data:image/png;base64," + bgBase64);
        result.put("blockImage", "data:image/png;base64," + blockBase64);
        result.put("x", x);
        result.put("y", y);
        return result;
    }


    /**
     * 生成文字点选验证码
     * @param imagePath relative path
     * @return Map containing: image (base64), words (List<String>), verifyWords (List<Point>)
     */
    public static Map<String, Object> createTextClickCaptcha(String imagePath) throws IOException {
        BufferedImage originalImage = ImageIO.read(new ClassPathResource(imagePath).getInputStream());
        BufferedImage image = new BufferedImage(TEXT_WIDTH, TEXT_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.drawImage(originalImage, 0, 0, TEXT_WIDTH, TEXT_HEIGHT, null);
        
        // 定义随机文字
        String[] possibleWords = {"树", "洞", "天", "空", "云", "海", "山", "水", "风", "雨", "花", "草", "叶", "梦", "爱", "心"};
        List<String> selectedWords = new ArrayList<>();
        List<Point> coords = new ArrayList<>();
        
        // 3个文字
        for (int i = 0; i < 3; i++) {
            String word = possibleWords[RandomUtil.randomInt(possibleWords.length)];
            selectedWords.add(word);
            
            // 随机位置（简单的碰撞检测）
            int wx, wy;
            boolean overlap;
            int attempts = 0;
            do {
                overlap = false;
                wx = RandomUtil.randomInt(20, TEXT_WIDTH - 40);
                wy = RandomUtil.randomInt(30, TEXT_HEIGHT - 20);
                for (Point p : coords) {
                    if (Math.abs(p.x - wx) < 40 && Math.abs(p.y - wy) < 40) {
                        overlap = true;
                        break;
                    }
                }
                attempts++;
            } while (overlap && attempts < 10);
            
            coords.add(new Point(wx, wy));
            
            // 绘制文字
            g.setColor(new Color(RandomUtil.randomInt(200, 255), RandomUtil.randomInt(200, 255), RandomUtil.randomInt(200, 255)));
            g.setFont(new Font("Microsoft YaHei", Font.BOLD, FONT_SIZE));
            // 添加旋转
            double theta = RandomUtil.randomDouble(-0.5, 0.5);
            g.rotate(theta, wx, wy);
            g.drawString(word, wx, wy);
            g.rotate(-theta, wx, wy);
        }
        g.dispose();

        String imgBase64 = ImgUtil.toBase64(image, "png");
        
        Map<String, Object> result = new HashMap<>();
        result.put("image", "data:image/png;base64," + imgBase64);
        result.put("words", selectedWords); // 发送文字给前端显示“请点击：X, Y, Z”
        result.put("coords", coords); // 存入Session
        return result;
    }

    // 拼图形状辅助方法（使用Java 2D路径替代）
    private static java.awt.geom.GeneralPath getPuzzlePath() {
        java.awt.geom.GeneralPath path = new java.awt.geom.GeneralPath();
        
        // 形状定义：右侧凸出，上方凸出的正方形
        // 尺寸大约为34x34正方形加上凸出部分
        float s = 34.0f; 
        float m = (BLOCK_WIDTH - s) / 2.0f; // 边距 ~8
        float r = 5.0f; // 凸出部分的半径

        // 从左上角开始
        path.moveTo(m, m);
        
        // 上边（凸出）
        path.lineTo(m + s/2 - r, m);
        // 半圆凸出
        path.quadTo(m + s/2 - r, m - r*1.5, m + s/2, m - r*1.5); // 控制点向上
        path.quadTo(m + s/2 + r, m - r*1.5, m + s/2 + r, m); // 向下
        path.lineTo(m + s, m);
        
        // 右边（凸出）
        path.lineTo(m + s, m + s/2 - r);
        path.quadTo(m + s + r*1.5, m + s/2 - r, m + s + r*1.5, m + s/2);
        path.quadTo(m + s + r*1.5, m + s/2 + r, m + s, m + s/2 + r);
        path.lineTo(m + s, m + s);
        
        // 下边（凹入）
        path.lineTo(m + s/2 + r, m + s);
        path.quadTo(m + s/2 + r, m + s - r*1.5, m + s/2, m + s - r*1.5);
        path.quadTo(m + s/2 - r, m + s - r*1.5, m + s/2 - r, m + s);
        path.lineTo(m, m + s);
        
        // 左边（直线）
        path.lineTo(m, m);
        
        path.closePath();
        return path;
    }

}
