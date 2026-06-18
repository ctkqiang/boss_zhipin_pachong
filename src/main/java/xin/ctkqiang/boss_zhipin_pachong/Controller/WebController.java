package xin.ctkqiang.boss_zhipin_pachong.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class WebController {

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/analysis")
    public String about() {
        return "analysis";
    }

    @GetMapping("/favicon.ico")
    @ResponseBody
    public void favicon() {}
}