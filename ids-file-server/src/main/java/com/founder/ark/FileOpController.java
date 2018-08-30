package com.founder.ark;

import com.founder.ark.common.utils.CodeUtil;
import com.founder.ark.common.utils.bean.ResponseObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
public class FileOpController {

    private static Logger logger = LoggerFactory.getLogger(FileOpController.class);

    @Value("${server.file-mapping-path}")
    private String mappingPath;

    @Value("${server.file-save-path}")
    private String savePath;

    @Value("${server.root-path}")
    private String rootPath;

    /**
     * 文件上传
     *
     * @param file 全路径
     * @return
     * @throws IOException
     * @throws IllegalStateException
     */
    @RequestMapping(value = "/upload")
//    @CrossOrigin
    public ResponseObject<?> photo(MultipartFile file) throws IllegalStateException, IOException {

        rootPath = rootPath.endsWith("/") ? rootPath.substring(0, rootPath.lastIndexOf("/")) : rootPath;
        mappingPath = !mappingPath.startsWith("/") ? "/" + mappingPath : mappingPath;
        mappingPath = !mappingPath.endsWith("/") ? mappingPath + "/" : mappingPath;
        savePath = !savePath.endsWith(File.separator) ? savePath + File.separator : savePath;

        if (file == null || file.getSize() <= 0) {
            return ResponseObject.newErrorResponseObject(1701, "请上传文件！");
        }

        if ( file.getSize() > 52428800 ) {
            return ResponseObject.newErrorResponseObject(1702, "文件过大。");
        }

        String fileName = file.getOriginalFilename();
        String extension = fileName.substring(
                fileName.lastIndexOf(".") < 0 ? fileName.length() : fileName.lastIndexOf("."));
        String uuid = CodeUtil.getRandomUUID();

        File save = new File(savePath);
        if (!save.exists()) {
            save.mkdirs();
        }

        file.transferTo(new File(savePath + uuid + extension));

        Map<String, Object> returnMap = new HashMap<>();
        returnMap.put("originalFilename", fileName);
        returnMap.put("newFileName", uuid + extension);
        returnMap.put("bytesSize", file.getSize());
        returnMap.put("url", rootPath + mappingPath + uuid + extension);

        logger.info("上传文件：{}", savePath + uuid + extension);

        return ResponseObject.newSuccessResponseObject(returnMap, "上传成功！");
    }

}
