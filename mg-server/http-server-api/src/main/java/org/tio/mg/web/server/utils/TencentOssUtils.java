package org.tio.mg.web.server.utils;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.BasicSessionCredentials;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.exception.CosServiceException;
import com.qcloud.cos.model.*;
import com.qcloud.cos.region.Region;
import com.qcloud.cos.transfer.TransferManager;
import com.qcloud.cos.transfer.TransferManagerConfiguration;
import com.qcloud.cos.transfer.Upload;
import org.tio.sitexxx.service.vo.Const;

import java.io.File;
import java.io.InputStream;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.qcloud.cos.demo.BucketReplicationDemo.createCOSClient;

/**
 * TencentOssUtils
 *
 * @author <a href="https://github.com/Zakkoree">Zakkoree</a>
 * @date 2025/8/6
 */
public class TencentOssUtils {

    private static final int CORE_POOL_SIZE = Runtime.getRuntime().availableProcessors();

    private static volatile ExecutorService sharedThreadPool;

    public static COSClient init() {
        // 1 传入获取到的临时密钥 (tmpSecretId, tmpSecretKey, sessionToken)
        String tmpSecretId = Const.TencentOss.SECRET_ID;
        String tmpSecretKey =  Const.TencentOss.SECRET_KEY;
        BasicCOSCredentials cred = new BasicCOSCredentials(tmpSecretId, tmpSecretKey);
        // 2 设置 bucket 的地域
        // clientConfig 中包含了设置 region, https(默认 http), 超时, 代理等 set 方法, 使用可参见源码或者常见问题 Java SDK 部分
        Region region = new Region(Const.TencentOss.REGION); //COS_REGION 参数：配置成存储桶 bucket 的实际地域，例如 ap-beijing，更多 COS 地域的简称请参见 https://intl.cloud.tencent.com/document/product/436/6224
        ClientConfig clientConfig = new ClientConfig(region);
        clientConfig.setConnectionTimeout(5000);
        clientConfig.setMaxErrorRetry(3);
        // 3 生成 cos 客户端
        return new COSClient(cred, clientConfig);
    }

    private static void shutdownTransferManager(TransferManager transferManager) {
        // 指定参数为 true, 则同时会关闭 transferManager 内部的 COSClient 实例。
        // 指定参数为 false, 则不会关闭 transferManager 内部的 COSClient 实例。
        transferManager.shutdownNow(true);
    }

    public static void multipartUploads(String objectKey, InputStream inputStream, long size, String contentType) {
        // 使用高级接口必须先保证本进程存在一个 TransferManager 实例，如果没有则创建
        // 详细代码参见本页：高级接口 -> 创建 TransferManager
        COSClient cosClient = init();
        // 自定义线程池大小，建议在客户端与 COS 网络充足（例如使用腾讯云的 CVM，同地域上传 COS）的情况下，设置成16或32即可，可较充分的利用网络资源
        // 对于使用公网传输且网络带宽质量不高的情况，建议减小该值，避免因网速过慢，造成请求超时。
        ExecutorService threadPool = Executors.newFixedThreadPool(32);
        // 传入一个 threadpool, 若不传入线程池，默认 TransferManager 中会生成一个单线程的线程池。
        TransferManager transferManager = new TransferManager(cosClient, threadPool);
        // 设置高级接口的配置项
        // 分块上传阈值和分块大小分别为 50MB 和 10MB
        TransferManagerConfiguration transferManagerConfiguration = new TransferManagerConfiguration();
        transferManagerConfiguration.setMultipartUploadThreshold(10*1024*1024);
        transferManagerConfiguration.setMinimumUploadPartSize(5*1024*1024);
        transferManager.setConfiguration(transferManagerConfiguration);

        // 存储桶的命名格式为 BucketName-APPID，此处填写的存储桶名称必须为此格式
        String bucketName = Const.TencentOss.BUCKET_NAME;

        // 这里创建一个 ByteArrayInputStream 来作为示例，实际中这里应该是您要上传的 InputStream 类型的流

        ObjectMetadata metadata = new ObjectMetadata();
        // 上传的流如果能够获取准确的流长度，则推荐一定填写 content-length
        // 如果确实没办法获取到，则下面这行可以省略，但同时高级接口也没办法使用分块上传了
        metadata.setContentLength(size);
        metadata.setContentType(contentType);

        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, objectKey, inputStream, metadata);

        // 设置存储类型（如有需要，不需要请忽略此行代码）, 默认是标准(Standard), 低频(standard_ia)
        // 更多存储类型请参见 https://cloud.tencent.com/document/product/436/33417
//        putObjectRequest.setStorageClass(StorageClass.Standard_IA);
        try {
            // 高级接口会返回一个异步结果Upload
            // 可同步地调用 waitForUploadResult 方法等待上传完成，成功返回UploadResult, 失败抛出异常
            Upload upload = transferManager.upload(putObjectRequest);
            UploadResult uploadResult = upload.waitForUploadResult();
            // 设置公有读私有写
            cosClient.setObjectAcl(bucketName, objectKey, CannedAccessControlList.PublicRead);
        } catch (CosServiceException e) {
            e.printStackTrace();
        } catch (CosClientException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            // 确定本进程不再使用 transferManager 实例之后，关闭之
            shutdownTransferManager(transferManager);
        }
    }


    public static void upload(String objectKey, InputStream inputStream, long size, String contentType) {
        uploadObject(objectKey, inputStream, size, contentType);
    }


    public static void uploadObject(String objectKey, InputStream inputStream, long size, String contentType) {
        COSClient cosClient = init();

        // 指定要上传的文件
        String bucketName = Const.TencentOss.BUCKET_NAME;
        ObjectMetadata metadata = new ObjectMetadata();
        // 设置内容类型
        metadata.setContentType(contentType);
        metadata.setContentLength(size);
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, objectKey, inputStream, metadata);
        PutObjectResult putObjectResult = cosClient.putObject(putObjectRequest);
        // 设置公有读私有写
        cosClient.setObjectAcl(bucketName, objectKey, CannedAccessControlList.PublicRead);
    }

    public static void uploadObject(File file, String key) {
        COSClient cosClient = init();
        // 指定要上传的文件
        String bucketName = Const.TencentOss.BUCKET_NAME;
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, key, file);
        PutObjectResult putObjectResult = cosClient.putObject(putObjectRequest);
    }

}
