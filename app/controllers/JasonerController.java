package controllers;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import play.Play;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;

import java.io.*;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

/**
 * Created by juuus on 11/12/16.
 */
public class JasonerController extends Controller {

    public Result jsonResult(Result httpResponse) {
        response().setContentType("application/json; charset=utf-8");
        return httpResponse;
    }

    @BodyParser.Of(BodyParser.Text.class)
    public Result createJasoner() {
        String template = request().body().asText();
        ObjectNode result = Json.newObject();
        result.put("message", "Create template");
        result.put("template",template);
        try {
            result.put("template_id",getTemplateId());
            this.writeTemplateFile(String.valueOf(getTemplateId()),template);
        } catch (IOException e) {
            return jsonResult(internalServerError(result.put("message",e.getClass().getCanonicalName()+":"+e.getMessage())));
        }
        return jsonResult(ok(result));
    }

    @BodyParser.Of(BodyParser.Text.class)
    public Result doJasoner(String id) {
        String source = request().body().asText();
        StringWriter w = null;
        try {
            MustacheFactory mf = new DefaultMustacheFactory();
            Mustache mustache = mf.compile(new FileReader(getTemplatePath(id)),"jasoner");
            w = new StringWriter();
            HashMap<String,Object> sourceMap =
                    new ObjectMapper().readValue(source, HashMap.class);
            mustache.execute(w,sourceMap);
        } catch (Exception e) {
            ObjectNode result = Json.newObject();
            return jsonResult(internalServerError(result.put("message",e.getClass().getCanonicalName()+":"+e.getMessage())));
        }
        return jsonResult(ok( Json.parse(w.toString())));
    }

    private void writeTemplateFile(String url,String template) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(getTemplatePath(url)))) {
            writer.write(template);
        }

    }

    private String getTemplatePath(String url) throws UnsupportedEncodingException{
        return Play.application().path() + "/"
                + Play.application().configuration().getString("jasoner.template.path") + "/"
                + URLEncoder.encode(url,"UTF-8");
    }

    private long getTemplateId() throws IOException {
        String dir = Play.application().path() + "/"
                + Play.application().configuration().getString("jasoner.template.path") + "/";
        return Files.list(Paths.get(dir)).count()+1;
    }
}
