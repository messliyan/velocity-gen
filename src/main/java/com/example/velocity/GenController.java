package com.example.velocity;

import com.example.velocity.constant.Constants;
import com.example.velocity.domain.GenTable;
import com.example.velocity.util.VelocityInitializer;
import com.example.velocity.util.VelocityUtils;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 代码生成 操作处理
 *
 * @author ruoyi
 */
@Controller
@RequestMapping("/tool/gen")
public class GenController  {

  private String prefix = "tool/gen";


  @GetMapping("/download")
  public void download(HttpServletResponse response) throws IOException
  {
    byte[] data = downloadCode();
    genCode(response, data);
  }


  /**
   * 查询代码生成列表
   */
  @GetMapping("/start")
  @ResponseBody
  public String start() {

    return "good";
  }


  @GetMapping("/gen")
  @ResponseBody
  public Map<String, String> previewCode()
  {
    Map<String, String> dataMap = new LinkedHashMap<>();
   // subTableName='null', subTableFkName='null',
    // isRequired='null',  isEdit='null', isList='null', isQuery='null',

    // 查询表信息
    GenTable table = GenTable.getIntent();

     //subTable=null,
 //options='null', treeCode='null', treeParentCode='null', treeName='null', parentMenuId='null', parentMenuName='null'}

    VelocityInitializer.initVelocity();

    VelocityContext context = VelocityUtils.prepareContext(table);

    // 获取模板列表
    List<String> templates = VelocityUtils.getTemplateList(table.getTplCategory());
    for (String template : templates)
    {
      // 渲染模板
      StringWriter sw = new StringWriter();
      Template tpl = Velocity.getTemplate(template, Constants.UTF8);
      tpl.merge(context, sw);
      dataMap.put(template, sw.toString());
    }
    return dataMap;
  }


  public byte[] downloadCode()
  {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    ZipOutputStream zip = new ZipOutputStream(outputStream);
    generatorCode(zip);
    IOUtils.closeQuietly(zip);
    return outputStream.toByteArray();
  }

  /**
   * 生成zip文件
   */
  private void genCode(HttpServletResponse response, byte[] data) throws IOException
  {
    response.reset();
    response.setHeader("Content-Disposition", "attachment; filename=\"ruoyi.zip\"");
    response.addHeader("Content-Length", "" + data.length);
    response.setContentType("application/octet-stream; charset=UTF-8");
    IOUtils.write(data, response.getOutputStream());
  }

  /**
   * 查询表信息并生成代码
   */
  private void generatorCode(ZipOutputStream zip)
  {
    // 查询表信息
    GenTable table = GenTable.getIntent();

    VelocityInitializer.initVelocity();

    VelocityContext context = VelocityUtils.prepareContext(table);

    // 获取模板列表
    List<String> templates = VelocityUtils.getTemplateList(table.getTplCategory());
    for (String template : templates)
    {
      // 渲染模板
      StringWriter sw = new StringWriter();
      Template tpl = Velocity.getTemplate(template, Constants.UTF8);
      tpl.merge(context, sw);
      try
      {
        // 添加到zip
        zip.putNextEntry(new ZipEntry(VelocityUtils.getFileName(template, table)));
        IOUtils.write(sw.toString(), zip, Constants.UTF8);
        IOUtils.closeQuietly(sw);
        zip.flush();
        zip.closeEntry();
      }
      catch (IOException e)
      {
        System.out.println("渲染模板失败，表名：" + table.getTableName());
      }
    }
  }
}