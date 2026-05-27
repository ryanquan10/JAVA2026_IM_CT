import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import org.tio.sitexxx.all.Starter;
import org.tio.sitexxx.service.model.main.User;
import org.tio.sitexxx.service.utils.TencentOssUtils;
import org.tio.sitexxx.service.vo.Const;
import org.tio.sitexxx.web.server.utils.VideoUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Test
 *
 * @author <a href="https://github.com/Zakkoree">Zakkoree</a>
 * @date 2025/6/23
 */
public class Test {


    public static void main(String[] args) throws SQLException, FileNotFoundException {
        Starter.initBase();
//        File file = new File("D:\\911Mothers_2010W-480p.mp4");
        File file = new File("D:\\敬礼.png");
        FileInputStream inputStream = new FileInputStream(file);
        long size = file.length();
        String contentType = "image/png"; // 根据实际文件类型设置
//        String contentType = "video/mp4"; // 根据实际文件类型设置

        COSClient cosClient = TencentOssUtils.init();
//        // 指定要上传的文件
        String bucketName = Const.TencentOss.BUCKET_NAME;
        long startTime = System.currentTimeMillis();
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, "敬礼.png", file);
        cosClient.putObject(putObjectRequest);
//        TencentOssUtils.multipartUploads("敬礼.png", inputStream, size, contentType);
        long endTime = System.currentTimeMillis();
        System.out.println("上传耗时: " + (endTime - startTime) + "ms");
    }
}
