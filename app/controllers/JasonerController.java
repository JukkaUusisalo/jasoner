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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.UUID;

public class JasonerController extends Controller {

    private static String DIR = Play.application().path() + "/"
            + Play.application().configuration().getString("jasoner.template.path") + "/";

    private String dir(String token) {
        return DIR + token + "/";
    }

    private Result jsonResult(Result httpResponse) {
        response().setContentType("application/json; charset=utf-8");
        return httpResponse;
    }

    @SuppressWarnings("unused")
    public Result getJasoners(String token) {
        ObjectNode result = Json.newObject();
        ArrayNode fileNames = result.putArray("templates");
        try {
            Files.list(Paths.get(dir(token))).forEach(f -> fileNames.add(f.getFileName().toString()));
        } catch (IOException e) {
            return jsonResult(internalServerError(result.put("message",e.getClass().getCanonicalName()+":"+e.getMessage())));
        }
        return jsonResult(ok(result));
    }

    @SuppressWarnings("unused")
    public Result getJasoner(String token,String id) {
        String content;
        try {
            String path = this.getTemplatePath(token,id);
            content = new String(Files.readAllBytes(Paths.get(path)));
        } catch (IOException e) {
            ObjectNode result = Json.newObject();
            return jsonResult(internalServerError(result.put("message",e.getClass().getCanonicalName()+":"+e.getMessage())));
        }
        return jsonResult(ok( Json.parse(content)));
    }

    @SuppressWarnings("unused")
    public Result createToken() {
        String uuid = UUID.randomUUID().toString();
        String dir = DIR + uuid;
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
    public Result createJasoner(String token) {
        String template = request().body().asText();
        ObjectNode result = Json.newObject();
        result.put("message", "Create template");
        result.put("template",template);
        try {
            result.put("template_id",getTemplateId());
            this.writeTemplateFile(token,String.valueOf(getTemplateId()),template);
        } catch (IOException e) {
            return jsonResult(internalServerError(result.put("message",e.getClass().getCanonicalName()+":"+e.getMessage())));
        }
        return jsonResult(ok(result));
    }

    @BodyParser.Of(BodyParser.Text.class)
    public Result updateJasoner(String token,String id) {
        String template = request().body().asText();
        ObjectNode result = Json.newObject();
        result.put("message", "Update template");
        result.put("template",template);
        try {
            result.put("template_id",id);
            this.writeTemplateFile(token,id,template);
        } catch (IOException e) {
            return jsonResult(internalServerError(result.put("message",e.getClass().getCanonicalName()+":"+e.getMessage())));
        }
        return jsonResult(ok(result));
    }

    @SuppressWarnings("unused")
    public Result deleteJasoner(String token,String id) {
        ObjectNode result = Json.newObject();
        result.put("message", "Delete template");
        try {
            result.put("template_id",id);
            this.deleteTemplate(token,id);
        } catch (IOException e) {
            return jsonResult(internalServerError(result.put("message",e.getClass().getCanonicalName()+":"+e.getMessage())));
        }
        return jsonResult(ok(result));

    }


    @BodyParser.Of(BodyParser.Text.class)
    public Result doJasoner(String token,String id) {
        String source = request().body().asText();
        StringWriter w;
        try {
            MustacheFactory mf = new DefaultMustacheFactory();
            Mustache mustache = mf.compile(new FileReader(getTemplatePath(token,id)),"jasoner");
            w = new StringWriter();
            HashMap sourceMap =
                    new ObjectMapper().readValue(source, HashMap.class);
            mustache.execute(w,sourceMap);
        } catch (Exception e) {
            ObjectNode result = Json.newObject();
            return jsonResult(internalServerError(result.put("message",e.getClass().getCanonicalName()+":"+e.getMessage())));
        }
        return jsonResult(ok( Json.parse(w.toString())));
    }

    @BodyParser.Of(BodyParser.Text.class)
    public Result doJasonerF(String token,String id,String ecodedUrl) {
        String source = request().body().asText();
        StringWriter w;
        ObjectNode result;
        try {
            MustacheFactory mf = new DefaultMustacheFactory();
            Mustache mustache = mf.compile(new FileReader(getTemplatePath(token,id)),"jasoner");
            w = new StringWriter();
            HashMap sourceMap =
                    new ObjectMapper().readValue(source, HashMap.class);
            mustache.execute(w,sourceMap);
            result = JasonerHttpPost.sendPost(ecodedUrl,Json.parse(w.toString()));
        } catch (Exception e) {
            ObjectNode error = Json.newObject();
            return jsonResult(internalServerError(error.put("message",e.getClass().getCanonicalName()+":"+e.getMessage())));
        }
        return jsonResult(ok( result));
    }

    private void deleteTemplate(String token,String id) throws IOException {
        if(Files.exists(Paths.get(dir(token)+id))) {
            Files.delete(Paths.get(dir(token)+id));
        }
    }

    private void writeTemplateFile(String token,String id,String template) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(getTemplatePath(token,id)))) {
            writer.write(template);
        }
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(DIR+"index.txt"))) {
            writer.write(String.valueOf(Long.parseLong(id)+1));
        }

    }

    private String getTemplatePath(String token,String id) {
        return dir(token) + id;
    }

    private long getTemplateId() throws IOException {
        if(Files.exists(Paths.get(DIR+"index.txt"))) {
            String indexStr = new String(Files.readAllBytes(Paths.get(DIR+"index.txt")));
            return Long.parseLong(indexStr);
        } else {
            return 1L;
        }

    }
}
