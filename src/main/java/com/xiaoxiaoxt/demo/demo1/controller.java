package com.xiaoxiaoxt.demo.demo1;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class controller {

    @GetMapping("/cut")
    public String getJSON(@RequestParam("start") String start,
                          @RequestParam("time") String time)
            throws IOException, InterruptedException {
        // cmd 为待执行的命令行
        String cmd2 = "ffmpeg -ss start -t time -i C:\\Users\\xiaoxiaoxt\\Desktop\\6.1.mp4 C:\\Users\\xiaoxiaoxt\\Desktop\\6.10.mp4";
        cmd2=cmd2.replace("start",start).replace("time",time);
        Process process = Runtime.getRuntime().exec(cmd2);
        Thread.sleep(5000);
        return "aaaa";
    }
}
