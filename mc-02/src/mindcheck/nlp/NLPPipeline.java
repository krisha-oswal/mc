package mindcheck.nlp;

import mindcheck.model.*;
import java.util.*;

/**
 * Orchestrates the NLP Chain of Responsibility pipeline.
 * Executes each TextProcessor in sequence.
 */
public class NLPPipeline {
    private final List<TextProcessor> chain;

    public NLPPipeline(List<EmotionResult> recentResults) {
        chain = new ArrayList<>();
        chain.add(new Tokenizer());
        chain.add(new EmojiProcessor());
        chain.add(new StopwordFilter());
        chain.add(new PorterStemmer());
        chain.add(new NegationHandler());
        chain.add(new IntensityScorer());
        chain.add(new NGramDetector());
        chain.add(new ContextAnalyzer(recentResults));
    }

    public NLPPipeline() {
        this(new ArrayList<>());
    }

    public ProcessedText run(String rawText) {
        ProcessedText pt = new ProcessedText(rawText);
        for (TextProcessor processor : chain) {
            pt = processor.process(pt);
        }
        return pt;
    }
}
