package chp38.ML.SentimentAnalysis;
import chp38.Files.FileReader;
import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunking;
import com.aliasi.hmm.HiddenMarkovModel;
import com.aliasi.hmm.HmmDecoder;
import com.aliasi.tag.Tagging;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.FastCache;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Class to handle communication with Lingpipe, and analyse
 * the news headlines.
 *
 * @author Charles Palmer
 */
public class SentimentAnalysis {

    /**
     * ArrayList to hold the corpus, to save having to keep reading from file
     */
    private ArrayList<String> corpus = new ArrayList<>();

    /**
     * Stores the HmmDecoder for POS-Tagging
     */
    private HmmDecoder posTagger;

    /**
     * Tokenizer, to tokenize the headline
     */
    private TokenizerFactory tokenizerFactory;

    /**
     * Variable to hold files location
     */
    private String filesFolder;

    /**
     * For the chunking of the tokens
     */
    private HeadlineChunker chunker;

    public SentimentAnalysis(String filesFolder) {
        this.filesFolder = filesFolder;
        this.corpus();
    }

    /**
     * Method to load the Hmm model for the POS tagger, and chunker
     */
    public void loadHmmModel(){
        String dir = this.filesFolder + "/train-brown";
        File hmmFile = new File(dir);
        int cacheSize = Integer.valueOf(256);
        FastCache<String,double[]> cache = new FastCache<String,double[]>(cacheSize);

        HiddenMarkovModel posHmm;
        try {
            posHmm
                    = (HiddenMarkovModel)
                    AbstractExternalizable.readObject(hmmFile);
        } catch (IOException e) {
            System.out.println("Exception reading model=" + e);
            e.printStackTrace(System.out);
            return;
        } catch (ClassNotFoundException e) {
            System.out.println("Exception reading model=" + e);
            e.printStackTrace(System.out);
            return;
        }

        this.posTagger  = new HmmDecoder(posHmm,null,cache);
        this.tokenizerFactory = IndoEuropeanTokenizerFactory.INSTANCE;
        this.chunker = new HeadlineChunker(posTagger,tokenizerFactory);
    }

    /**
     * Start the process of learning the sentiment of a news headline
     *
     * @param headline the headline to detect the sentiment of
     * @return Double the sentiment of the headline
     */
    public double detectSentiment(String headline){
        if(this.chunker == null){
            this.loadHmmModel();
        }

        String[] tokens
                = this.tokenizerFactory
                .tokenizer(headline.toCharArray(), 0, headline.length())
                .tokenize();

        List<String> tokenList = Arrays.asList(tokens);
        Tagging<String> tagging = posTagger.tag(tokenList);

        Chunking chunking = chunker.chunk(headline);
        CharSequence cs = chunking.charSequence();

        double SO = 0.0;
        int count = 0;

        for (Chunk chunk : chunking.chunkSet()) {
            count++;
            String type = chunk.type();
            int start = chunk.start();
            int end = chunk.end();
            CharSequence text = cs.subSequence(start, end);
            SO += this.calculateSO((String)text);

        }

        return SO/count;
    }

    /**
     * Method to calculate the semantic orientation of a key-phrase.
     *
     * @param phrase the extracted phrase from the headline
     * @return double Semantic Orientation of the headline
     */
    private double calculateSO(String phrase){
        double positivePMI = calculatePMI(phrase, "positive");

        double negativePMI = calculatePMI(phrase, "negative");

        return positivePMI - negativePMI;
    }

    /**
     * Method to calculate the PMI of a key-phrase extracted from a news headline
     *
     * @param phrase The extracted phrase
     * @param word The given keyword (Positive/Negative)
     * @return double The PMI
     */
    private double calculatePMI(String phrase, String word){
        double PMI;
        double combinedProb, probPhrase, probWord;

        combinedProb = searchCorpus(phrase, word);
        probPhrase = searchCorpus(phrase, "NA");
        probWord = searchCorpus("NA", word);

        if(probPhrase == 0){
            probPhrase = 0.001;
        }

        if(probWord == 0){
            probWord = 0.001;
        }

        if(combinedProb == 0){
            combinedProb = 0.001;
        }

        PMI = combinedProb / (probPhrase * probWord);
        PMI = Math.log(PMI) / Math.log(2);

        return PMI;
    }

    /**
     * Search the corpus for a given phrase and word, phrase or word
     *
     * @param phrase The extracted phrase
     * @param word The given keyword
     * @return int hit count in the corpus
     */
    private int searchCorpus(String phrase, String word){

        int count = 0;

        for(String headline : this.corpus) {

            if (word.equals("NA")) {
                if (headline.contains(phrase)) {
                    count++;
                }
            } else if (phrase.equals("NA")) {
                if (headline.contains(word)) {
                    count++;
                }
            } else {
                if (headline.contains(phrase) && headline.contains(word)) {
                    count++;
                }
            }

        }
        return count;
    }

    /**
     * If the Corpus hasn't been loaded in locally, then call getCorpus
     * to load it in. Else, return the corpus.
     *
     * @return corpus
     */
    private void corpus(){
        if(this.corpus.size() == 0){
            this.corpus = this.getCorpus();
        }
    }

    /**
     * Retrieve the headlines for the Corpus from the file, and store them
     * in an ArrayList.
     *
     * @return headlines
     */
    private ArrayList<String> getCorpus(){
        String line = "";
        ArrayList<String> headlines = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new java.io.FileReader(this.filesFolder + "/RedditNews.csv"))) {
            while ((line = br.readLine()) != null) {
                String[] data = FileReader.splitLine(line);

                headlines.add(data[1]);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return headlines;
    }

}