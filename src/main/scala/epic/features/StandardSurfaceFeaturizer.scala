package epic.features

import epic.framework.Feature
import breeze.linalg.Counter
import scala.Array
import epic.parser.features.PairFeature
import scala.collection.mutable.ArrayBuffer
import epic.parser.features.StandardSpanFeatures._

/**
 *
 * @author dlwh
 */
@SerialVersionUID(1L)
class StandardSurfaceFeaturizer(wordFeaturizer: WordFeaturizer[String]) extends SurfaceFeaturizer[String] with Serializable {
  def this(wordCounts: Counter[String, Double],
           functionWordThreshold: Int = 100,
           unknownWordThreshold: Int = 2)  = this(new MinimalWordFeaturizer(wordCounts, functionWordThreshold, unknownWordThreshold))

  def anchor(words: IndexedSeq[String]):SurfaceFeatureAnchoring[String] = {
    val w = words
    val wordAnch = wordFeaturizer.anchor(w)
    import wordAnch._
    new SurfaceFeatureAnchoring[String] {
      val wholeSentenceIsUpperCase = words.forall(_.forall(c => !c.isLetter || c.isUpper))
      def words = w
      val sentenceLengthFeature = SentenceLengthFeature(distanceBinner.binnedDistance(0,words.length))


      def featuresForSpan(begin: Int, end: Int): Array[Feature] = {
        val feats = ArrayBuffer[Feature]()
        if (begin < end - 1) {
          for(f1 <- featuresForWord(begin); f2 <- featuresForWord(end)) {
            feats += WordEdges('BeginEnd, f1, f2)
          }
          for(f1 <- featuresForWord(begin); f2 <- featuresForWord(end-1)) {
            feats += WordEdges('Inside, f1, f2)
          }

          for(f1 <- featuresForWord(begin-1); f2 <- featuresForWord(end)) {
            feats += WordEdges('Outside, f1, f2)
          }


          for(f1 <- featuresForWord(begin)) {
            feats += WordBoundary('Begin, f1)
          }

          for(f1 <- featuresForWord(end-1)) {
            feats += WordBoundary('Outside, f1)
          }
        }



        feats += sentenceLengthFeature
        if(wholeSentenceIsUpperCase)
          feats += WholeSentenceIsUpperCaseFeature


        if (begin == 0)
          feats += BeginSentFeature
        if(end == words.length)
          feats += EndSentFeature
        if (begin == 0 && end == words.length)
          feats += WholeSentFeature

        feats += SpanShapeFeature(SpanShapeGenerator.signatureFor(words, begin, end, includeContext = true))
        feats += SpanShapeFeature(SpanShapeGenerator.signatureFor(words, begin, end, includeContext = false))

        val spanLength = SpanLengthFeature(distanceBinner.binnedDistance(begin, end))
        for(f <- feats.toArray) {
          feats += PairFeature(f, spanLength)
        }
        feats += spanLength

        feats.toArray
      }
    }

  }

  private val distanceBinner = DistanceBinner()

}

case class FirstWordCapsAnd(f: Feature) extends Feature
case class NthWordCapsAnd(f: Feature) extends Feature
case class SentenceLengthFeature(length: Int) extends Feature
case object WholeSentenceIsUpperCaseFeature extends Feature
case class WordFeature(word: Any, kind: Symbol) extends Feature

case object BoundaryFeature extends Feature