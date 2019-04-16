package hb.kg.content.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.annotation.Resource;

import org.apache.commons.fileupload.disk.DiskFileItem;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.ObjectMetadata;

import hb.kg.common.dao.BaseDao;
import hb.kg.common.service.BaseCRUDService;
import hb.kg.common.util.json.JSONObject;
import hb.kg.content.bean.mongo.HBArticleMediaDownload;
import hb.kg.content.dao.ArticleMediaDownloadDao;
import hb.kg.user.bean.http.HBUserBasic;

@Service
public class ArticleMediaDownloadService extends BaseCRUDService<HBArticleMediaDownload> {
    @Resource
    private ArticleMediaDownloadDao articleMediaDownloadDao;

    public BaseDao<HBArticleMediaDownload> dao() {
        return articleMediaDownloadDao;
    }

    public JSONObject uploadMediaToOss(MultipartFile file,
                                       String userid)
            throws IOException {
        JSONObject jsobj = new JSONObject();
        CommonsMultipartFile cf = (CommonsMultipartFile) file;
        DiskFileItem fil = (DiskFileItem) cf.getFileItem();
        File fi = fil.getStoreLocation();// MultipartFile转临时file
        String originalFilename = file.getOriginalFilename(); // 上传时的文件名带后缀
        String[] downloadFileToks = originalFilename.split("\\."); // 分割
        String fileType = downloadFileToks != null ? downloadFileToks[downloadFileToks.length - 1]
                : "";
        HBArticleMediaDownload df = new HBArticleMediaDownload();
        df.prepareBaseBean();
        String encodeFileName = df.getId() + "." + fileType; // 文件id+后缀为更改后的文件名
        OSSClient client = new OSSClient(sysConfService.getOssEndPoint(),
                                         sysConfService.getOssAccessId(),
                                         sysConfService.getOssAccessKey());
        try {
            uploadFile(client,
                       sysConfService.getOssBucketArticlemediaName(),
                       encodeFileName,
                       fileType,
                       fi);// 上传文件流
            df.setFilename(originalFilename); // 原文件名带文件类型后缀
            df.setTitle(encodeFileName); // 文件id+后缀为更改后的文件名
            df.setFiletype(fileType);// 更改后的文件名
            df.setAuthor(new HBUserBasic(userid));
            df.setUrl("https://" + sysConfService.getOssBucketArticlemediaName()
                    + ".oss-cn-beijing.aliyuncs.com/" + encodeFileName);
            df.setSize(getPrintSize(file.getSize()));
            dao().insert(df);
            logger.info("用户上传文件:" + fi + encodeFileName);
            jsobj.put("state", "SUCCESS");
            jsobj.put("url", df.getUrl());
            jsobj.put("title", df.getTitle());
            jsobj.put("original", df.getFilename());
            jsobj.put("type", df.getFiletype());
            jsobj.put("size", df.getSize());
        } catch (Exception e) {
            logger.error("上传失败：检查文件是否小于2048");
            jsobj.put("state", "ERROR");
            jsobj.put("ERROR", "上传失败：检查文件是否小于2k");
            return jsobj;
        } finally {
            client.shutdown();// 关闭client
        }
        return jsobj;
    }

    // 上传文件oss
    private void uploadFile(OSSClient client,
                            String bucketName,
                            String Objectkey,
                            String fileType,
                            File fi)
            throws OSSException, ClientException, IOException {
        ObjectMetadata objectMeta = new ObjectMetadata();
        objectMeta.setContentLength(fi.length());
        switch (fileType) {
        case "gif":
            objectMeta.setContentType("image/gif");
            break;
        case "png":
            objectMeta.setContentType("image/png");
            break;
        case "jpeg":
        case "jpe":
            objectMeta.setContentType("image/jpeg");
            break;
        case "html":
            objectMeta.setContentType("text/html");
            break;
        case "jkg":
            objectMeta.setContentType("application/x-jkg");
            break;
        case "mp4":
            objectMeta.setContentType("video/mpeg4");
            break;
        default:
            objectMeta.setContentType("application/octet-stream");
            break;
        }
        InputStream input = new FileInputStream(fi);
        client.putObject(bucketName, Objectkey, input, objectMeta);
        input.close();// 关闭io流
    }

    /**
     * 把字节数B转化为KB、MB、GB的方法
     * @param size
     * @return
     */
    public String getPrintSize(long size) {
        // 如果字节数少于1024，则直接以B为单位，否则先除于1024，后3位因太少无意义
        if (size < 1024) {
            return String.valueOf(size) + "B";
        } else {
            size = size / 1024;
        }
        // 如果原字节数除于1024之后，少于1024，则可以直接以KB作为单位
        // 因为还没有到达要使用另一个单位的时候
        // 接下去以此类推
        if (size < 1024) {
            return String.valueOf(size) + "KB";
        } else {
            size = size / 1024;
        }
        if (size < 1024) {
            // 因为如果以MB为单位的话，要保留最后1位小数，
            // 因此，把此数乘以100之后再取余
            size = size * 100;
            return String.valueOf((size / 100)) + "." + String.valueOf((size % 100)) + "MB";
        } else {
            // 否则如果要以GB为单位的，先除于1024再作同样的处理
            size = size * 100 / 1024;
            return String.valueOf((size / 100)) + "." + String.valueOf((size % 100)) + "GB";
        }
    }

    public String deleteOSSFile(String fileid) {
        OSSClient client = new OSSClient(sysConfService.getOssEndPoint(),
                                         sysConfService.getOssAccessId(),
                                         sysConfService.getOssAccessKey());
        boolean found = client.doesObjectExist(sysConfService.getOssBucketArticlemediaName(),
                                               fileid);// 查看文件是否存在
        if (found == true) {
            client.deleteObject(sysConfService.getOssBucketArticlemediaName(), fileid);
            client.shutdown();// 关闭client
            return fileid;
        }
        client.shutdown();// 关闭client
        return null;
    }
}
