

// -----( IS Java Code Template v1.2

import com.wm.data.*;
import com.wm.util.Values;
import com.wm.app.b2b.server.Service;
import com.wm.app.b2b.server.ServiceException;
// --- <<IS-START-IMPORTS>> ---
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
// --- <<IS-END-IMPORTS>> ---

public final class logic

{
	// ---( internal utility methods )---

	final static logic _instance = new logic();

	static logic _newInstance() { return new logic(); }

	static logic _cast(Object o) { return (logic)o; }

	// ---( server methods )---




	public static final void indoToUnang (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(indoToUnang)>> ---
		// @sigtype java 3.5
		// [i] field:0:required indo_text
		// [o] field:0:required unang_text
		// get input
		IDataCursor pipelineCursor = pipeline.getCursor();
		String	indoText = IDataUtil.getString( pipelineCursor, "indo_text" );
		
		// split words
		List<String> tokens = separateWords(indoText);
		
		// Translate words 
		StringBuilder translatedBuilder = new StringBuilder();
		for (String token : tokens) {
		    if (token.matches("(?i)\\b\\w*[aiueo]\\w*\\b")) {
		    	List<String> outliers = Arrays.asList("tanya", "hanya", "punya", "nyonya");
		    	
		    	String lowerToken = token.toLowerCase();
		    	String translated;
		
		    	if (lowerToken.endsWith("nya") && !outliers.stream()
		    		    .anyMatch(lowerToken::endsWith)) {
		    	    // Remove "nya", translate, then re-append "nya"
		    	    String base = lowerToken.substring(0, lowerToken.length() - 3);
		    	    translated = toUnang(base) + "nya";
		    	} else {
		    	    translated = toUnang(lowerToken);
		    	}
		
		        // Preserve original casing
		        if (Character.isUpperCase(token.charAt(0))) {
		            translated = Character.toUpperCase(translated.charAt(0)) + translated.substring(1);
		        }
		
		        translatedBuilder.append(translated);
		    } else {
		        translatedBuilder.append(token); // preserve punctuation or whitespace
		    }
		}
		
		// Store result
		String translated = translatedBuilder.toString().trim();
		IDataUtil.put(pipelineCursor, "unang_text", translated);
		pipelineCursor.destroy();
		// --- <<IS-END>> ---

                
	}



	public static final void unangToIndo (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(unangToIndo)>> ---
		// @sigtype java 3.5
		// [i] field:0:required indo_text
		// [o] field:0:required output_text
		// pipeline
		IDataCursor pipelineCursor = pipeline.getCursor();
		String	indoText = IDataUtil.getString( pipelineCursor, "indo_text" );
		pipelineCursor.destroy();
		
		List<String> tokens = new ArrayList<>();
		Matcher matcher = Pattern.compile("(?i)u\\w* \\w*g|\\p{Punct}|\\s+").matcher(indoText);
		while (matcher.find()) {
		    tokens.add(matcher.group());
		}
		
		StringBuilder outputBuilder = new StringBuilder();
		
		for (String token : tokens) {
			if (token.matches("(?i)u\\w* \\w*gnya")){
				Matcher unangMatcher = Pattern.compile("(?i)u(\\w*) (\\w*)n([aiueo])ng(nya)").matcher(token);
				
				String x_changed = unangMatcher.group(1); // x part (with 'a' vowels)
		        String b = unangMatcher.group(2);   // b part
		        String c = unangMatcher.group(3);    // c part
		        String nya = unangMatcher.group(4);  //-nya ending
		
		        // Replace the last 'a' in the modified part with the original vowel
		        StringBuilder originalRoot = new StringBuilder(x_changed);
		        for (int i = originalRoot.length() - 1; i >= 0; i--) {
		            if (originalRoot.charAt(i) == 'a') {
		                originalRoot.setCharAt(i, c.charAt(0));
		                break;
		                }
		        }
		
		        String originalWord = b + originalRoot.toString() + nya;
		        
		        if (Character.isUpperCase(token.charAt(0))) {
		            originalWord = Character.toUpperCase(originalWord.charAt(0)) + originalWord.substring(1);
		        }
		        
		        outputBuilder.append(originalWord);
				
			} else if (token.matches("(?i)u\\w* \\w*g")) {
				Matcher unangMatcher = Pattern.compile("(?i)u(\\w*) (\\w*)n([aiueo])ng").matcher(token);
				if (unangMatcher.matches()) {
		            String x_changed = unangMatcher.group(1); // x part (with 'a' vowels)
		            String b = unangMatcher.group(2);   // b part
		            String c = unangMatcher.group(3);    // c part
		
		            // Replace the last 'a' in the modified part with the original vowel
		            StringBuilder originalRoot = new StringBuilder(x_changed);
		            for (int i = originalRoot.length() - 1; i >= 0; i--) {
		                if (originalRoot.charAt(i) == 'a') {
		                    originalRoot.setCharAt(i, c.charAt(0));
		                    break;
			                }
		            }
		
		            String originalWord = b + originalRoot.toString();
		            
		            if (Character.isUpperCase(token.charAt(0))) {
		                originalWord = Character.toUpperCase(originalWord.charAt(0)) + originalWord.substring(1);
		            }
		            
		            outputBuilder.append(originalWord);
		        } else {
		            // If pattern doesn't match exactly, append as-is
		            outputBuilder.append(token);
		        }
			} else {
		        // Preserve punctuation or spaces
		        outputBuilder.append(token);
		    }
		}
		
		// pipeline
		IDataCursor pipelineCursor_1 = pipeline.getCursor();
		IDataUtil.put( pipelineCursor_1, "output_text", outputBuilder.toString().trim() );
		pipelineCursor_1.destroy();
			
		// --- <<IS-END>> ---

                
	}

	// --- <<IS-START-SHARED>> ---
	private static List<String> separateWords(String text) {
	    List<String> tokens = new ArrayList<>();
	    Matcher matcher = Pattern.compile("\\w+|\\p{Punct}|\\s+").matcher(text);
	    while (matcher.find()) {
	        tokens.add(matcher.group());
	    }
	    return tokens;
	}
	
	private static String toUnang(String word) {
		String[] subWords = word.split("(?<=[aiueo])(?=[bcdfghjklmnpqrstvwxyz]*[aiueo][bcdfghjklmnpqrstvwxyz]*\\b)");
	
		String x_raw = subWords[subWords.length - 1];
	
		String b = (subWords.length == 2) ? subWords[0] : "";
	
		StringBuilder x_builder = new StringBuilder();
		String c = "a";
	
		for (char ch : x_raw.toCharArray()) {
		    if ("aiueo".indexOf(ch) != -1) {
		        c = String.valueOf(ch);
		        x_builder.append('a');
		    } else {
		        x_builder.append(ch);
		    }
		}
	
		String x = x_builder.toString();
	
		return "u" + x + " " + b + "n" + c + "ng";
	}
	// --- <<IS-END-SHARED>> ---
}

