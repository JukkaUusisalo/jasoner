package controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
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
import java.util.UUID;

public class JasonerController extends Controller {

    private Result jsonResult(Result httpResponse) {
        response().setContentType("application/json; charset=utf-8");
        return httpResponse;
    }

    public Result getJasoners() {
        String dir = Play.application().path() + "/"
                + Play.application().configuration().getString("jasoner.template.path") + "/";
        ObjectNode result = Json.newObject();
        ArrayNode fileNames = result.putArray("templates");
        try {
            Files.list(Paths.get(dir)).forEach(f -> {
                if(!f.getFileName().toString().equals("index.txt"))
                    fileNames.add(f.getFileName().toString());
            });
        } catch (IOException e) {
            return jsonResult(internalServerError(result.put("message",e.getClass().getCanonicalName()+":"+e.getMessage())));
        }
        return jsonResult(ok(result));
    }

    public Result getJasoner(String id) {
        String content;
        try {
            String path = this.getTemplatePath(id);
            content = new String(Files.readAllBytes(Paths.get(path)));
        } catch (IOException e) {
            ObjectNode result = Json.newObject();
            return jsonResult(internalServerError(result.put("message",e.getClass().getCanonicalName()+":"+e.getMessage())));
        }
        return jsonResult(ok( Json.parse(content)));
    }

    public Result createToken() {
        String uuid = UUID.randomUUID().toString();
        String dir = Play.application().path() + "/"
                + Play.application().configuration().getString("jasoner.template.path") + "/" + uuid;
        ObjectNode result = Json.newObject();
        try {
            Files.createDirectories(Paths.get(dir));
            result.put("token", uuid);
        } catch (IOException e) {
            return jsonResult(internalServerError(result.put("message",e.getClass().getCanonicalName()+":"+e.getMessage())));
        }
        return jsonResult(ok(result));
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
    public Result updateJasoner(String id) {
        String template = request().body().asText();
        ObjectNode result = Json.newObject();
        result.put("message", "Update template");
        result.put("template",template);
        try {
            result.put("template_id",id);
            this.writeTemplateFile(id,template);
        } catch (IOException e) {
            return jsonResult(internalServerError(result.put("message",e.getClass().getCanonicalName()+":"+e.getMessage())));
        }
        return jsonResult(ok(result));
    }

    public Result deleteJasoner(String id) {
        ObjectNode result = Json.newObject();
        result.put("message", "Delete template");
        try {
            result.put("template_id",id);
            this.deleteTemplate(id);
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

    @BodyParser.Of(BodyParser.Text.class)
    public Result doJasonerF(String id,String ecodedUrl) {
        String source = request().body().asText();
        StringWriter w = null;
        ObjectNode result = null;
        try {
            MustacheFactory mf = new DefaultMustacheFactory();
            Mustache mustache = mf.compile(new FileReader(getTemplatePath(id)),"jasoner");
            w = new StringWriter();
            HashMap<String,Object> sourceMap =
                    new ObjectMapper().readValue(source, HashMap.class);
            mustache.execute(w,sourceMap);
            result = JasonerHttpPost.sendPost(ecodedUrl,Json.parse(w.toString()));
        } catch (Exception e) {
            ObjectNode error = Json.newObject();
            return jsonResult(internalServerError(error.put("message",e.getClass().getCanonicalName()+":"+e.getMessage())));
        }
        return jsonResult(ok( result));
    }

    private void deleteTemplate(String id) throws IOException {
        String dir = Play.application().path() + "/"
                + Play.application().configuration().getString("jasoner.template.path") + "/";
        if(Files.exists(Paths.get(dir+id))) {
            Files.delete(Paths.get(dir+id));
        }
    }

    private void writeTemplateFile(String id,String template) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(getTemplatePath(id)))) {
            writer.write(template);
        }
        String dir = Play.application().path() + "/"
                + Play.application().configuration().getString("jasoner.template.path") + "/";

        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(dir+"index.txt"))) {
            writer.write(String.valueOf(Long.parseLong(id)+1));
        }

    }


    private String getTemplatePath(String id) throws UnsupportedEncodingException{
        return Play.application().path() + "/"
                + Play.application().configuration().getString("jasoner.template.path") + "/"
                + URLEncoder.encode(id,"UTF-8");
    }

    private long getTemplateId() throws IOException {
        String dir = Play.application().path() + "/"
                + Play.application().configuration().getString("jasoner.template.path") + "/";
        if(Files.exists(Paths.get(dir+"index.txt"))) {
            String indexStr = new String(Files.readAllBytes(Paths.get(dir+"index.txt")));
            return Long.parseLong(indexStr);
        } else {
            return 1L;
        }

    }
}
