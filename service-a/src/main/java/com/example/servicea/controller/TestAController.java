package com.example.servicea.controller;

import com.deepoove.poi.XWPFTemplate;
import com.deepoove.poi.config.Configure;
import com.deepoove.poi.data.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * @Classname TestAController
 * @Description TODO
 * @Date 2022/2/14 13:35
 * @Created by jt
 * @projectName
 */
@RestController
@RequestMapping("/service-objcat-a")
// 添加注解声明是注册中心客户端
@EnableEurekaClient
// 实现不同子服务调用
@EnableFeignClients
public class TestAController {

    @Autowired
    private FeignClient feignClient;

    private static final int  BUFFER_SIZE = 2 * 1024;

    @RequestMapping("call")
    public String call(){
        String result = feignClient.TestAController();
        return "a to b 访问结果：" + result;
    }

    @RequestMapping("exportZip")
    public void exportZip(HttpServletResponse response) throws IOException {
        for(int j=0;j<3;j++){
            ZipOutputStream zos=null;
            ByteArrayOutputStream arrayOutputStream=null;
            //获取系统的路径
            ClassPathResource pathResource = new ClassPathResource("file/zip");
            String path = pathResource.getURL().getPath();
            File file = new File(path);
            OutputStream fileOutputStream = new FileOutputStream(path+"\\答复内容"+j+".zip");
            zos = new ZipOutputStream(fileOutputStream);//压缩文件输出流
            arrayOutputStream= new ByteArrayOutputStream();//ByteArray临时存储流
            ClassPathResource classPathResource = new ClassPathResource("file/dfnr.docx");
            String resource = classPathResource.getURL().getPath();
            for(int i=0;i<3;i++){
                XWPFTemplate template = XWPFTemplate.compile(resource);
                // 设置文件名
                String fileName = "("+i+").docx";
                fileName=fileName.replaceAll("/","-");
                fileName=fileName.replaceAll("null","");
                zos.putNextEntry(new ZipEntry(fileName));//放入zipEntry实体，取一个名字
                arrayOutputStream.reset();//重置ByteArray流（为了重复使用）
                template.write(arrayOutputStream);//把word对象内容写到ByteArray流临时存储
                zos.write(arrayOutputStream.toByteArray());//把ByteArray流内容写入zip输出流
                template.close();
            }
            arrayOutputStream.close();//关闭ByteArray流
            zos.flush();//刷新zip输出流缓冲区
            zos.close();//关闭zip输出流
        }
    }


    @RequestMapping("download")
    public void download(HttpServletResponse response,String path) throws IOException {
         response.reset();
         response.setContentType("application/octet-stream");// 指明response的返回对象是文件流
         response.setHeader("Content-Disposition", "attachment;filename=" +new String("意见合集".getBytes("GB2312"),"iso-8859-1")+".zip");
         toZip(path, response.getOutputStream(),true);     //response.getOutputStream()浏览器下载压缩文件
        /** 5.删除临时文件和文件夹 */
        File file = new File(path);
        File[] listFiles = file.listFiles();
        for (int i = 0; i < listFiles.length; i++) {
            listFiles[i].delete();
        }
        file.delete();
    }


