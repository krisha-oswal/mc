package mindcheck.nlp;

import mindcheck.model.ProcessedText;

/**
 * Interface for the NLP Chain of Responsibility pipeline.
 * Each processor transforms a ProcessedText and passes it along.
 */
public interface TextProcessor {
    ProcessedText process(ProcessedText input);
    String getStepName();
}
