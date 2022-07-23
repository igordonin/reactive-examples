package guru.springframework.reactivebeerclient.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import static io.netty.handler.logging.LogLevel.DEBUG;
import static reactor.netty.transport.logging.AdvancedByteBufFormat.TEXTUAL;

@Configuration
public class WebClientConfig {

  @Bean
  public WebClient webClient() {
    return WebClient.builder()
        .baseUrl(WebClientProperties.BASE_URL)
        .clientConnector(this.createConnector())
        .build();
  }

  private ReactorClientHttpConnector createConnector() {
    final String clientClass = "reactor.netty.client.HttpClient";
    return new ReactorClientHttpConnector(HttpClient.create().wiretap(clientClass, DEBUG, TEXTUAL));
  }
}