    public static void toZip(String srcDir, OutputStream out, boolean KeepDirStructure) throws RuntimeException{
        long start = System.currentTimeMillis();
        ZipOutputStream zos = null ;
        try {
            zos = new ZipOutputStream(out);
            File sourceFile = new File(srcDir);
            compress(sourceFile,zos,sourceFile.getName(),KeepDirStructure);
            long end = System.currentTimeMillis();
            System.out.println("压缩完成，耗时：" + (end - start) +" ms");
        } catch (Exception e) {
            throw new RuntimeException("zip error from ZipUtils",e);
        }finally{
            if(zos != null){
                try {
                    zos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void compress(File sourceFile, ZipOutputStream zos, String name, boolean KeepDirStructure) throws Exception{
        byte[] buf = new byte[BUFFER_SIZE];
        if(sourceFile.isFile()){  //是否为文件
            // 向zip输出流中添加一个zip实体，构造器中name为zip实体的文件的名字
            zos.putNextEntry(new ZipEntry(name));
            // copy文件到zip输出流中
            int len;
            FileInputStream in = new FileInputStream(sourceFile);
            while ((len = in.read(buf)) != -1){
                zos.write(buf, 0, len);
            }
            // Complete the entry
            zos.closeEntry();
            in.close();
        } else {
            File[] listFiles = sourceFile.listFiles();
            if(listFiles == null || listFiles.length == 0){
                // 需要保留原来的文件结构时,需要对空文件夹进行处理
                if(KeepDirStructure){
                    // 空文件夹的处理
                    zos.putNextEntry(new ZipEntry(name + "/"));
                    // 没有文件，不需要文件的copy
                    zos.closeEntry();
                }
            }else {
                for (File file : listFiles) {
                    // 判断是否需要保留原来的文件结构
                    if (KeepDirStructure) {
                        // 注意：file.getName()前面需要带上父文件夹的名字加一斜杠,
                        // 不然最后压缩包中就不能保留原来的文件结构,即：所有文件都跑到压缩包根目录下了
                        compress(file, zos, name + "/" + file.getName(),KeepDirStructure);
                    } else {
                        compress(file, zos, file.getName(),KeepDirStructure);
                    }

                }
            }
        }
    }

    @RequestMapping("/exportDataWord")
    public void exportDataWord(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ClassPathResource classPathResource = new ClassPathResource("file/template.docx");
        String resource = classPathResource.getURL().getPath();
        RowRenderData row0 = Rows.of("姓名", "学历").textColor("FFFFFF")
                .bgColor("4472C4").center().create();
        RowRenderData row1 = Rows.create("李四", "博士");
        XWPFTemplate template = XWPFTemplate.compile(resource).render(
                new HashMap<String, Object>(){{
                    put("name", "Sayi");
                    put("author", new TextRenderData("000000", "Sayi"));
                    put("link", new HyperlinkTextRenderData("website", "www.baidu.com"));
                    put("anchor", new HyperlinkTextRenderData("anchortxt", "anchor:appendix1"));
                    put("table1", Tables.create(row0, row1));
                }});
        response.setContentType("application/force-download");
        // 设置文件名
        String fileName = URLEncoder.encode("导出word", "UTF-8");
        response.addHeader("Content-Disposition", "attachment;fileName=" +fileName+".docx");
//        OutputStream out = response.getOutputStream();
        File file = new File(resource);
        OutputStream out = new FileOutputStream(file.getParentFile()+"/zip/答复内容"+".zip");
        template.write(out);
        out.flush();
        out.close();
        template.close();
    }

    //word文件压缩成压缩文件导出
    public void zipInWord(HttpServletResponse response) throws IOException {
        response.reset();
        response.setContentType("application/octet-stream");// 指明response的返回对象是文件流
        response.setHeader("Content-Disposition", "attachment;filename=" + new String("答复内容".getBytes("GB2312"), "iso-8859-1") + ".zip");
        ZipOutputStream zos = new ZipOutputStream(response.getOutputStream());//压缩文件输出流
        ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();//ByteArray临时存储流
        ClassPathResource classPathResource = new ClassPathResource("file/dfnr.docx");
        String resource = classPathResource.getURL().getPath();
        Configure config = Configure.newBuilder().build();
        XWPFTemplate template = XWPFTemplate.compile(resource, config);
        template.render(
                new HashMap<String, Object>() {{
                    put("result", "");
                }});
        // 设置文件名
        String fileName = "(" + ").docx";
        fileName = fileName.replaceAll("/", "-");
        fileName = fileName.replaceAll("null", "");
        zos.putNextEntry(new ZipEntry(fileName));//放入zipEntry实体，取一个名字
        arrayOutputStream.reset();//重置ByteArray流（为了重复使用）
        template.write(arrayOutputStream);//把word对象内容写到ByteArray流临时存储
        zos.write(arrayOutputStream.toByteArray());//把ByteArray流内容写入zip输出流
        template.close();
        zos.closeEntry(); //关闭zipEntry
        arrayOutputStream.close(); //关闭ByteArray流
        zos.flush(); //刷新zip输出流缓冲区
        zos.close(); //关闭zip输出流
    }
}
