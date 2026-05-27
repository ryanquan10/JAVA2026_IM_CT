package org.tio.sitexxx.web.server.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.http.common.UploadFile;
import org.tio.http.server.annotation.RequestPath;
import org.tio.http.server.mvc.Routes;
import org.tio.sitexxx.service.utils.ImgUtils;
import org.tio.sitexxx.web.server.controller.wx.ChatController;
import org.tio.sitexxx.web.server.utils.VideoUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;

/**
 * TestController
 *
 * @author <a href="https://github.com/Zakkoree">Zakkoree</a>
 * @date 2025/6/21
 */
@RequestPath(value = "/test")
public class TestController {
    private static final Logger log = LoggerFactory.getLogger(UploadController.class);
    ChatController chatController = Routes.getController(ChatController.class);

    @RequestPath(value = "/1")
    public String test1(UploadFile uploadFile) {

        try {
            // 读取文件内容
            // 创建 ByteArrayInputStream 对象
            ByteArrayInputStream bais = new ByteArrayInputStream(uploadFile.getData());
            // 提取封面图
            BufferedImage coverImage = VideoUtils.generateCoverFromVideo(bais);
            // 压缩封面图
            byte[] coverBytes = ImgUtils.compressImage(coverImage, 1f, 0.6d, "jpg");
            InputStream inputStream = new ByteArrayInputStream(coverBytes);


            BufferedImage bufferedImage = ImageIO.read(inputStream);


            File outputfile = new File("C:\\Users\\张道云\\Desktop\\备份\\saved_image.png");
            ImageIO.write(bufferedImage, "png", outputfile);
            System.out.println("图像已保存!");

            bais.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return "Success";
    }


    @RequestPath(value = "/1")
    public String Testasdasd() {
        return "Success";
    }
}
