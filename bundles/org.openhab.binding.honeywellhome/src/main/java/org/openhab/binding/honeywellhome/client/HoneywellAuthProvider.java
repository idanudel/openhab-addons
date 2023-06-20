package org.openhab.binding.honeywellhome.client;

import static org.openhab.binding.honeywellhome.client.HoneywellClientConstants.HONEYWELL_REFRESH_TOKEN_URI;

import java.util.Base64;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.FormContentProvider;
import org.eclipse.jetty.util.Fields;
import org.openhab.binding.honeywellhome.client.api.response.GetTokenResponse;

import com.google.gson.Gson;

public class HoneywellAuthProvider {
    HttpClient httpClient;
    String consumerKey;
    String consumerSecret;
    String token;
    String refreshToken;
    String accessToken;
    String tokenExpiresInSec;
    Gson gson;

    public HoneywellAuthProvider(HttpClient httpClient, String consumerKey, String consumerSecret, String token,
            String refreshToken) {
        this.httpClient = httpClient;
        this.consumerKey = consumerKey;
        this.consumerSecret = consumerSecret;
        this.token = token;
        this.refreshToken = refreshToken;
        this.gson = new Gson();
    }

    public boolean init() throws Exception {
        HoneywellCredentials honeywellCredentials = this.pullHoneywellCredentials();
        return honeywellCredentials.isValid;
    }

    private HoneywellCredentials pullHoneywellCredentials() throws Exception {
        Fields fields = new Fields();
        fields.put("grant_type", "refresh_token");
        fields.put("refresh_token", this.refreshToken);
        String basicAuth = Base64.getEncoder()
                .encodeToString((this.consumerKey + ":" + this.consumerSecret).getBytes());
        ContentResponse contentResponse = this.httpClient.POST(HONEYWELL_REFRESH_TOKEN_URI)
                .content(new FormContentProvider(fields)).header("Authorization", "Basic " + basicAuth).send();
        if (contentResponse.getStatus() == 200 && contentResponse.getContentAsString() != null) {
            String getTokenJsonResponse = contentResponse.getContentAsString();
            GetTokenResponse getTokenResponse = gson.fromJson(getTokenJsonResponse, GetTokenResponse.class);
            this.accessToken = getTokenResponse.getAccessToken();
            this.refreshToken = getTokenResponse.getRefreshToken();
            this.tokenExpiresInSec = getTokenResponse.getExpiresIn();
            // todo Start Scheduler;
            return new HoneywellCredentials(true, this.consumerKey, this.consumerSecret,
                    getTokenResponse.getAccessToken());
        } else {
            return new HoneywellCredentials(false, null, null, null);
        }
    }

    public HoneywellCredentials getHoneywellCredentials() throws Exception {
        if (this.accessToken != null) {
            return new HoneywellCredentials(true, this.consumerKey, this.consumerSecret, this.accessToken);
        }
        return pullHoneywellCredentials();
    }
}
