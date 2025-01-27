package poc.pt.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import org.springframework.ai.client.AiClient;
import org.springframework.ai.client.AiResponse;
import org.springframework.ai.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import poc.pt.model.GeneratedImage;

@Service 
public class AIService {
	
	private static String prompt_placeholder = 	"WRITE A MESMERIZING POEM ON %s";
	private static String JOKE = "CAN YOU PLEASE CRAFT A JOKE FOR ME? YOU CAN CAFT ON {topic}, BUT IT SHOULD INCLUDE A OLD MEN";
	private static String FIND_A_MOVIE = "HELLO AI CAN YOU PLEASE HELP ME IN FINDING A MOVIE OF BOLLYWOOD FOR THIS SATURDAY NIGHT? AS YOU KNOW I AM MOVIE LOVER BUT I LIKE ONLY {category} MOVIES GENERALLY DO NOT MIND OLD MOVIES BUT PREFERT TO SEE MOVIES OF YEAR {year}. BETTER IF YOU CAN GIVE ME SUMMERY ALSO ALONG WITH ITS NAME. PLEASE PROVIDE ME THESE DETAILS IN JSON FORMAT LIKE THIS JSON format: category, year, movieName, director, review, smallSummary. ";	
	private static String CREATE_A_SATISFYING_IMAGE_2 = "I AM REALLY BOARD FROM ONLINE MEMES. CAN YOU CREATE ME A PROMPT ABOUT {topic}. ELEVATE THE GIVEN TOPIC. MAKE IT CLASSY. MAKE A RESOLUTION OF 256X256, BUT ENSURE THAT IT IS PRESENTED IN JSON IT NEED TO BE STRING. I DESIRE ONLY ONE CREATION. GIVE ME AS JSON FORMAT: prompt, n, size.";
	
	@Autowired
	AiClient aiClient;
	
	@Value("${spring.ai.openai.imageUrl}")
	private String openAIImageUrl;
	
	@Value("${spring.ai.openai.api-key}")
	private String aiApiKey;
	
	public String createMeme(String subject) {
		
		String prompt = String.format(prompt_placeholder, subject);
		return aiClient.generate(prompt);
	}
	
	
    public String getJoke(String topic){
    	
        PromptTemplate promptTemplate = new PromptTemplate(JOKE);
        promptTemplate.add("topic", topic);
        return this.aiClient.generate(promptTemplate.create()).getGeneration().getText();
    }
    
    
    public InputStreamResource getImage(@RequestParam(name = "topic") String topic) throws URISyntaxException {
        PromptTemplate promptTemplate = new PromptTemplate(CREATE_A_SATISFYING_IMAGE_2);
        promptTemplate.add("topic", topic);
        String imagePrompt = this.aiClient.generate(promptTemplate.create()).getGeneration().getText();

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + aiApiKey);
        headers.add("Content-Type", "application/json");
        HttpEntity<String> httpEntity = new HttpEntity<>(imagePrompt,headers);

        String imageUrl = restTemplate.exchange(openAIImageUrl, HttpMethod.POST, httpEntity, GeneratedImage.class)
                .getBody().getData().get(0).getUrl();
        byte[] imageBytes = restTemplate.getForObject(new URI(imageUrl), byte[].class);
        assert imageBytes != null;
        return new InputStreamResource(new java.io.ByteArrayInputStream(imageBytes));
    }
    
    public String findANovel(String category, String year) {
        PromptTemplate promptTemplate = new PromptTemplate(FIND_A_MOVIE);
        Map.of("category", category, "year", year).forEach(promptTemplate::add);
        AiResponse generate = this.aiClient.generate(promptTemplate.create());
        return generate.getGeneration().getText();
    }
	
}
