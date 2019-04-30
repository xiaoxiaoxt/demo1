package com.xiaoxiaoxt.demo.demo1;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.*;

@Controller
public class controller {
    @Value("${file.uploadPath}")
    String uploadPath;
    @Value("${file.imgPath}")
    String imgPath;
    @Value("${file.vedioPath}")
    String vedioPath;
    @Value("${file.picAddr}")
    String picAddr;
    @Value("${file.logPath}")
    String logPath;
    @Value("${file.cutImg.cmd}")
    String cutImgCmd;
    @Value("${file.cutVedio.cmd}")
    String cutVedioCmd;
    @Value("${file.addLog.cmd}")
    String addLogCmd;
    @Value("${file.isVedio.cmd}")
    String isVedioCmd;

    @RequestMapping("/")
    public String index(){
        return "index.html";
    }

    @PostMapping("/upload")
    @ResponseBody
    public String upload(@RequestParam("fileName") MultipartFile file, HttpServletRequest request) throws IOException {
        File desfile=null;//接受文件绝对地址
        String fileName=null;
        if (!file.isEmpty()){
            try {
                fileName=file.getOriginalFilename();
                desfile=new File(uploadPath+fileName);
                // 转存文件
                file.transferTo(desfile);
                //判断目标文件是否为视频文件
                if(isVedio(uploadPath+fileName)){
                    cutImg(uploadPath,fileName);
                    cutVedio(uploadPath,fileName);
                    addLog(uploadPath,fileName);
                    return "上传成功";
                }else{
                    desfile.delete();
                    return "您上传的文件格式不对";
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return "上传失败";
    }
    //判断是否为视频文件
    private boolean isVedio(String fileFullPath)throws IOException{
        isVedioCmd=isVedioCmd.replace("oldPath",fileFullPath);
        Runtime run = Runtime.getRuntime();
        try {
            Process process = run.exec(isVedioCmd);
            BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = null;
            StringBuilder sb = new StringBuilder();
            while ((line = br.readLine()) != null) {
                sb.append(line + "\n");
            }
            String json=sb.toString();
            String flag="format_name";
            int i = json.indexOf(flag);
            String subJson=json.substring(i+1);
            if(subJson.contains("mov")||subJson.contains("mp4")
                    ||subJson.contains("m4a")||subJson.contains("3pg")
                    ||subJson.contains("3g2")||subJson.contains("mj2")){
                return true;
            }else {
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    //截取图片,第2秒钟的图片
    private void cutImg(String filePath,String fileName) throws IOException {
        cutImgCmd=cutImgCmd.replace("oldPath",filePath+fileName);
        int index = fileName.lastIndexOf(".");
        fileName = fileName.substring(0, index)+".jpg";
        cutImgCmd=cutImgCmd.replace("newPath",imgPath+fileName);
        Process process = Runtime.getRuntime().exec(cutImgCmd);
    }

    //截取视频，2秒
    private void cutVedio(String filePath,String fileName) throws IOException {
        cutVedioCmd=cutVedioCmd.replace("oldPath",filePath+fileName).replace("newPath",vedioPath+fileName);
        Process process = Runtime.getRuntime().exec(cutVedioCmd);
    }

    //加log
    private void addLog(String filePath,String fileName) throws IOException {
        addLogCmd=addLogCmd.replace("oldPath",filePath+fileName).replace("picAddr",picAddr).replace("newPath",logPath+fileName);
        Process process = Runtime.getRuntime().exec(addLogCmd);
    }
}
