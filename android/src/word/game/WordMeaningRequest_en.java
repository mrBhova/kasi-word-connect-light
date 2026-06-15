package word.game;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

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
        request.setUrl("https://api.dictionaryapi.dev/api/v2/entries/en/" + word.toLowerCase(Locale.ENGLISH));

        request.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
        request.setHeader("Accept", "application/json");
        request.setTimeOut(10000);

        RequestSender sender = new RequestSender();
        sender.request = request;
        if(Gdx.app.getType() == Application.ApplicationType.Android){
            new Thread(sender).start();
        }else{
            sender.run();
        }
    }


    class RequestSender implements Runnable{

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
                    final String text = t.getMessage();
                    Gdx.app.log("dictionary ------>", "failed:"+text);
                    Gdx.app.postRunnable(new Runnable() {
                        @Override
                        public void run() {
                            WordMeaningRequest_en.this.callback.onMeaning(WordMeaningRequest_en.this.word, text);
                        }
                    });
                }

                @Override
                public void cancelled() {
                    Gdx.app.log("dictionary ------>", "cancelled");
                }
            });
        }


        private String parseResponse(String json){
            try {
                JsonReader reader = new JsonReader();
                JsonValue root = reader.parse(json);
                StringBuilder sb = new StringBuilder();

                // root is a JSON array of entries
                for (JsonValue entry = root.child; entry != null; entry = entry.next) {
                    // Word + phonetic
                    String word = entry.getString("word", "");
                    if (!word.isEmpty()) {
                        sb.append(word.toUpperCase(Locale.ENGLISH));
                        String phonetic = entry.getString("phonetic", "");
                        if (!phonetic.isEmpty()) {
                            sb.append("  ").append(phonetic);
                        }
                        sb.append("\n");
                    }

                    // Meanings array
                    JsonValue meanings = entry.get("meanings");
                    if (meanings != null) {
                        for (JsonValue meaning = meanings.child; meaning != null; meaning = meaning.next) {
                            String partOfSpeech = meaning.getString("partOfSpeech", "");

                            // Definitions array
                            JsonValue definitions = meaning.get("definitions");
                            if (definitions != null) {
                                for (JsonValue def = definitions.child; def != null; def = def.next) {
                                    String definition = def.getString("definition", "");

                                    sb.append("  ● ");
                                    if (!partOfSpeech.isEmpty()) {
                                        sb.append("(").append(partOfSpeech).append(") ");
                                    }
                                    sb.append(definition).append("\n");

                                    // Example
                                    String example = def.getString("example", "");
                                    if (!example.isEmpty()) {
                                        sb.append("    \"").append(example).append("\"\n");
                                    }
                                }
                            }

                            // Synonyms (up to 5)
                            JsonValue synonyms = meaning.get("synonyms");
                            if (synonyms != null && synonyms.child != null) {
                                sb.append("    Synonyms: ");
                                int count = 0;
                                for (JsonValue syn = synonyms.child; syn != null && count < 5; syn = syn.next) {
                                    if (count > 0) sb.append(", ");
                                    sb.append(syn.asString());
                                    count++;
                                }
                                sb.append("\n");
                            }
                        }
                    }
                }

                return sb.length() > 0 ? sb.toString() : "";
            } catch (Exception e) {
                Gdx.app.log("dictionary", "parse error: " + e.getMessage());
                return "";
            }
        }
    }


}
