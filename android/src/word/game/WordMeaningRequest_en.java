package word.game;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.utils.StringBuilder;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Locale;

import word.game.managers.LanguageManager;
import word.game.net.WordMeaningRequest;
import word.game.ui.dialogs.DictionaryDialog;

public class WordMeaningRequest_en implements WordMeaningRequest {

    private DictionaryDialog.DictionaryCallback callback;
    private String word;

    @Override
    public void request(String word, DictionaryDialog.DictionaryCallback callback) {
        this.word = word;
        this.callback = callback;

        Net.HttpRequest request = new Net.HttpRequest(Net.HttpMethods.GET);
        request.setUrl("https://api.dictionaryapi.dev/api/v2/entries/en/" + word);
        request.setHeader("User-Agent", "WordConnect/1.0");
        request.setTimeOut(10000);

        RequestSender sender = new RequestSender();
        sender.request = request;
        if(Gdx.app.getType() == Application.ApplicationType.Android){
            new Thread(sender).start();
        }else{
            sender.run();
        }
    }


    class RequestSender implements Runnable {

        Net.HttpRequest request;

        @Override
        public void run() {
            Gdx.net.sendHttpRequest(request, new Net.HttpResponseListener() {
                @Override
                public void handleHttpResponse(final Net.HttpResponse httpResponse) {

                    String response = parseResponse(httpResponse.getResultAsString());
                    if(response.isEmpty()) response = LanguageManager.get("no_response");
                    final String responseToSend = response;

                    Gdx.app.postRunnable(new Runnable() {
                        @Override
                        public void run() {
                            WordMeaningRequest_en.this.callback.onMeaning(WordMeaningRequest_en.this.word, responseToSend);
                        }
                    });
                }

                @Override
                public void failed(Throwable t) {
                    Gdx.app.log("dictionary", "failed:" + t.getMessage());
                    Gdx.app.postRunnable(new Runnable() {
                        @Override
                        public void run() {
                            WordMeaningRequest_en.this.callback.onMeaning(WordMeaningRequest_en.this.word, LanguageManager.get("no_response"));
                        }
                    });
                }

                @Override
                public void cancelled() {
                    Gdx.app.log("dictionary", "cancelled");
                }
            });
        }


        private String parseResponse(String json){
            try {
                JSONArray entries = new JSONArray(json);
                StringBuilder sb = new StringBuilder();

                for(int i = 0; i < entries.length(); i++){
                    JSONObject entry = entries.getJSONObject(i);
                    JSONArray meanings = entry.getJSONArray("meanings");

                    for(int m = 0; m < meanings.length(); m++){
                        JSONObject meaning = meanings.getJSONObject(m);
                        String partOfSpeech = meaning.getString("partOfSpeech");

                        sb.append(partOfSpeech.toUpperCase(Locale.ENGLISH));
                        sb.append("\n");

                        JSONArray definitions = meaning.getJSONArray("definitions");
                        for(int d = 0; d < definitions.length(); d++){
                            JSONObject def = definitions.getJSONObject(d);
                            String definition = def.getString("definition");
                            sb.append("* ");
                            sb.append(definition);
                            sb.append("\n");

                            if(def.has("example") && !def.isNull("example")){
                                String example = def.getString("example");
                                sb.append("  \"");
                                sb.append(example);
                                sb.append("\"\n");
                            }
                        }
                        sb.append("\n");
                    }
                }
                return sb.toString();
            } catch (Exception e) {
                Gdx.app.log("dictionary", "parse error: " + e.getMessage());
                return "";
            }
        }
    }

}
