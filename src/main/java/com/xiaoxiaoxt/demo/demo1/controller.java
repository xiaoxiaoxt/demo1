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
    @Value("${file.transformTypePath}")
    String transformTypePath;
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
    @Value("${file.transformType.cmd}")
    String transformTypeCmd;
    boolean mismatching=false;


    @RequestMapping("/")
    public String index(){
        return "index.html";
    }

    @PostMapping("/upload")
    @ResponseBody
    public String upload(@RequestParam("fileName") MultipartFile file,
                         HttpServletRequest request,@RequestParam("type") String type) throws IOException {
        //接受文件绝对地址
        File desfile=null;
        String fileName=null;
        String newfileName=null;
        if (!file.isEmpty()){
            try {
                fileName=file.getOriginalFilename();
                desfile=new File(uploadPath+fileName);
                // 转存文件
                file.transferTo(desfile);
                //判断目标文件是否为视频文件
                if(isVedio(uploadPath+fileName)){
                    if(!fileName.endsWith(".mp4")&&!fileName.endsWith(".mov")&&!fileName.endsWith(".m4a")){
                        mismatching=true;
                    }
                    if(!fileName.endsWith(type)){
                        int index = fileName.lastIndexOf(".");
                        String prefix = fileName.substring(0, index);
                        newfileName=prefix+"."+type;
                    }
                    final String tFileName=fileName;
                    final String tNewfileName=newfileName;
                    final File tDesfile=desfile;
                    new Thread(new Runnable() {
                        @Override
                        public void run(){
                            try {
                                //转换格式
                                transformType(uploadPath,tFileName,type);
                                //删除原格式文件
                                cutVedio(transformTypePath,tNewfileName);
                                cutImg(transformTypePath,tNewfileName);
                                addLog(transformTypePath,tNewfileName);
                                tDesfile.delete();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                    if(mismatching){
                        mismatching=false;
                        return "上传成功</br>文件的后缀名不对，请修改成mp4格式";
                    }
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
        boolean isVedio=false;
        String realIsVedioCmd=isVedioCmd.replace("oldPath",fileFullPath);
        Process process=null;
        Runtime run = Runtime.getRuntime();
        try {
            process = run.exec(realIsVedioCmd);
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
                isVedio=true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(process!=null){
            dealStream(process);
            try {
                process.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return isVedio;
    }

    /**
     *   消费inputstream，并返回
     */
    public static String consumeInputStream(InputStream is) throws IOException{
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String s ;
        StringBuilder sb = new StringBuilder();
        while((s=br.readLine())!=null){
            System.out.println(s);
            sb.append(s);
        }
        return sb.toString();
    }

    //格式转换
    private void transformType(String filePath,String fileName,String type) throws IOException {
        int index = fileName.lastIndexOf(".");
        String prefix = fileName.substring(0, index);
        String newfileName=prefix+"."+type;
        String realTransformTypeCmd=transformTypeCmd.replace("oldPath",filePath+fileName).replace("newPath",transformTypePath+newfileName);
        Process process = Runtime.getRuntime().exec(realTransformTypeCmd);
        dealStream(process);
        try {
            process.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    //截取图片,第2秒钟的图片
    private void cutImg(String filePath,String fileName) throws IOException {
        String realCutImgCmd=cutImgCmd.replace("oldPath",filePath+fileName);
        int index = fileName.lastIndexOf(".");
        fileName = fileName.substring(0, index)+".jpg";
        realCutImgCmd=realCutImgCmd.replace("newPath",imgPath+fileName);
        Process process = Runtime.getRuntime().exec(realCutImgCmd);
        dealStream(process);
        try {
            process.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //截取视频，2秒
    private void cutVedio(String filePath,String fileName) throws IOException {
        String realCutVedioCmd=cutVedioCmd.replace("oldPath",filePath+fileName).replace("newPath",vedioPath+fileName);
        Process process = Runtime.getRuntime().exec(realCutVedioCmd);
        InputStream in = process.getInputStream();
        dealStream(process);
        try {
            process.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //加log
    private void addLog(String filePath,String fileName) throws IOException {
        String realAddLogCmd=addLogCmd.replace("oldPath",filePath+fileName).replace("picAddr",picAddr).replace("newPath",logPath+fileName);
        Process process = Runtime.getRuntime().exec(realAddLogCmd);
        dealStream(process);
        try {
            process.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 处理process输出流和错误流，防止进程阻塞
     * 在process.waitFor();前调用
     * @param process
     */
    private static void dealStream(Process process) {
        if (process == null) {
            return;
        }
        // 处理InputStream的线程
        new Thread() {
            @Override
            public void run() {
                BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line = null;
                try {
                    while ((line = in.readLine()) != null) {
                        //logger.info("output: " + line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
        // 处理ErrorStream的线程
        new Thread() {
            @Override
            public void run() {
                BufferedReader err = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                String line = null;
                try {
                    while ((line = err.readLine()) != null) {
                        //logger.info("err: " + line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        err.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }
}
