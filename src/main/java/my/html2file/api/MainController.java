package my.html2file.api;

import my.html2file.api.entity.MyAjaxPost;
import my.html2file.api.entity.MyAjaxResult;
import my.html2file.html2excel.service.Html2ExcelService;
import my.html2file.html2html.service.Html2HtmlService;
import my.html2file.html2image.service.Html2ImageService;
import my.html2file.html2markdown.service.Html2MarkdownService;
import my.html2file.html2pdf.service.Html2PdfService;
import my.html2file.html2word.service.Html2WordService;
import my.html2file.utils.BaseUtils;
import my.html2file.utils.PathUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Controller
public class MainController {
    @Autowired
    private Html2ImageService html2ImageService;
    @Autowired
    private Html2PdfService html2PdfService;
    @Autowired
    private Html2MarkdownService html2MarkdownService;
    @Autowired
    private Html2ExcelService html2ExcelService;
    @Autowired
    private Html2HtmlService html2HtmlService;
    @Autowired
    private Html2WordService html2WordService;
    @Value("${server.port}")
    private String serverPort;

    /**
     * html页面转图片
     *
     * @param pageUrl
     * @param fileExt
     * @return
     */
    @RequestMapping("/html2image")
    public String html2image(HttpServletRequest request, @RequestParam(name = "pageUrl") String pageUrl, @RequestParam(name = "fileExt", defaultValue = "") String fileExt) {
        try {
            pageUrl = fullParameter(request, pageUrl);
            String fileRelativePath = html2ImageService.excute(pageUrl, fileExt);
            return "redirect:" + fileRelativePath;
        } catch (Exception e) {
            e.printStackTrace();
            return "/error";
        }
    }

    /**
     * html页面转PDF
     *
     * @param pageUrl
     * @return
     */
    @RequestMapping("/html2pdf")
    public String html2pdf(HttpServletRequest request, @RequestParam(name = "pageUrl") String pageUrl) {
        try {
            pageUrl = fullParameter(request, pageUrl);
            String fileRelativePath = html2PdfService.excute(pageUrl);
            return "redirect:" + fileRelativePath;
        } catch (Exception e) {
            e.printStackTrace();
            return "/error";
        }
    }

    /**
     * html页面转markdown
     *
     * @param pageUrl
     * @return
     */
    @RequestMapping("/html2markdown")
    public String html2markdown(HttpServletRequest request, @RequestParam(name = "pageUrl") String pageUrl) {
        try {
            pageUrl = fullParameter(request, pageUrl);
            String fileRelativePath = html2MarkdownService.excute(pageUrl);
            return "redirect:" + fileRelativePath;
        } catch (Exception e) {
            e.printStackTrace();
            return "/error";
        }
    }

    /**
     * html页面转excel
     *
     * @param pageUrl
     * @return
     */
    @RequestMapping("/html2excel")
    public String html2excel(HttpServletRequest request, @RequestParam(name = "pageUrl") String pageUrl) {
        try {
            pageUrl = fullParameter(request, pageUrl);
            String fileRelativePath = html2ExcelService.excute(pageUrl);
            return "redirect:" + fileRelativePath;
        } catch (Exception e) {
            e.printStackTrace();
            return "/error";
        }
    }

    /**
     * html页面转word
     *
     * @param pageUrl
     * @return
     */
    @RequestMapping("/html2word")
    public String html2word(HttpServletRequest request, @RequestParam(name = "pageUrl") String pageUrl) {
        try {
            pageUrl = fullParameter(request, pageUrl);
            String fileRelativePath = html2WordService.excute(pageUrl);
            return "redirect:" + fileRelativePath;
        } catch (Exception e) {
            e.printStackTrace();
            return "/error";
        }
    }

    /**
     * html页面转文档
     *
     * @param myAjaxPost
     * @return
     */
    @RequestMapping("/html2file")
    public @ResponseBody
    MyAjaxResult html2file(HttpServletRequest request, @RequestBody MyAjaxPost myAjaxPost) {
        MyAjaxResult result = new MyAjaxResult();
        try {
            if (BaseUtils.isBlank(myAjaxPost.getPageUrl()) && !BaseUtils.isBlank(myAjaxPost.getPageHtmlContent())) {
                //如果只传入了html内容，未传入页面链接（pageUrl），将HTML内容保存为本应用的HTML文档，并且获得http链接地址赋给
                String tempHtmlPath = html2HtmlService.excute(myAjaxPost.getPageHtmlContent());
                String newPageUrl = PathUtils.getPathBaseClass(tempHtmlPath);
                myAjaxPost.setPageUrl(newPageUrl);
            }
            String fileRelativePath = null;
            if (MyAjaxPost.TO_IMG.equals(myAjaxPost.getFileType())) {
                fileRelativePath = html2ImageService.excute(myAjaxPost.getPageUrl(), myAjaxPost.getFileExt());
            } else if (MyAjaxPost.TO_PDF.equals(myAjaxPost.getFileType())) {
                fileRelativePath = html2PdfService.excute(myAjaxPost.getPageUrl());
            } else if (MyAjaxPost.TO_MD.equals(myAjaxPost.getFileType())) {
                fileRelativePath = html2MarkdownService.excute(myAjaxPost.getPageUrl());
            } else if (MyAjaxPost.TO_EXCEL.equals(myAjaxPost.getFileType())) {
                fileRelativePath = html2ExcelService.excute(myAjaxPost.getPageUrl());
            } else if (MyAjaxPost.TO_WORD.equals(myAjaxPost.getFileType())) {
                fileRelativePath = html2WordService.excute(myAjaxPost.getPageUrl());
            } else {
                result.setStatus(MyAjaxResult.FAIL);
                fileRelativePath = "暂时不支持该类型文档转化！";
            }
            result.setResult(fileRelativePath);
        } catch (Exception e) {
            e.printStackTrace();
            result.setStatus(MyAjaxResult.FAIL);
            result.setErrorMsg("解析失败！{" + e.getMessage() + "}");
        }
        return result;
    }

    /**
     * 拼接pageUrl后面的参数，防止pageUrl的参数丢失
     *
     * @param request
     * @param pageUrl
     * @return
     */
    private String fullParameter(HttpServletRequest request, String pageUrl) {
        Map<String, String[]> parameterMap = request.getParameterMap();
        for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
            String key = entry.getKey();
            String[] values = entry.getValue();
            if (!"pageUrl".equals(key) && !BaseUtils.isBlank(key) && values.length > 0) {
                for (String value : values) {
                    if(!BaseUtils.isBlank(value)){
                        if (pageUrl.contains("?")) {
                            pageUrl += "&" + key + "=" + value;
                        } else {
                            pageUrl += "?" + key + "=" + value;
                        }
                    }
                }
            }
        }
        return pageUrl;
    }
}
