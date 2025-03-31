package com.browserstack;

import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.Test;

public class BStackDemoTest extends SeleniumTest {
	// Method to download the image
    private static void downloadImage(String imageUrl, String targetPath) {
        try (InputStream in = new URL(imageUrl).openStream();
             BufferedInputStream bis = new BufferedInputStream(in);
             FileOutputStream fos = new FileOutputStream(targetPath)) {

            byte[] buffer = new byte[1024];
            int bytesRead;

            //Read and write the image data
            while ((bytesRead = bis.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }

            System.out.println("Imagen descargada: " + targetPath);
        } catch (IOException e) {
            System.out.println("Error al descargar la imagen: " + e.getMessage());
        }
    }
    
 // Method to translate text using Google Translate API
    private static String translateText(String text, String sourceLanguage, String targetLanguage) {
        Translate translate = TranslateOptions.getDefaultInstance().getService();
        Translation translation = translate.translate(
                text,
                Translate.TranslateOption.sourceLanguage(sourceLanguage),
                Translate.TranslateOption.targetLanguage(targetLanguage)
        );
        return translation.getTranslatedText();
    }
    @Test
    public void getTitleAndContentOfArticles() throws Exception {
    	driver.get("https://elpais.com/");
    	driver.manage().timeouts().implicitlyWait(10,TimeUnit.SECONDS);
    	driver.findElement(By.id("didomi-notice-agree-button")).click();
    	
    	List<WebElement> articles = driver.findElements(By.xpath("//section[header//a[contains(text(),'Opinión')]]/div/article"));
		
		HashMap<String, Integer> countHeaders = new HashMap<>();
		
		for (int i = 0; i < 5 && i < articles.size(); i++) {
			
				WebElement article = articles.get(i);
	            
	            //Fetch the title
	            WebElement title = article.findElement(By.tagName("h2"));
	            String titleText = title.getText();
	            
	            //Print the title of the article
	            System.out.println("Article " + (i + 1) + " Title in Spanish: " + titleText);
	            
	            boolean isSpanishTitle = titleText.matches(".*[áéíóúñ].*");
	            
	            Assert.assertTrue(isSpanishTitle, "Text is not in Spanish.");
	            
                String translatedTitle = translateText(titleText, "es", "en");
                
                boolean isEnglish = translatedTitle.matches(".*[a-zA-Z].*");

                Assert.assertTrue(isEnglish, "Text is not in English.");
                
                System.out.println("Article " + (i + 1) + " Title in English: " + translatedTitle);
	            
                String[] words = translatedTitle.split("\\s+");
                
                for (String word : words) {
                    // Convert the word to lower case for case-insensitive counting
                    word = word.toLowerCase();
                    
                    // Update the word count in the HashMap
                    countHeaders.put(word, countHeaders.getOrDefault(word, 0) + 1);
                }
                
	            try {
            	//Fetch the Content
	            WebElement content = article.findElement(By.tagName("p"));
	            String contentText = content.getText();
	            
	            boolean isSpanishContent = contentText.matches(".*[áéíóúñ].*");
	            
	            Assert.assertTrue(isSpanishContent);
	            
	            //Print the Content of the article
	            System.out.println("Article " + (i + 1) + " Content in Spanish: " + contentText);
	            System.out.println("------------------------------------------------");
	            }catch(Exception e) {
	            	System.out.println("Error" + e.getMessage());
	            	
	            	System.out.println("Article " + (i + 1) + " image is saved!");
	            	
	    			//Check if there's a cover image and download it if available
	                WebElement imgElement = article.findElement(By.tagName("figure")).findElement(By.tagName("a")).findElement(By.tagName("img"));
	                String imageUrl = imgElement.getAttribute("src");
	                
	                boolean imageSaved = false;
	                if (imageUrl != null && !imageUrl.isEmpty()) {
	                	
	                    //Download and save the cover image of the article to the local machine
	                    downloadImage(imageUrl, "Downloads\\image_"+i+1+".jpg");
	                    imageSaved = true;
	                }
	                Assert.assertTrue(imageSaved);
	    		}
	            System.out.println("From the translated headers, words that are repeated more than twice across all headers combined:");
	            
	            boolean repeatedWordsFound = false;
	            
	            for (Map.Entry<String, Integer> entry : countHeaders.entrySet()) {
	                if (entry.getValue() > 2) {  
	                    System.out.println(entry.getKey() + ": " + entry.getValue());
	                    repeatedWordsFound = true;
	                }
	            }
	            Assert.assertTrue(repeatedWordsFound, "No repeated Words Found.");
		}
    }
}
