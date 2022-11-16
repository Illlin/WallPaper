package com.example.wallpaper;

import android.app.WallpaperManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Environment;
import android.util.Base64;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;
import android.content.Context;


public class ApiImageGen extends Thread{

    private String apiKey;
    private String prompt;
    private String negPrompt;
    private Context context;
    public ImageView output;

    public ApiImageGen(String prompt, String negPrompt, String apiKey, Context context){
        setApiKey(apiKey);
        this.context = context;
        this.prompt = prompt;
        this.negPrompt = negPrompt;
    }
    public void setPrompt(String prompt){
        this.prompt = prompt;
    }

    public void setNegPrompt(String negPrompt){
        this.negPrompt = negPrompt;
    }

    public void run(){
        try {
            // Request Generation
            String id = "";
            id = this.requestNewImage();

            while (!checkImage(id)){
                Thread.sleep(5000);
            }

            System.out.println("Woo Hoo! Image generated!");

            this.downloadImage(id);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public JSONObject urlGetJSON(String getUrl) throws JSONException, IOException {
        URL url = new URL(getUrl);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Accept", "application/json");
        con.setRequestProperty("User-Agent", "Wallpaperbot, Made by Illin#1433");

        BufferedReader br = new BufferedReader(
                new InputStreamReader(con.getInputStream(), "utf-8"));
        StringBuilder response = new StringBuilder();
        String responseLine = null;
        while ((responseLine = br.readLine()) != null) {
            response.append(responseLine.trim());
        }
        System.out.println(response.toString());

        JSONObject resp = new JSONObject(response.toString());
        return resp;
    }

    public boolean checkImage(String id) throws IOException, JSONException {
        JSONObject resp = this.urlGetJSON("https://stablehorde.net/api/v2/generate/check/"+id);

        return resp.getInt("finished") == 1;
    }

    public String requestNewImage() throws IOException, JSONException {
        // Request Generation
        String genUrl = "https://stablehorde.net/api/v2/generate/async";

        URL url = new URL(genUrl);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setDoOutput(true);
        con.setDoInput(true);
        con.setRequestProperty("Accept", "application/json");
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("apikey", this.apiKey);
        con.setRequestProperty("User-Agent", "Wallpaperbot, Made by Illin#1433");
        // Construct data
        JSONObject request = new JSONObject();
        JSONObject params = new JSONObject();
        JSONArray models = new JSONArray();
        models.put("stable_diffusion");
        //params.put("height", 1024);
        //params.put("width", 448);
        request.put("prompt", this.prompt + " ### " + this.negPrompt);
        request.put("models", models);
        request.put("nsfw", false);
        request.put("params", params);
        // Send Post Request


        try (DataOutputStream os = new DataOutputStream(con.getOutputStream())) {
            //byte[] input = request.toString().getBytes(StandardCharsets.UTF_8);
            os.writeBytes(request.toString());
            os.flush();
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }


        BufferedReader br = new BufferedReader(
                new InputStreamReader(con.getInputStream(), "utf-8"));
        StringBuilder response = new StringBuilder();
        String responseLine = null;
        while ((responseLine = br.readLine()) != null) {
            response.append(responseLine.trim());
        }
        System.out.println(response.toString());


        int status = con.getResponseCode();
        if (status >= 400 && status <= 499) {
            System.out.println(status);
            InputStream error = con.getErrorStream();
            System.out.println(error);
        }

        con.disconnect();

        System.out.println(response.toString());

        JSONObject resp = new JSONObject(response.toString());

        return resp.getString("id");
    }

    public void downloadImage(String id) throws IOException, JSONException {
        JSONObject resp = this.urlGetJSON("https://stablehorde.net/api/v2/generate/status/"+id);
        String img = resp.getJSONArray("generations").getJSONObject(0).getString("img");
        File bg = saveImage(this.context, img);

        Bitmap image = BitmapFactory.decodeFile(bg.getAbsolutePath());
        WallpaperManager myWallpaperManager = WallpaperManager.getInstance(context);
        myWallpaperManager.setBitmap(image);

    }

    public static File saveImage(final Context context, final String imageData) throws IOException {
        final byte[] imgBytesData = android.util.Base64.decode(imageData,
                android.util.Base64.DEFAULT);

        final File file = File.createTempFile("image", null, context.getCacheDir());
        final FileOutputStream fileOutputStream;
        try {
            fileOutputStream = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }

        final BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(
                fileOutputStream);
        try {
            bufferedOutputStream.write(imgBytesData);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                bufferedOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    public ApiImageGen setApiKey(String apiKey) {
        this.apiKey = apiKey;
        if (apiKey.length() == 0) {
            this.apiKey = "0000000000";
        }
        return this;
    }
}